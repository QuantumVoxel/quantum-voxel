package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.gui.icon.Icon;
import dev.ultreon.quantum.util.Identifier;

public enum ControlIcon implements Icon {
    Close(0, 0),
    Maximize(1, 0),
    Minimize(2, 0),
    ;

    private final int u;
    private final int v;

    ControlIcon(int u, int v) {
        this.u = u * 7;
        this.v = v * 7;
    }

    @Override
    public Identifier id() {
        return new Identifier("textures/gui/control_icons.png");
    }

    @Override
    public int width() {
        return 7;
    }

    @Override
    public int height() {
        return 7;
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
    public int texWidth() {
        return 64;
    }

    @Override
    public int texHeight() {
        return 64;
    }
}
