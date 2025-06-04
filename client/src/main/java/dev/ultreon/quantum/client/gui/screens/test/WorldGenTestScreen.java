package dev.ultreon.quantum.client.gui.screens.test;

import com.badlogic.gdx.Input;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.WorldGenTestPanel;
import org.jetbrains.annotations.NotNull;

public class WorldGenTestScreen extends Screen {
    private WorldGenTestPanel panel;

    public WorldGenTestScreen() {
        super("WorldGenTestScreen");
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        focused = this.panel = (WorldGenTestPanel) builder.add(WorldGenTestPanel.create().withBounding(() -> new Bounds(0, 0, this.size.width, this.size.height)));
        focused.isFocused = true;
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
