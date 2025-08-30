package dev.ultreon.quantum.switchgdx;

import dev.ultreon.quantum.Logger;

@SuppressWarnings("CallToPrintStackTrace")
public class SwitchGdxLogger implements Logger {
    public SwitchGdxLogger(String name) {

    }

    @Override
    public void log(Level level, String message, Throwable t) {
        System.out.println(level.name() + ": " + message);
        if (t != null) {
            t.printStackTrace();
        }
    }
}
