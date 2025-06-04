package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.IntegratedServer;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.Screenshot;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.settings.SettingsScreen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.Platform;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PauseScreen extends Screen {
    private Label gamePausedLabel;
    private TextButton backToGameButton;
    private TextButton optionsButton;
    private TextButton exitWorldButton;
    private Platform platform;
    private Screenshot screenshot;

    public PauseScreen() {
        super("Game Paused");
    }

    @Override
    protected void init() {
        super.init();

        IntegratedServer integratedServer = client.integratedServer;
        if (integratedServer != null) {
            client.saving = true;

            QuantumServer.invoke(() -> {
                try {
                    integratedServer.save(false);
                } catch (IOException e) {
                    CommonConstants.LOGGER.error("ERROR", e);
                    throw new RuntimeException(e);
                } finally {
                    client.saving = false;
                }
            }).exceptionally(throwable -> {
                CommonConstants.LOGGER.error("ATHROW", throwable);
                client.notifications.add("Saving Failed", throwable.getMessage(), "Failed to save world");
                return null;
            });
        }
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        this.platform = builder.add(Platform.create()
                        .withBounding(() -> new Bounds(10, 10, 130, this.size.height - 20)));

        this.gamePausedLabel = builder.add(Label.of(TextObject.translation("quantum.ui.gamePaused"))
                        .withBounding(() -> new Bounds(15, this.size.height - 100, 120, 21)));

        this.backToGameButton = builder.add(TextButton.of(TextObject.translation("quantum.ui.backToGame"))
                        .withBounding(() -> new Bounds(15, this.size.height - 100, 120, 21)))
                .withCallback(this::resumeGame);

        this.optionsButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.options"), 95)
                        .withBounding(() -> new Bounds(15, this.size.height - 75, 120, 21)))
                .withCallback(caller -> QuantumClient.get().showScreen(new SettingsScreen()));

        this.exitWorldButton = builder.add(TextButton.of(TextObject.translation("quantum.ui.exitWorld"), 95)
                        .withBounding(() -> new Bounds(15, this.size.height - 36, 120, 21)))
                .withCallback(this::exitWorld);
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        if (QuantumClient.get().saving) {
            renderer.textRight(TextObject.translation("quantum.ui.saving_world"), size.width - 20, size.height - 20);
        }
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }

    public TextButton getBackToGameButton() {
        return this.backToGameButton;
    }

    public TextButton getOptionsButton() {
        return this.optionsButton;
    }

    public TextButton getExitWorldButton() {
        return this.exitWorldButton;
    }

    private void exitWorld(TextButton caller) {
        this.client.exitWorldToTitle();
    }

    private void resumeGame(TextButton caller) {
        this.client.resume();
    }

    public Platform getPanel() {
        return platform;
    }

    public Label getGamePausedLabel() {
        return gamePausedLabel;
    }

    public void setScreenshot(Screenshot screenshot) {
        this.screenshot = screenshot;
    }

    public Screenshot getScreenshot() {
        return this.screenshot;
    }
}
