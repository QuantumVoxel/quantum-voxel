package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Panel;
import dev.ultreon.quantum.client.gui.widget.PlatformTest;
import dev.ultreon.quantum.client.gui.widget.TextButton;

public class UITestScreen extends Screen {
    protected UITestScreen() {
        super("UI Test");
    }

    @Override
    protected void init() {
        super.init();

        add(Panel.create()).bounds(10, 10, 20, 20);
        add(PlatformTest.create()).bounds(40, 10, 20, 20);

        add(TextButton.of("Back", 50))
                .setCallback(button -> back())
                .bounds(70, 10, 50, 20);

        add(TextButton.of("Close", 50))
                .setCallback(button -> close())
                .bounds(130, 10, 50, 20);
    }
}
