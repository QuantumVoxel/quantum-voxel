package dev.ultreon.quantum.client.gui.icon;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.Identifier;

public record MessageIcon(int width, int height, int u, int v) implements Icon {
    public static final MessageIcon WARNING = new MessageIcon(16, 16, 0, 0);
    public static final MessageIcon ERROR = new MessageIcon(16, 16, 16, 0);
    public static final MessageIcon INFO = new MessageIcon(16, 16, 32, 0);
    public static final MessageIcon DANGER = new MessageIcon(16, 16, 48, 0);
    public static final MessageIcon QUESTION = new MessageIcon(16, 16, 64, 0);

    @Override
    public Identifier id() {
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
}
