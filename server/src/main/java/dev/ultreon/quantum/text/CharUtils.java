package dev.ultreon.quantum.text;

public class CharUtils {
    public static boolean isAsciiAlphanumeric(char it) {
        return (it >= 'a' && it <= 'z') || (it >= 'A' && it <= 'Z') || (it >= '0' && it <= '9');
    }

    public static boolean isAlphabetic(char r) {
        return (r >= 'a' && r <= 'z') || (r >= 'A' && r <= 'Z');
    }

    public static boolean isNumeric(char r) {
        return r >= '0' && r <= '9';
    }

    public static boolean isSpace(char r) {
        return r == ' ' || r == '\t' || r == '\n' || r == '\r' || r == '\f';
    }

    public static boolean isControl(char r) {
        return r < 32 || r == 127;
    }

    public static boolean isPrintable(char r) {
        return !isControl(r);
    }

    public static boolean isPrintable(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isPrintable(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWhitespace(char c) {
        return isSpace(c) || c == '\u00A0' || c == '\u202F' || c == '\u205F' || c == '\u3000' || c == '\uFEFF';
    }
}
