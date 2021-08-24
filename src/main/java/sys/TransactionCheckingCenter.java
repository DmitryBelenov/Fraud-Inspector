package sys;

import database.DBConnectionHolder;
import kafka.client.KafkaProducerClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.type.CheckResult;
import sys.type.StatTransactionData;
import sys.type.TransportTransactionData;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionCheckingCenter {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static TransactionCheckingCenter instance;
    private static final DBConnectionHolder DB_CONN = DBConnectionHolder.instance();

    private final KafkaProducerClient producer;

    private static final ReentrantLock lock = new ReentrantLock();

    private TransactionCheckingCenter() {
        producer = KafkaProducerClient.instance;
    }

    public static TransactionCheckingCenter instance() throws Exception {
        if (instance == null) {
            synchronized (TransactionCheckingCenter.class) {
                if (instance == null) {
                    instance = new TransactionCheckingCenter();
                }
            }
        }
        return instance;
    }

    public CheckResult checkTransaction(final StatTransactionData data, final Long id) throws Exception {
        // if result already has in DB keep it
        CheckResult result = CheckResult.get(id);
        if (result == null) {
            result = check(id, data);
            storeResultToDB(result);
        }
        data.setSuspicious(result.isSuspicious());

        putStatToKafka(String.valueOf(id), data);
        return result;
    }

    public void storeResultToDB(final CheckResult result) {
        DB_CONN.getSource().insert(result);
    }

    public void putTransactionToKafka(final String key, final TransportTransactionData transportData) throws Exception {
        produce(FIParams.paymentsTopicName, getTransactionTime(transportData.getData()), key, transportData);
    }

    public void putStatToKafka(final String key, final StatTransactionData data) throws Exception {
        produce(FIParams.statTopicName, getTransactionTime(data), key, data);
    }

    private long getTransactionTime(final StatTransactionData data) throws Exception {
        return new Date().getTime();
    }

    private void produce(final String topic, final long transactionTime, final String key, final Serializable data) throws Exception {
        log.debug("Producing message with id:" + key + " to kafka");
        lock.lock();
        try {
            producer.produce(topic, transactionTime, key, data);
        } finally {
            lock.unlock();
        }
    }

    private CheckResult check(final Long id, final StatTransactionData data) throws Exception {
        return ResultExtractor.extract(id, data);
    }
}
