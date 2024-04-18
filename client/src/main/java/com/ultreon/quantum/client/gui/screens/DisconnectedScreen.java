package com.ultreon.quantum.client.gui.screens;

import com.ultreon.quantum.client.gui.GuiBuilder;
import com.ultreon.quantum.client.gui.Position;
import com.ultreon.quantum.client.gui.Renderer;
import com.ultreon.quantum.client.gui.widget.TextButton;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.util.Color;
import org.jetbrains.annotations.NotNull;

public class DisconnectedScreen extends Screen {
    private final String message;
    private boolean wasMultiplayer;

    public DisconnectedScreen(String message, boolean wasMultiplayer) {
        super(TextObject.translation("quantum.screen.message.disconnected"));
        this.message = message;
        this.wasMultiplayer = wasMultiplayer;
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(TextButton.of(TextObject.translation("quantum.ui.exitWorld"), 150)
                .position(() -> new Position(this.size.width / 2 - 75, this.size.height / 2 - 10))
                .callback(caller -> this.client.showScreen(wasMultiplayer ? new MultiplayerScreen() : new TitleScreen())));
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);
        renderer.textCenter(this.title, 2, this.size.width / 2, this.size.height / 3, Color.WHITE);

        int lineY = 0;
        for (String line : this.message.lines().toList()) {
            renderer.textCenter(line, this.size.width / 2, this.size.height / 3 + 30 + lineY * (this.font.lineHeight + 1) - 1, Color.WHITE, false);
            lineY++;
        }
    }
}
