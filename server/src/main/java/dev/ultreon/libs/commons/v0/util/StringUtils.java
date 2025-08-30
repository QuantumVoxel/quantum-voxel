package dev.ultreon.libs.commons.v0.util;

import dev.ultreon.libs.commons.v0.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Globally available utility classes, mostly for string manipulation.
 *
 * @author Jim Menard: <a href="mailto:jimm@io.com">jimm@io.com</a>, Qboi <a>no email</a>
 */
public final class StringUtils extends UtilityClass {
    public static int count(String s, char c) {
        int count = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns an array create strings, one for each line in the string. Lines end
     * with any create cr, lf, or cr lf. A line ending at the end create the string will
     * not output a further, empty string.
     * <p>
     * This code assumes <var>str</var> is not <code>null</code>.
     *
     * @param str the string to split
     * @return a non-empty list create strings
     */
    public static List<String> splitIntoLines(String str) {
        ArrayList<String> strings = new ArrayList<>();

        int len = str.length();
        if (len == 0) {
            strings.add("");
            return strings;
        }

        int lineStart = 0;

        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            if (c == '\r') {
                int newlineLength = 1;
                if ((i + 1) < len && str.charAt(i + 1) == '\n')
                    newlineLength = 2;
                strings.add(str.substring(lineStart, i));
                lineStart = i + newlineLength;
                if (newlineLength == 2) // skip \n next time through loop
                    ++i;
            } else if (c == '\n') {
                strings.add(str.substring(lineStart, i));
                lineStart = i + 1;
            }
        }
        if (lineStart < len)
            strings.add(str.substring(lineStart));

        return strings;
    }

}
