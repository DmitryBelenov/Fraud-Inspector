package kafka;

import kafka.client.KafkaConsumerClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import service.HttpProcessor;
import service.HttpReply;
import sys.FIParams;
import sys.TransactionCheckingCenter;
import sys.type.CheckResult;
import sys.type.DeterminingFunction;
import sys.type.DeterminingFunction.PostAction;
import sys.type.TransportTransactionData;
import utils.StringUtils;
import utils.SysUtils;

import javax.servlet.AsyncContext;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class PaymentLoader implements Runnable {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final AtomicBoolean LOADING = new AtomicBoolean(true);

    public static void start() {
        final ExecutorService executor = SysUtils.newFixedThreadPool("Pmt-NoDelay-Loader", 1);
        executor.execute(new PaymentLoader());
    }

    @Override
    public void run() {
        final KafkaConsumerClient<String, TransportTransactionData> kafkaConsumerCli = new KafkaConsumerClient<>("payments-loader", FIParams.paymentsTopicName, StringDeserializer.class);
        final long loadFrom = kafkaConsumerCli.endOffset();
        kafkaConsumerCli.seekToOffset(loadFrom);

        log.info("Start no delay payments loader");
        while (LOADING.get()) {
            final ConsumerRecords<String, TransportTransactionData> records = kafkaConsumerCli.poll(Duration.ofMillis(50));
            if (!records.isEmpty()) {
                for (final ConsumerRecord<String, TransportTransactionData> record : records) {
                    if (LOADING.get()) {
                        TransportTransactionData transport = kafkaConsumerCli.readRecord(record);
                        final Long id = transport.getId();
                        HttpReply reply;
                        try {
                            final CheckResult result = TransactionCheckingCenter.instance().checkTransaction(transport.getData(), id);
                            PostAction pA = result.getPostAction();
                            reply = new HttpReply(true, StringUtils.booleanYNWrapper(result.isSuspicious()), pA == null ? "N/A" : pA.getShortName(), result.getInfo());
                        } catch (Exception se) {
                            log.error(se);
                            reply = new HttpReply(false, "N/A", "N/A", "Transaction handling error");
                        }

                        final AsyncContext asyncContext = transport.getRespCtx();
                        HttpProcessor.writeAsyncReply(id, asyncContext, reply);
                        kafkaConsumerCli.commitAsync();
                    }
                }
            }
        }

        kafkaConsumerCli.close();
        log.info("Stop no delay payments loader");
    }

    public static void initClose() {
        LOADING.set(false);
    }
}
