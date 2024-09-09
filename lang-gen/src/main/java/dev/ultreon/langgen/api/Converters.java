package dev.ultreon.langgen.api;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class for converting between package names.
 *
 * @author XyperCode
 * @since 1.0
 */
public class Converters {
    private static final LinkedHashMap<String, String> CONVERTERS = new LinkedHashMap<>();

    private Converters() {

    }

    /**
     * Gets the name of the package name to convert to.
     *
     * @param name the name of the package
     * @return the name of the package to convert to
     */
    public static @Nullable String convert(String name) {
        for (Map.Entry<String, String> entry : CONVERTERS.sequencedEntrySet()) {
            if (name.startsWith(entry.getKey() + ".")) {
                String value = entry.getValue();
                return value + name.substring(entry.getKey().length());
            }
        }
        return null;
    }

    /**
     * Register a converter for package names.
     *
     * @param from the package name to convert from
     * @param to the package name to convert to
     */
    public static void register(String from, String to) {
        CONVERTERS.put(from, to);
    }
}
