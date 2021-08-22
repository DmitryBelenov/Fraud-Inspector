package sys.key;

import java.io.Serializable;
import java.util.Arrays;

public class FIKey<T> implements Serializable {
    private static final long serialVersionUID = 6340934543336839235L;

    @SuppressWarnings("unchecked")
    public FIKey(final T... keys) {
        this.keys = keys;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0)
            hash = h = Arrays.hashCode(keys);
        return h;
    }

    private int hash;

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object other) {
        if (this == other)
            return true;

        if (!(other instanceof FIKey))
            return false;

        final FIKey<T> otherFIKey = (FIKey<T>) other;

        return Arrays.equals(keys, otherFIKey.keys);
    }

    private final T[] keys;

    public T[] getKeys() {
        return keys;
    }

    @Override
    public String toString() {
        return Arrays.toString(keys);
    }
}
