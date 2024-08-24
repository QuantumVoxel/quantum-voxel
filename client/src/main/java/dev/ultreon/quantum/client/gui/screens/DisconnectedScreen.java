package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.libs.commons.v0.util.StringUtils;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DisconnectedScreen extends Screen {
    private final String message;
    private final boolean wasMultiplayer;

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

        try {
            // Make sure the connection is closed
            this.client.connection.close();
            this.client.connection = null;
        } catch (IOException|NullPointerException ignored) {
            this.client.connection = null;
        }

        if (this.client.worldRenderer != null) this.client.worldRenderer.dispose();
        if (this.client.world != null) this.client.world.dispose();
        this.client.player = null;
        this.client.worldRenderer = null;
        this.client.world = null;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);
        renderer.textCenter(this.title, 2, this.size.width / 2, this.size.height / 3, RgbColor.WHITE);

        int lineY = 0;
        for (String line : StringUtils.splitIntoLines(this.message)) {
            renderer.textCenter(line, this.size.width / 2, this.size.height / 3 + 30 + lineY * (this.font.lineHeight + 1) - 1, RgbColor.WHITE, false);
            lineY++;
        }
    }
}
