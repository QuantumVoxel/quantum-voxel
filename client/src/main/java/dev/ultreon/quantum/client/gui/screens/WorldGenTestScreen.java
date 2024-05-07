package dev.ultreon.quantum.client.gui.screens;

import com.badlogic.gdx.Input;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.widget.WorldGenTestPanel;

public class WorldGenTestScreen extends Screen {
    private WorldGenTestPanel panel;

    protected WorldGenTestScreen() {
        super("WorldGenTestScreen");
    }

    @Override
    public void build(GuiBuilder builder) {
        focused = this.panel = (WorldGenTestPanel) builder.add(WorldGenTestPanel.create().bounds(() -> new Bounds(0, 0, this.size.width, this.size.height)));
        focused.focused = true;
    }

    @Override
    public boolean keyRelease(int keyCode) {
        if (keyCode == Input.Keys.SPACE) {
            this.panel.random();
            return true;
        }

        return super.keyRelease(keyCode);
    }
}
