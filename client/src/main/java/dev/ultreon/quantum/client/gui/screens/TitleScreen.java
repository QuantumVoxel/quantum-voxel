package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.settings.SettingsScreen;
import dev.ultreon.quantum.client.gui.screens.test.UITestScreen;
import dev.ultreon.quantum.client.gui.screens.test.WorldGenTestScreen;
import dev.ultreon.quantum.client.gui.screens.world.WorldSelectionScreen;
import dev.ultreon.quantum.client.gui.widget.Rectangle;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.gui.widget.TitleButton;
import dev.ultreon.quantum.client.rpc.GameActivity;
import dev.ultreon.quantum.client.util.Resizer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.util.Vec2f;
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
    private @Nullable TextButton uiTestButton;

    public TitleScreen() {
        super((TextObject) null, null);

        this.resizer = new Resizer(7680, 4320);
    }

    @Override
    protected boolean isTitleEnabled() {
        return false;
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        this.client.setActivity(GameActivity.MAIN_MENU);

        if (!GamePlatform.get().hasBackPanelRemoved())
            builder.add(Rectangle.create().withBounding(() -> new Bounds(0, 0, this.size.width, this.size.height)).withBackgroundColor(RgbColor.rgba(0, 0, 0, .4f)));

        this.singleplayerButton = builder.add(TitleButton.of(TextObject.translation("quantum.screen.title.singleplayer"), 100)
                        .icon(NamespaceID.of("textures/gui/title/singleplayer.png"))
                .withBounding(() -> new Bounds(this.size.width / 2 - 50 - 10 - 100 - 10 - 100, 2 * this.size.height / 3 - 100, 100, 150))
                .withCallback(this::openSingleplayer));

        if (GamePlatform.get().isDevEnvironment()) {
            this.worldGenTestButton = builder.add(TextButton.of(TextObject.literal("WORLD-GEN TEST"), 100)
                    .withPositioning(() -> new Position(this.size.width / 2 - 50 - 10 - 100 - 10 - 100, 2 * this.size.height / 3 - 125))
                    .withCallback(this::openWorldGenTest));

            this.uiTestButton = builder.add(TextButton.of(TextObject.literal("UI TEST"), 100)
                    .withPositioning(() -> new Position(this.size.width / 2 - 50 - 10 - 100, 2 * this.size.height / 3 - 150))
                    .withCallback(this::openUITest));
        }

        this.multiplayerButton = builder.add(TitleButton.of(TextObject.translation("quantum.screen.multiplayer"), 100)
                        .icon(NamespaceID.of("textures/gui/title/multiplayer.png"))
                .withBounding(() -> new Bounds(this.size.width / 2 - 50 - 10 - 100, 2 * this.size.height / 3 - 100, 100, 150))
                .withCallback(this::openMultiplayer));

        this.modListButton = builder.add(TitleButton.of(TextObject.translation("quantum.screen.mod_list"), 100)
                        .icon(NamespaceID.of("textures/gui/title/mods.png"))
                .withBounding(() -> new Bounds(this.size.width / 2 - 50, 2 * this.size.height / 3 - 100, 100, 150))
                .withCallback(this::showModList));

        this.optionsButton = builder.add(TitleButton.of(TextObject.translation("quantum.screen.options"), 100)
                        .icon(NamespaceID.of("textures/gui/title/options.png"))
                .withBounding(() -> new Bounds(this.size.width / 2 + 10 + 50, 2 * this.size.height / 3 - 100, 100, 150))
                .withCallback(this::showOptions));

        this.quitButton = builder.add(TitleButton.of(TextObject.translation("quantum.screen.title.quit"), 100)
                        .icon(NamespaceID.of("textures/gui/title/quit.png"))
                .withBounding(() -> new Bounds(this.size.width / 2 + 10 + 50 + 10 + 100, 2 * this.size.height / 3 - 100, 100, 150))
                .withCallback(this::quitGame));

        // FIXME Mod sidebar
//        this.modSidebar = builder.add(new ModSidebar(this));

        if (GamePlatform.get().isMobile()) {
            this.quitButton.isEnabled = false;
        }
    }

    private void openWorldGenTest(TextButton textButton) {
        this.client.showScreen(new WorldGenTestScreen());
    }

    private void openUITest(TextButton textButton) {
        this.client.showScreen(new UITestScreen());
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

            renderer.blit(NamespaceID.of("textures/gui/title_background.png"), (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, this.resizer.getSourceWidth(), this.resizer.getSourceHeight(), (int) this.resizer.getSourceWidth(), (int) this.resizer.getSourceHeight());
        }
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        renderer.blit(NamespaceID.of("textures/gui/quantum_voxel.png"), this.size.width / 2f - 878 / 6f, Math.max(2 * this.size.height / 3f - 301 / 6f - 200, 10), 878 / 3f, 301 / 3f, 0, 0, 100, 150, 100, 150);
    }

    public TitleButton getSingleplayerButton() {
        return this.singleplayerButton;
    }

    public @Nullable TextButton getWorldGenTestButton() {
        return worldGenTestButton;
    }

    public @Nullable TextButton getUITestButton() {
        return uiTestButton;
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
