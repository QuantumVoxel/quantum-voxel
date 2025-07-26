package dev.ultreon.quantum.network.system;

import com.esotericsoftware.minlog.Log;
import dev.ultreon.quantum.Logger;
import dev.ultreon.quantum.LoggerFactory;

public class KyroNetSlf4jLogger extends Log.Logger {
    public static final Log.Logger INSTANCE = new KyroNetSlf4jLogger();

    public final Logger logger = LoggerFactory.getLogger("KryoNet");

    private KyroNetSlf4jLogger() {
    }

    public static void set() {
        Log.setLogger(INSTANCE);
    }

    @Override
    public void log(int level, String category, String message, Throwable ex) {
        switch (level) {
            case Log.LEVEL_INFO:
                logger.info(message, ex);
                break;
            case Log.LEVEL_WARN:
                logger.warn(message, ex);
                break;
            case Log.LEVEL_ERROR:
                logger.error(message, ex);
                break;
            default:
                logger.debug(message, ex);
                break;
        }
    }
}
