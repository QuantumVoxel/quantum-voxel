package dev.ultreon.quantum.log;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.Logger;

@Deprecated
public class LoggerFactory {
    public static Logger getLogger(String name) {
        return GamePlatform.get().getLogger(name);
    }

    public static Logger getLogger(Class<?> type) {
        return GamePlatform.get().getLogger(type.getSimpleName());
    }
}
