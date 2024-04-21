package com.ultreon.quantum.client.gui.screens;

import com.ultreon.quantum.client.gui.Bounds;
import com.ultreon.quantum.client.gui.GuiBuilder;
import com.ultreon.quantum.client.gui.widget.WorldGenTestPanel;

public class WorldGenTestScreen extends Screen {
    protected WorldGenTestScreen() {
        super("WorldGenTestScreen");
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(WorldGenTestPanel.create().bounds(() -> new Bounds(0, 0, this.size.width, this.size.height)));
    }
}
