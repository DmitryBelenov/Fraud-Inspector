package kafka;

import kafka.client.KafkaConsumerClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.FIParams;
import sys.type.StatTransactionData;
import sys.type.TimeMonitor;
import utils.UtilsSerialization;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BootstrapStatLoader {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    public static final Set<String> RECOVERED_KEYS = ConcurrentHashMap.newKeySet();

    private static KafkaConsumerClient<String, StatTransactionData> consumerClient;

    private static void initConsumer() {
        consumerClient = new KafkaConsumerClient<>("bootstrap.stat.loader", FIParams.statTopicName, StringDeserializer.class);
    }

    public static void load() {
        initConsumer();

        log.info("Start stat bootloader");
        final long endOffset = consumerClient.endOffset();
        consumerClient.seekToOffset(0L);
        boolean read = true;

        final Map<String, StatTransactionData> dataMap = new HashMap<>();
        while (read) {
            dataMap.clear();
            ConsumerRecords<String, StatTransactionData> records = consumerClient.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, StatTransactionData> record : records) {
                if (endOffset > 0 && record.offset() > endOffset) {
                    read = false;
                    break;
                }
                StatTransactionData trData = UtilsSerialization.deserialize(record.value());
                dataMap.put(record.key(), trData);
            }
            if (dataMap.isEmpty()) {
                break;
            }
            for (Map.Entry<String,StatTransactionData> data : dataMap.entrySet()) {
                StatTransactionData tData = data.getValue();
                log.info("Read from Kafka, stat data: " + String.join(",", tData.getAttrValues()));

                Iterator<TimeMonitor> tmIt = TimeMonitor.getIterator();
                while (tmIt.hasNext()) {
                    TimeMonitor tm = tmIt.next();
                    tm.addToStatistics(tData);
                }
                final String key = data.getKey();
                RECOVERED_KEYS.add(key);

                log.info("Key " + key + " was added to existing set");
            }
            consumerClient.commitAsync();
        }
        BootstrapStatChecker.setLoaded();

        log.info("Stop stat bootloader");
    }
}
