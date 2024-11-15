package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.IntegratedServer;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.Screenshot;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.settings.SettingsScreen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.Panel;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public class PauseScreen extends Screen {
    private Label gamePausedLabel;
    private TextButton backToGameButton;
    private TextButton optionsButton;
    private TextButton exitWorldButton;
    private Panel panel;
    private Screenshot screenshot;

    public PauseScreen() {
        super("Game Paused");
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        IntegratedServer server = this.client.getSingleplayerServer();
        ServerWorld world = server != null ? server.getWorld() : null;
        if (world != null)
            this.client.addFuture(world.saveAsync(false));

        this.panel = builder.add(Panel.create()
                        .bounds(() -> new Bounds(10, 10, 130, this.size.height - 20)));

        this.gamePausedLabel = builder.add(Label.of(TextObject.translation("quantum.ui.gamePaused"))
                        .bounds(() -> new Bounds(15, this.size.height - 100, 120, 21)));

        this.backToGameButton = builder.add(TextButton.of(TextObject.translation("quantum.ui.backToGame"))
                        .bounds(() -> new Bounds(15, this.size.height - 100, 120, 21)))
                .setCallback(this::resumeGame);

        this.optionsButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.options"), 95)
                        .bounds(() -> new Bounds(15, this.size.height - 75, 120, 21)))
                .setCallback(caller -> QuantumClient.get().showScreen(new SettingsScreen()));

        this.exitWorldButton = builder.add(TextButton.of(TextObject.translation("quantum.ui.exitWorld"), 95)
                        .bounds(() -> new Bounds(15, this.size.height - 36, 120, 21)))
                .setCallback(this::exitWorld);
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

    public Panel getPanel() {
        return panel;
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
