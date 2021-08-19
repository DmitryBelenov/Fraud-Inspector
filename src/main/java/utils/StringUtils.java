package utils;

public class StringUtils {
    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.equalsIgnoreCase("null") || str.length() == 0;
    }
}
