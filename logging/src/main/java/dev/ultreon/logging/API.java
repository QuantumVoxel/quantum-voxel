package dev.ultreon.logging;

import java.io.PrintStream;

public interface API {
    Property<API> PROPERTY = new Property<>(new API() {
        @Override
        public void addShutdownHook(Runnable runnable) {
            // Do nothing
        }

        @Override
        public void setOut(PrintStream oldOut) {
            // Do nothing
        }

        @Override
        public void setErr(PrintStream oldErr) {
            // Do nothing
        }
    });

    void addShutdownHook(Runnable runnable);

    void setOut(PrintStream oldOut);

    void setErr(PrintStream oldErr);
}
