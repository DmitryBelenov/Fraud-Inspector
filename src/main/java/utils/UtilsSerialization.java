package utils;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class UtilsSerialization {

    public static byte[] serialize(final Serializable data) {
        return SerializationUtils.serialize(data);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(final Serializable data) {
        if (data instanceof byte[])
            return (T) SerializationUtils.deserialize((byte[]) data);

        return (T) data;
    }
}
