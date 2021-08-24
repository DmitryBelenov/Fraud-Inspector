package kafka;

import kafka.client.KafkaConsumerClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import sys.FIParams;
import sys.type.TimeInterval;
import sys.type.StatTransactionData;
import utils.SysUtils;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SlidingWindow implements Runnable {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private final TimeInterval interval;
    private final TaskScheduler taskScheduler;

    private final String consumerName;
    private final Consumer<StatTransactionData> statCallback;
    private final KafkaConsumerClient<String, StatTransactionData> kafkaConsumerCli;

    private boolean initDone;
    private boolean closing;

    public SlidingWindow( String consumerName, TimeInterval interval, Consumer<StatTransactionData> statCallback) {
        this.interval = interval;
        this.consumerName = consumerName;
        this.statCallback = statCallback;
        this.kafkaConsumerCli = new KafkaConsumerClient<>(this.consumerName, FIParams.statTopicName, StringDeserializer.class);

        taskScheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            private final AtomicInteger num = new AtomicInteger();

            @Override
            public Thread newThread(final Runnable r) {
                final Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setDaemon(true);
                thread.setName("sw." + consumerName + ".ts-" + num.getAndAdd(1));
                return thread;
            }
        }));

        final ExecutorService executor = SysUtils.newFixedThreadPool("sw." + consumerName + ".es", 1);
        executor.execute(this);
    }

    @Override
    public void run() {
        try {
            BootstrapStatLoader.BOOTSTRAP_LATCH.await();
        } catch (InterruptedException e) {
            log.error("Bootstrap stat loading error. Unable to start sliding window " + consumerName, e);
            return;
        }

        log.info("Start sw consumer: " + consumerName);
        while (!closing) {
                final ConsumerRecords<String, StatTransactionData> records = kafkaConsumerCli.poll();
                if (!records.isEmpty()) {
                    for (final ConsumerRecord<String, StatTransactionData> record : records) {
                        if (interval.readyForCleanStats(record)) {
                            if (!closing) {
                                StatTransactionData trData = kafkaConsumerCli.readRecord(record);
                                statCallback.accept(trData);
                            }
                        } else {
                            kafkaConsumerCli.shiftPosition(record.offset());
                            kafkaConsumerCli.commitAsync();

                            final Date schedule = new Date(record.timestamp() + interval.getIntervalMs());
                            taskScheduler.schedule(this, schedule);

                            log.trace("Future event registered for " + consumerName + " on " + schedule);
                            return;
                        }
                    }
                    kafkaConsumerCli.commitAsync();
                } else {
                    final long scheduleAt = new Date().getTime() + interval.getIntervalMs();
                    taskScheduler.schedule(this, new Date(scheduleAt));

                    return;
                }
        }
        kafkaConsumerCli.close();
        log.info("Close sw consumer: " + consumerName);
    }

    public boolean initDone() {
        return initDone;
    }

    public void close() {
        log.info("Stopping sw consumer" + consumerName);
        closing = true;
    }
}
