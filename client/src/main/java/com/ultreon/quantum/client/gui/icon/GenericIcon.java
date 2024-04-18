package com.ultreon.quantum.client.gui.icon;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.util.Identifier;

public record GenericIcon(int width, int height, int u, int v) implements Icon {
    public static final GenericIcon LOCKED = new GenericIcon(16, 16, 0, 0);
    public static final GenericIcon UNLOCKED = new GenericIcon(16, 16, 16, 0);
    public static final GenericIcon RELOAD = new GenericIcon(16, 16, 0, 16);

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
}
