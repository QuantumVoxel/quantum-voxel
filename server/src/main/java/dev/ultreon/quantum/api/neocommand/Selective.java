package dev.ultreon.quantum.api.neocommand;

import java.util.Objects;

public final class Selective {
    private final String name;
    private final Selector<?>[] selectors;

    public Selective(String name, Selector<?>... selectors) {
        this.name = name;
        this.selectors = selectors;
    }

    public String name() {
        return name;
    }

    public Selector<?>[] selectors() {
        return selectors;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Selective) obj;
        return Objects.equals(this.name, that.name) &&
               Objects.equals(this.selectors, that.selectors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, selectors);
    }

    @Override
    public String toString() {
        return "Selective[" +
               "name=" + name + ", " +
               "selectors=" + selectors + ']';
    }

}
