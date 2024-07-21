package dev.ultreon.quantum.client.gui.screens.world;

import com.badlogic.gdx.Gdx;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.*;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.WorldStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldSelectionScreen extends Screen {
    public static final Path WORLDS_DIR = QuantumClient.data("worlds").file().toPath();
    private static final int ENTRY_WIDTH = 200;
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
                .bounds(() -> new Bounds(20, this.getHeight() / 2 - (this.getHeight() / 2 - 40) - 20, 20, this.getHeight() - 80))
                .callback(caller -> scrollLeft()));

        this.nextButton = builder.add(TextButton.of(TextObject.literal(">"), 150)
                .bounds(() -> new Bounds(this.getWidth() - 40, this.getHeight() / 2 - (this.getHeight() / 2 - 40) - 20, 20, this.getHeight() - 80))
                .callback(caller -> scrollRight()));

        builder.add(Panel.create()
                .bounds(() -> new Bounds(-5, this.getHeight() - 41, this.getWidth() + 10, 46)));

        this.createButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_selection.create"), 150)
                .position(() -> new Position(this.getWidth() / 2 - 227, this.getHeight() - 31))
                .callback(this::createWorld)
                .type(Button.Type.DARK_EMBED));

        this.playButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_selection.play"), 150)
                .position(() -> new Position(this.getWidth() / 2 - 75, this.getHeight() - 31))
                .callback(this::playWorld)
                .type(Button.Type.DARK_EMBED));

        this.deleteWorld = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_selection.delete"), 150)
                .position(() -> new Position(this.getWidth() / 2 + 77, this.getHeight() - 31))
                .callback(this::deleteWorld)
                .type(Button.Type.DARK_EMBED));
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

        this.showDialog(new DialogBuilder(this)
                .title(TextObject.translation("quantum.dialog.delete_world.title"))
                .message(TextObject.translation("quantum.dialog.delete_world.message"))
                .button(TextObject.translation("quantum.ui.yes"), () -> {
                    try {
                        selected.delete();
                    } catch (IOException e) {
                        CommonConstants.LOGGER.error("Failed to delete world", e);
                    } finally {
                        this.getDialog().close();
                    }
                })
                .button(TextObject.translation("quantum.ui.no"), () -> this.getDialog().close()));
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

    public List<WorldStorage> locateWorlds() {
        var worlds = new ArrayList<WorldStorage>();
        try (Stream<Path> worldPaths = Files.list(WorldSelectionScreen.WORLDS_DIR)) {
            worlds = worldPaths.map(WorldStorage::new).sorted(Comparator.comparing(o -> o.getDirectory().getFileName().toString())).collect(Collectors.toCollection(ArrayList::new));
            worlds.sort((o1, o2) -> {
                try {
                    if (!o1.exists("info.ubo") && !o2.exists("info.ubo")) {
                        long millis1 = Files.readAttributes(o1.getDirectory(), BasicFileAttributes.class).lastAccessTime().toMillis();
                        long millis2 = Files.readAttributes(o2.getDirectory(), BasicFileAttributes.class).lastAccessTime().toMillis();
                        return Long.compare(millis2, millis1);
                    }
                    if (!o1.exists("info.ubo")) return 1;
                    if (!o2.exists("info.ubo")) return -1;

                    long millis1 = o1.loadInfo().lastSave().toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli();
                    long millis2 = o2.loadInfo().lastSave().toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli();
                    return Long.compare(millis2, millis1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
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
