package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.settings.SettingsScreen;
import dev.ultreon.quantum.client.gui.screens.world.WorldSelectionScreen;
import dev.ultreon.quantum.client.gui.widget.Rectangle;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.gui.widget.TitleButton;
import dev.ultreon.quantum.client.rpc.GameActivity;
import dev.ultreon.quantum.client.util.Resizer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.util.Vec2f;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TitleScreen extends Screen {
    private TitleButton singleplayerButton;
    private TitleButton multiplayerButton;
    private TitleButton modListButton;
    private TitleButton optionsButton;
    private TitleButton quitButton;
    private final Resizer resizer;
    private @Nullable TextButton worldGenTestButton;

    public TitleScreen() {
        super((TextObject) null, null);

        this.resizer = new Resizer(7680, 4320);
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        this.client.setActivity(GameActivity.MAIN_MENU);

        if (!GamePlatform.get().hasBackPanelRemoved())
            builder.add(Rectangle.create().bounds(() -> new Bounds(0, 0, this.size.width, this.size.height)).backgroundColor(RgbColor.rgba(0, 0, 0, .4f)));

        this.singleplayerButton = builder.add(TitleButton.of(TextObject.translation("quantum.screen.title.singleplayer"), 100)
                        .icon(QuantumClient.id("textures/gui/title/singleplayer.png"))
                .bounds(() -> new Bounds(this.size.width / 2 - 50 - 10 - 100 - 10 - 100, 2 * this.size.height / 3 - 100, 100, 150))
                .setCallback(this::openSingleplayer));

        if (GamePlatform.get().isDevEnvironment()) {
            this.worldGenTestButton = builder.add(TextButton.of(TextObject.literal("WORLD-GEN TEST"), 100)
                    .position(() -> new Position(this.size.width / 2 - 50 - 10 - 100 - 10 - 100, 2 * this.size.height / 3 - 125))
                    .setCallback(this::openTest));
        }

        this.multiplayerButton = builder.add(TitleButton.of(TextObject.translation("quantum.screen.multiplayer"), 100)
                        .icon(QuantumClient.id("textures/gui/title/multiplayer.png"))
                .bounds(() -> new Bounds(this.size.width / 2 - 50 - 10 - 100, 2 * this.size.height / 3 - 100, 100, 150))
                .setCallback(this::openMultiplayer));

        this.modListButton = builder.add(TitleButton.of(TextObject.translation("quantum.screen.mod_list"), 100)
                        .icon(QuantumClient.id("textures/gui/title/mods.png"))
                .bounds(() -> new Bounds(this.size.width / 2 - 50, 2 * this.size.height / 3 - 100, 100, 150))
                .setCallback(this::showModList));

        this.optionsButton = builder.add(TitleButton.of(TextObject.translation("quantum.screen.options"), 100)
                        .icon(QuantumClient.id("textures/gui/title/options.png"))
                .bounds(() -> new Bounds(this.size.width / 2 + 10 + 50, 2 * this.size.height / 3 - 100, 100, 150))
                .setCallback(this::showOptions));

        this.quitButton = builder.add(TitleButton.of(TextObject.translation("quantum.screen.title.quit"), 100)
                        .icon(QuantumClient.id("textures/gui/title/quit.png"))
                .bounds(() -> new Bounds(this.size.width / 2 + 10 + 50 + 10 + 100, 2 * this.size.height / 3 - 100, 100, 150))
                .setCallback(this::quitGame));

        if (GamePlatform.get().isMobile()) {
            this.quitButton.isEnabled = false;
        }
    }

    private void openTest(TextButton textButton) {
        this.client.showScreen(new WorldGenTestScreen());
    }

    private void quitGame(TitleButton caller) {
        this.client.tryShutdown();
    }

    private void openSingleplayer(TitleButton caller) {
        this.client.showScreen(new WorldSelectionScreen());
    }

    private void openMultiplayer(TitleButton caller) {
        this.client.showScreen(new MultiplayerScreen());
    }

    private void showOptions(TitleButton caller) {
        this.client.showScreen(new SettingsScreen());
    }

    private void showModList(TitleButton caller) {
        this.client.showScreen(new ModListScreen(this));
    }

    @Override
    protected void renderSolidBackground(Renderer renderer) {
        if (!GamePlatform.get().hasBackPanelRemoved()) {
            Vec2f thumbnail = this.resizer.thumbnail(this.size.width, this.size.height);

            float drawWidth = thumbnail.x;
            float drawHeight = thumbnail.y;

            float drawX = (this.size.width - drawWidth) / 2;
            float drawY = (this.size.height - drawHeight) / 2;

            renderer.blit(QuantumClient.id("textures/gui/title_background.png"), (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, this.resizer.getSourceWidth(), this.resizer.getSourceHeight(), (int) this.resizer.getSourceWidth(), (int) this.resizer.getSourceHeight());
        }
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        renderer.blit(QuantumClient.id("textures/gui/quantum_voxel.png"), this.size.width / 2f - 878 / 6f, Math.max(2 * this.size.height / 3f - 301 / 6f - 200, 10), 878 / 3f, 301 / 3f, 0, 0, 100, 150, 100, 150);
    }

    public TitleButton getSingleplayerButton() {
        return this.singleplayerButton;
    }

    public @Nullable TextButton getWorldGenTestButton() {
        return worldGenTestButton;
    }

    public TitleButton getMultiplayerButton() {
        return this.multiplayerButton;
    }

    public TitleButton getModListButton() {
        return this.modListButton;
    }

    public TitleButton getOptionsButton() {
        return this.optionsButton;
    }

    public TitleButton getQuitButton() {
        return this.quitButton;
    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public boolean onClose(Screen next) {
        return !(next instanceof TitleScreen);
    }
}
