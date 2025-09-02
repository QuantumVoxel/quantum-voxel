package dev.ultreon.quantum.client.gui.icon;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Objects;

public final class MessageIcon implements Icon {
    public static final MessageIcon WARNING = new MessageIcon(16, 16, 0, 0);
    public static final MessageIcon ERROR = new MessageIcon(16, 16, 16, 0);
    public static final MessageIcon INFO = new MessageIcon(16, 16, 32, 0);
    public static final MessageIcon DANGER = new MessageIcon(16, 16, 48, 0);
    public static final MessageIcon QUESTION = new MessageIcon(16, 16, 64, 0);
    private final int width;
    private final int height;
    private final int u;
    private final int v;

    public MessageIcon(int width, int height, int u, int v) {
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
    }

    @Override
    public NamespaceID id() {
        return QuantumClient.id("textures/gui/icons/message.png");
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
        var that = (MessageIcon) obj;
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
        return "MessageIcon[" +
               "width=" + width + ", " +
               "height=" + height + ", " +
               "u=" + u + ", " +
               "v=" + v + ']';
    }

}
