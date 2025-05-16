package dev.ultreon.quantum.debug.timing;

import java.util.Locale;

public class Timed {
    public final String name;
    public final long time;

    public Timed(String name, long time) {
        this.name = name;
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s: %.2fms", name, time / 1_000_000.0);
    }
}
