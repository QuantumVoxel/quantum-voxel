package dev.ultreon.quantum.desktop;

import dev.ultreon.quantum.Logger;
import org.slf4j.LoggerFactory;

public class DesktopLogger implements Logger {
    private final org.slf4j.Logger logger;

    public DesktopLogger(String name) {
        this.logger = LoggerFactory.getLogger(name);
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (level == null) return;
        if (throwable == null) {
            switch (level) {
                case TRACE:
                    logger.trace(message);
                    break;
                case DEBUG:
                    logger.debug(message);
                    break;
                case INFO:
                    logger.info(message);
                    break;
                case WARN:
                    logger.warn(message);
                    break;
                case ERROR:
                    logger.error(message);
                    break;
            }
            return;
        }
        switch (level) {
            case TRACE:
                logger.trace(message, throwable);
                break;
            case DEBUG:
                logger.debug(message, throwable);
                break;
            case INFO:
                logger.info(message, throwable);
                break;
            case WARN:
                logger.warn(message, throwable);
                break;
            case ERROR:
                logger.error(message, throwable);
                break;
        }
    }
}
