package dev.ultreon.quantum.client.gui.icon;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.Identifier;

import java.util.Objects;

public final class GenericIcon implements Icon {
    public static final GenericIcon LOCKED = new GenericIcon(16, 16, 0, 0);
    public static final GenericIcon UNLOCKED = new GenericIcon(16, 16, 16, 0);
    public static final GenericIcon RELOAD = new GenericIcon(16, 16, 0, 16);
    private final int width;
    private final int height;
    private final int u;
    private final int v;

    public GenericIcon(int width, int height, int u, int v) {
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
    }

    @Override
    public Identifier id() {
        return QuantumClient.id("textures/gui/icons/generic.png");
    }

    @Override
    public int width() {
        return 16;
    }

    @Override
    public int height() {
        return 16;
    }

    @Override
    public int texWidth() {
        return 256;
    }

    @Override
    public int texHeight() {
        return 256;
    }

    @Override
    public int u() {
        return u;
    }

    @Override
    public int v() {
        return v;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GenericIcon) obj;
        return this.width == that.width &&
               this.height == that.height &&
               this.u == that.u &&
               this.v == that.v;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, u, v);
    }

    @Override
    public String toString() {
        return "GenericIcon[" +
               "width=" + width + ", " +
               "height=" + height + ", " +
               "u=" + u + ", " +
               "v=" + v + ']';
    }

}
