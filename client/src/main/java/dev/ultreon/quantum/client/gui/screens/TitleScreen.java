package dev.ultreon.quantum.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.settings.SettingsScreen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.Panel;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.rpc.GameActivity;
import dev.ultreon.quantum.client.util.Resizer;
import dev.ultreon.quantum.text.Formatter;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Color;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

public class TitleScreen extends Screen {
    private Label titleLabel;
    private TextButton singleplayerButton;
    private TextButton multiplayerButton;
    private TextButton modListButton;
    private TextButton optionsButton;
    private TextButton quitButton;
    private final Resizer resizer;
    private TextButton worldGenTestButton;

    public TitleScreen() {
        super(TextObject.translation("quantum.screen.title"));

        this.resizer = new Resizer(7680, 4320);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.client.setActivity(GameActivity.MAIN_MENU);

        builder.add(Panel.create()
                .bounds(() -> new Bounds(0, 0, 250, this.size.height))
                .backgroundColor(Color.BLACK.withAlpha(0x80)));

        this.titleLabel = builder.add(Label.of(Formatter.format("<bold>Quantum<gray><bold>Voxel")).position(() -> new Position(125, 40))
                .alignment(Alignment.CENTER)
                .scale(2));

        this.singleplayerButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.title.singleplayer"), 150)
                .position(() -> new Position(50, this.size.height / 2 - 35))
                .callback(this::openSingleplayer));

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            this.worldGenTestButton = builder.add(TextButton.of(TextObject.literal("WORLD-GEN TEST"), 150)
                    .position(() -> new Position(50, this.size.height / 2 - 60))
                    .callback(this::openTest));
        }

        this.multiplayerButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.multiplayer"), 150)
                        .position(() -> new Position(50, this.size.height / 2 - 10)))
                .callback(this::openMultiplayer);

        this.modListButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.mod_list"), 150)
                .position(() -> new Position(50, this.size.height / 2 + 15))
                .callback(this::showModList));

        this.optionsButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.options"), 150)
                .position(() -> new Position(50, this.size.height / 2 + 40))
                .callback(this::showOptions));

        this.quitButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.title.quit"), 150)
                .position(() -> new Position(50, this.size.height / 2 + 78))
                .callback(this::quitGame));
    }

    private void openTest(TextButton textButton) {
        this.client.showScreen(new WorldGenTestScreen());
    }

    private void quitGame(TextButton caller) {
        Gdx.app.exit();
    }

    private void openSingleplayer(TextButton caller) {
        this.client.showScreen(new WorldSelectionScreen());
    }

    private void openMultiplayer(TextButton caller) {
        this.client.showScreen(new MultiplayerScreen());
    }

    private void showOptions(TextButton caller) {
        this.client.showScreen(new SettingsScreen());
    }

    private void showModList(TextButton caller) {
        this.client.showScreen(new ModListScreen(this));
    }

    @Override
    protected void renderSolidBackground(Renderer renderer) {
        super.renderSolidBackground(renderer);

        Vec2f thumbnail = this.resizer.thumbnail(this.size.width, this.size.height);

        float drawWidth = thumbnail.x;
        float drawHeight = thumbnail.y;

        float drawX = (this.size.width - drawWidth) / 2;
        float drawY = (this.size.height - drawHeight) / 2;
        renderer.blit(QuantumClient.id("textures/gui/title_background.png"), (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, this.resizer.getSourceWidth(), this.resizer.getSourceHeight(), (int) this.resizer.getSourceWidth(), (int) this.resizer.getSourceHeight());

        client.newFont.drawText(renderer, "!\"#", 20, 20, Color.WHITE, true);
    }

    public TextButton getSingleplayerButton() {
        return this.singleplayerButton;
    }

    @Nullable
    public TextButton getWorldGenTestButton() {
        return worldGenTestButton;
    }

    public TextButton getMultiplayerButton() {
        return this.multiplayerButton;
    }

    public TextButton getModListButton() {
        return this.modListButton;
    }

    public TextButton getOptionsButton() {
        return this.optionsButton;
    }

    public TextButton getQuitButton() {
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

    public Label getTitleLabel() {
        return this.titleLabel;
    }
}
