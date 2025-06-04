package dev.ultreon.quantum.client.gui.screens.test;

import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Platform;
import dev.ultreon.quantum.client.gui.widget.PlatformTest;
import dev.ultreon.quantum.client.gui.widget.TextButton;

public class UITestScreen extends Screen {
    public UITestScreen() {
        super("UI Test");
    }

    @Override
    public TitleRenderMode titleRenderMode() {
        return TitleRenderMode.First;
    }

    @Override
    protected void init() {
        super.init();

        add(Platform.create()).withBounding(10, pos.y+10, 20, 20);
        add(PlatformTest.create()).withBounding(40, pos.y+10, 20, 20);

        add(TextButton.of("Back", 50))
                .withCallback(button -> back())
                .withBounding(70, pos.y+10, 50, 20);

        add(TextButton.of("Close", 50))
                .withCallback(button -> close())
                .withBounding(130, pos.y+10, 50, 20);
    }
}
