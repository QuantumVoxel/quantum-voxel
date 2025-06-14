package dev.ultreon.quantum.teavm;

import dev.ultreon.quantum.Logger;
import org.teavm.jso.JSBody;

public class TeaVMLogger implements Logger {
    private final String name;

    public TeaVMLogger(String name) {
        this.name = name;
    }

    @Override
    public void log(Level level, String message, Throwable t) {
        if (level == null) return;

        message = "[" + name + "] " + message;

        if (t == null) {
            switch (level) {
                case TRACE:
                    Console.trace(message);
                    break;
                case DEBUG:
                    Console.debug(message);
                    break;
                case INFO:
                    Console.info(message);
                    break;
                case WARN:
                    Console.warn(message);
                    break;
                case ERROR:
                    Console.error(message);
                    break;
            }
            return;
        }

        switch (level) {
            case TRACE:
                Console.trace(message, t);
                break;
            case DEBUG:
                Console.debug(message);
                break;
            case INFO:
                Console.info(message, t);
                break;
            case WARN:
                Console.warn(message, t);
                break;
            case ERROR:
                Console.error(message, t);
                break;
        }
    }
}
