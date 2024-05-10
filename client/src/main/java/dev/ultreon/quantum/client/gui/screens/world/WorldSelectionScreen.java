package dev.ultreon.quantum.client.gui.screens.world;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.screens.Screen;
import dev.ultreon.quantum.client.gui.screens.WorldCreationScreen;
import dev.ultreon.quantum.client.gui.screens.WorldDeleteConfirmScreen;
import dev.ultreon.quantum.client.gui.widget.Rectangle;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.gui.widget.WorldCardList;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.WorldSaveInfo;
import dev.ultreon.quantum.world.WorldStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldSelectionScreen extends Screen {
    public static final Path WORLDS_DIR = Paths.get("worlds");
    private static final int ENTRY_WIDTH = 200;
    private static final int ENTRY_HEIGHT = 300;
    private WorldCardList worldList;
    private WorldStorage selected;
    private TextButton createButton;
    private TextButton playButton;
    private TextButton deleteWorld;
    private TextButton prevButton;
    private TextButton nextButton;

    public WorldSelectionScreen() {
        super(TextObject.translation("quantum.screen.world_selection.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        this.worldList = builder.add(WorldCardList.create(() -> calculateMaxEntries(this.size.width)))
                .worlds(this.locateWorlds())
                .selectable(true)
                .xOffset(60)
                .callback(this::selectWorld)
                .count(() -> calculateMaxEntries(size.width))
                .bounds(() -> new Bounds(0, 0, this.getWidth(), this.getHeight() - 41));

        builder.add(Rectangle.create().bounds(() -> new Bounds(0, 0, 60, this.getHeight() - 41)).backgroundColor(RgbColor.rgba(0, 0, 0, .2f)));
        builder.add(Rectangle.create().bounds(() -> new Bounds(this.getWidth() - 60, 0, 60, this.getHeight() - 41)).backgroundColor(RgbColor.rgba(0, 0, 0, .2f)));

        this.prevButton = builder.add(TextButton.of(TextObject.literal("<"), 150)
                .bounds(() -> new Bounds(20, this.getHeight() / 2 - 15, 20, 30))
                .callback(caller -> scrollLeft()));

        this.nextButton = builder.add(TextButton.of(TextObject.literal(">"), 150)
                .bounds(() -> new Bounds(this.getWidth() - 40, this.getHeight() / 2 - 15, 20, 30))
                .callback(caller -> scrollRight()));

        this.createButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_selection.create"), 150)
                .position(() -> new Position(this.getWidth() / 2 - 227, this.getHeight() - 31))
                .callback(this::createWorld));

        this.playButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_selection.play"), 150)
                .position(() -> new Position(this.getWidth() / 2 - 75, this.getHeight() - 31))
                .callback(this::playWorld));

        this.deleteWorld = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_selection.delete"), 150)
                .position(() -> new Position(this.getWidth() / 2 + 77, this.getHeight() - 31))
                .callback(this::deleteWorld));
    }

    private void selectWorld(WorldCardList.Entry entry) {
        this.selectWorld(entry.getWorld());
    }

    private void scrollLeft() {
        this.worldList.scrollDelta(-1);
    }

    private void scrollRight() {
        this.worldList.scrollDelta(1);
    }

    private int calculateMaxEntries(int width) {
        return (width - 80) / ENTRY_WIDTH;
    }

    private void deleteWorld(TextButton caller) {
        if (this.selected == null) return;

        this.client.showScreen(new WorldDeleteConfirmScreen(this.selected));
    }

    private void playWorld(TextButton t) {
        if (this.selected == null) return;

        WorldStorage selected = this.selected;
        this.client.startWorld(selected);

    }

    private void createWorld(TextButton caller) {
        this.client.showScreen(new WorldCreationScreen());
    }

    private void selectWorld(WorldStorage storage) {
        this.selected = storage;
    }

    private void renderItem(Renderer renderer, WorldStorage storage, int x, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        renderer.renderFrame(x, y, ENTRY_WIDTH, ENTRY_HEIGHT);

        WorldSaveInfo worldSaveInfo = storage.loadInfo();

        if (renderer.pushScissors(x + 2, y + 2, ENTRY_WIDTH - 4, ENTRY_HEIGHT - 4)) {
            renderer.textCenter("<bold>" + worldSaveInfo.name(), x + ENTRY_WIDTH / 2, y + 5, 0xffffff);
            renderer.textCenter("<gray><bold>" + worldSaveInfo.lastPlayedInMode(), x + ENTRY_WIDTH / 2, y + 15, 0xa0a0a0);
            renderer.popScissors();
        }
    }

    public List<WorldStorage> locateWorlds() {
        var worlds = new ArrayList<WorldStorage>();
        try (Stream<Path> worldPaths = Files.list(WorldSelectionScreen.WORLDS_DIR)) {
            worlds = worldPaths.map(WorldStorage::new).sorted(Comparator.comparing(o -> o.getDirectory().getFileName().toString())).collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ignored) {
            // ignored
        }

        return worlds;
    }

    public WorldCardList getWorldList() {
        return this.worldList;
    }

    public WorldStorage getSelected() {
        return this.selected;
    }

    public TextButton getCreateButton() {
        return this.createButton;
    }

    public TextButton getDeleteWorld() {
        return this.deleteWorld;
    }

    public TextButton getPlayButton() {
        return this.playButton;
    }
}
