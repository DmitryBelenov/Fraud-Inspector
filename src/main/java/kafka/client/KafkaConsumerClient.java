package kafka.client;

import sys.type.IRestore;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import utils.UtilsSerialization;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

public class KafkaConsumerClient<K, V extends IRestore> implements Closeable {
    private static final Duration DURATION = Duration.ofMillis(100);

    private final KafkaConsumer<K, V> consumer;
    private Properties props = null;
    private final String name;
    private final TopicPartition partition;
    private final Collection<TopicPartition> partitions;
    private long curOffset;

    public static final int MAX_POLL_RECORDS = 1000 /* default is 500 */; // set by config

    public <T extends Deserializer<?>> KafkaConsumerClient(final String name, final String topicName, final Class<T> keyDeserializer) {
        this.name = name;

        fillProperties(keyDeserializer);
        consumer = new KafkaConsumer<>(props);
        partition = new TopicPartition(topicName, 0);
        partitions = Collections.singletonList(partition);
        consumer.assign(partitions);

        seekToOffset(endOffset());
    }

    private <T extends Deserializer<?>> void fillProperties(final Class<T> keyDeserializer) {
        props = new Properties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "system identifier" + "." + name);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka broker from config");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
    }

    public void commitAsync() {
        consumer.commitAsync();
        curOffset = endOffset();
    }

    public long position() {
        return consumer.position(partition);
    }

    public void pause() {
        consumer.pause(partitions);
    }

    public void resume() {
        consumer.resume(partitions);
    }

    @Override
    public void close() {
        consumer.unsubscribe();
        consumer.close();
    }

    public void shiftPosition(final long offset) {
        for (final TopicPartition tp : consumer.assignment()) {
            consumer.seek(tp, offset);
            curOffset = offset;
        }
    }

    public ConsumerRecords<K, V> poll() {
        return consumer.poll(DURATION);
    }

    public long endOffset() {
        return consumer.endOffsets(partitions).get(partition);
    }

    public OffsetAndTimestamp offsetForTime(final long timestamp) {
        return consumer.offsetsForTimes(Collections.singletonMap(partition, timestamp)).get(partition);
    }

    public void seekToOffset(final long offset) {
        consumer.seek(partition, offset);
    }

    public V readRecord(final ConsumerRecord<K, V> record) {
        final V value = UtilsSerialization.deserialize(record.value());
        Objects.requireNonNull(value).restore();
        return value;
    }
}
