package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.SelectionList;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Color;
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
    private SelectionList<WorldStorage> worldList;
    private WorldStorage selected;
    private TextButton createButton;
    private TextButton playButton;
    private TextButton deleteWorld;

    public WorldSelectionScreen() {
        super(TextObject.translation("quantum.screen.world_selection.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        this.worldList = builder.add(new SelectionList<WorldStorage>()
                .itemHeight(50)
                .bounds(() -> new Bounds(0, 0, this.getWidth(), this.getHeight() - 41))
                .entries(this.locateWorlds())
                .itemRenderer(this::renderItem)
                .selectable(true)
                .callback(this::selectWorld));

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

    private void renderItem(Renderer renderer, WorldStorage storage, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        int x = this.worldList.getX() + this.worldList.getWidth() / 2 - 200;
        renderer.textLeft(TextObject.literal(storage.getDirectory().getFileName().toString()).setBold(true), x, y + 5, Color.WHITE);
        storage.loadInfo();
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

    public SelectionList<WorldStorage> getWorldList() {
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
