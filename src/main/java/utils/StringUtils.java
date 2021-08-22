package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {

    public static final String PARAM_DELIMITERS = ";,|";

    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.length() == 0 || str.equalsIgnoreCase("null");
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

    public static boolean stringYNWrapper(final String val) {
        return val.equalsIgnoreCase("Y");
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

    public static List<String> breakString(String str, String delimiter) {
        return breakString(str, delimiter, new ArrayList<String>());
    }

    private static <T extends Collection<String>> T breakString(String str, String delimiter, final T lResult) {
        StringTokenizer tokenizer = null;
        if (delimiter == null)
            tokenizer = new StringTokenizer(str);
        else
            tokenizer = new StringTokenizer(str, delimiter);

        while (tokenizer.hasMoreTokens())
            lResult.add(tokenizer.nextToken());

        return lResult;
    }
}
