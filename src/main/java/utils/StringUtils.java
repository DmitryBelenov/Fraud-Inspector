package utils;

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
}
