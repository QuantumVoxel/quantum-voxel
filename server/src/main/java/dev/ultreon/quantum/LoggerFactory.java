package dev.ultreon.quantum;

public class LoggerFactory {
    public static Logger getLogger(String name) {
        return GamePlatform.get().getLogger(name);
    }

    public static Logger getLogger(Class<?> type) {
        return getLogger(type.getSimpleName());
    }
}
