package utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.equalsIgnoreCase("null") || str.length() == 0;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence sc) {
        return !isEmpty(sc);
    }

    public static String booleanYNWrapper(final boolean val) {
        return val ? "Y" : "N";
    }

    public static String getSQLValues(Object... values) {
        List<String> quoted = new ArrayList<>();
        for (Object o : values) {
            if (o instanceof String) {
                final String strValue = (String) o;
                quoted.add("'" + strValue + "'");
            } else if (o instanceof Long) {
                final Long numValue = (Long) o;
                quoted.add(String.valueOf(numValue));
            }
        }
        return String.join(",", quoted);
    }
}
