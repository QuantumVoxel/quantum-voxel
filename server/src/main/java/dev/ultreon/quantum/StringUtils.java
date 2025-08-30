package dev.ultreon.quantum;

import dev.ultreon.quantum.text.CharUtils;

public class StringUtils {
    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String uncapitalize(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static String join(String lineBreak, String... array) {
        StringBuilder builder = new StringBuilder();
        for (String s : array) {
            builder.append(s).append(lineBreak);
        }
        return builder.toString();
    }

    public static String join(String lineBreak, Iterable<String> iterable) {
        StringBuilder builder = new StringBuilder();
        for (String s : iterable) {
            builder.append(s).append(lineBreak);
        }
        return builder.toString();
    }

    public static boolean isBlank(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!CharUtils.isWhitespace(text.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
