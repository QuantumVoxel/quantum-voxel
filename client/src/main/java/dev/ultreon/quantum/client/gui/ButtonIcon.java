package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.gui.icon.Icon;
import dev.ultreon.quantum.util.NamespaceID;

public enum ButtonIcon implements Icon {
    Edit(0,0),
    ;
    private static final NamespaceID ID = NamespaceID.of("textures/gui/btn_icons.png");
    private final int u;
    private final int v;

    ButtonIcon(int u, int v) {
        this.u = u;
        this.v = v;
    }

    @Override
    public NamespaceID id() {
        return ID;
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
    public int u() {
        return u * 16;
    }

    @Override
    public int v() {
        return v;
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
