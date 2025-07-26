package dev.ultreon.logging;

import java.util.Objects;

public class LogCategory {
    public static final LogCategory DEFAULT = new LogCategory("DEFAULT");
    private final String name;

    public LogCategory(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LogCategory that = (LogCategory) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public String getName() {
        return name;
    }
}
