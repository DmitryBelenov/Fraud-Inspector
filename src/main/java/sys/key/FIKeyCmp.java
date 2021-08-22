package sys.key;

@SuppressWarnings("rawtypes")
public class FIKeyCmp extends FIKey<Comparable> implements Comparable<FIKey<Comparable>> {
    private static final long serialVersionUID = 3702104241362305931L;

    public FIKeyCmp(final Comparable... keys) {
        super(keys);
    }

    @Override
    public int compareTo(final FIKey<Comparable> o) {
        if (this == o)
            return 0;

        final Comparable[] a = getKeys();
        final Comparable[] a2 = o.getKeys();

        final int length = a.length;
        for (int i = 0; i < length; i++) {
            final Comparable v = a[i];
            final Comparable v2 = a2[i];

            if (v == v2)
                continue;

            if (v == null)
                return -1;

            if (v2 == null)
                return 1;

            @SuppressWarnings("unchecked")
            final int res = v.compareTo(v2);
            if (res != 0)
                return res;
        }
        return 0;
    }

    @Override
    public String toString() {
        final Object[] keys = getKeys();
        if (keys == null)
            return "null";
        int iMax = keys.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; ; i++) {
            final Object o = keys[i];
            if (o == null)
                sb.append("null");
            else
                sb.append(o.toString());

            if (i == iMax)
                return sb.toString();
            sb.append(':');
        }
    }
}
