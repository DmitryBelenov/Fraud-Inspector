package kafka.client;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.FIParams;
import utils.UtilsSerialization;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KafkaProducerClient {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private final Map<Class<?>, KafkaProducer<Object, Object>> producers = new HashMap<>();
    private final KafkaProducer<Object, Object> defProducer;
    public static KafkaProducerClient instance;

    public static void setInstance() {
        if (instance == null) {
            instance = new KafkaProducerClient();
            log.info("Kafka producer client instantiated");
        }
    }

    private KafkaProducerClient() {
        final Properties props = new Properties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, FIParams.kafkaBrokerHost);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        final KafkaProducer<Object, Object> longProducer = new KafkaProducer<>(props);
        producers.put(Long.class, longProducer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        final KafkaProducer<Object, Object> stringProducer = new KafkaProducer<>(props);
        defProducer = stringProducer;
        producers.put(String.class, stringProducer);
    }

    public void produce(final String topic, final Serializable data) {
        produce(topic, null, data);
    }

    public <K> void produce(final String topic, final K key, final Serializable data) {
        produce(topic, null, key, data);
    }

    public <K> void produce(final String topic, final Long timestamp, final K key, final Serializable data) {
        if (key != null && !producers.containsKey(key.getClass())) {
            throw new RuntimeException("Unknown class of producer key: " + key.getClass());
        }
        final byte[] serializedData = UtilsSerialization.serialize(data);
        if (key == null) {
            defProducer.send(new ProducerRecord<>(topic, 0, timestamp, null, serializedData));
        } else {
            producers.get(key.getClass()).send(new ProducerRecord<>(topic, 0, timestamp, key, serializedData));
        }
    }

    public void flush() {
        producers.forEach((k,v) -> v.flush());
    }
}
