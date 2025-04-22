package dev.ultreon.quantum.client.gui.screens.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.WorldCreationScreen;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.WorldSaveInfo;
import dev.ultreon.quantum.world.WorldStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class WorldSelectionScreen extends Screen {
    public static final FileHandle WORLDS_DIR = Gdx.files.local("worlds");
    private SelectionList<WorldStorage> worldList;
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
    public void build(@NotNull GuiBuilder builder) {
        this.worldList = builder.add(new SelectionList<WorldStorage>())
                .entries(this.locateWorlds())
                .selectable(true)
                .callback(this::selectWorld)
                .itemRenderer(this::renderWorldItem)
                .itemHeight(60)
                .drawBackground(true)
                .drawButtons(false)
                .cutButtons(false)
                .bounds(() -> new Bounds(0, 0, this.getWidth(), this.getHeight() - 38));

        builder.add(Rectangle.create())
                .bounds(() -> new Bounds(2, this.getHeight() - 40, this.getWidth() - 4, 46))
                .backgroundColor(RgbColor.BLACK.withAlpha(0x40));

        builder.add(Rectangle.create())
                .bounds(() -> new Bounds(1, this.getHeight() - 39, this.getWidth() - 2, 44))
                .backgroundColor(RgbColor.BLACK.withAlpha(0x40));

        builder.add(Panel.create()
                .bounds(() -> new Bounds(-5, this.getHeight() - 41, this.getWidth() + 10, 46)));

        this.createButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_selection.create"), 150)
                .position(() -> new Position(this.getWidth() / 2 - 227, this.getHeight() - 31))
                .setCallback(this::createWorld)
                .setType(Button.Type.DARK_EMBED));

        this.playButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_selection.play"), 150)
                .position(() -> new Position(this.getWidth() / 2 - 75, this.getHeight() - 31))
                .setCallback(this::playWorld)
                .setType(Button.Type.DARK_EMBED));

        this.deleteWorld = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_selection.delete"), 150)
                .position(() -> new Position(this.getWidth() / 2 + 77, this.getHeight() - 31))
                .setCallback(this::deleteWorld)
                .setType(Button.Type.DARK_EMBED));
    }

    private void renderWorldItem(Renderer renderer, WorldStorage worldStorage, int y, boolean selected, float delta) {
        if (selected) {
            renderer.drawHighlightPlatform(0, y - 1, this.getWidth(), 60, 2);
        } else {
            renderer.drawPlatform(0, y - 3, this.getWidth(), 60, 4);
        }

        renderer.setColor(RgbColor.WHITE);
        renderer.textLeft(worldStorage.getName(), 10, y + 10);

        WorldSaveInfo worldSaveInfo = worldStorage.loadInfo();
        if (worldSaveInfo == null) {
            renderer.textLeft(TextObject.translation("quantum.screen.world_selection.unsaved"), 10, y + 30, RgbColor.rgb(0xa0a0a0));
            return;
        }
        String lowerCase = worldSaveInfo.gamemode().name().toLowerCase(Locale.ROOT);
        renderer.textLeft(lowerCase + " - " + LocalDateTime.ofEpochSecond(worldSaveInfo.lastSave().toEpochSecond(), 0, ZoneOffset.UTC).format(CommonConstants.DATE_FORMAT) + " - Gen " + worldSaveInfo.generatorVersion(), 10, y + 30, RgbColor.rgb(0x808080));
    }

    private void deleteWorld(TextButton caller) {
        if (this.selected == null) return;

        this.showDialog(new DialogBuilder(this)
                .title(TextObject.translation("quantum.dialog.delete_world.title"))
                .message(TextObject.translation("quantum.dialog.delete_world.message"))
                .button(TextObject.translation("quantum.ui.yes"), () -> {
                    try {
                        selected.delete();
                        worldList.clear();
                        worldList.entries(this.locateWorlds());
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
        FileHandle[] worldPaths = WorldSelectionScreen.WORLDS_DIR.list();
        if (worldPaths != null) {
            worlds = Arrays.stream(worldPaths).map(WorldStorage::new).sorted(Comparator.comparing(o -> o.getDirectory().name())).collect(Collectors.toCollection(ArrayList::new));
            worlds.sort((o1, o2) -> {
                if (!o1.exists("info.ubo") && !o2.exists("info.ubo")) {
                    long millis1 = o1.getDirectory().lastModified();
                    long millis2 = o2.getDirectory().lastModified();
                    return Long.compare(millis2, millis1);
                }
                if (!o1.exists("info.ubo")) return 1;
                if (!o2.exists("info.ubo")) return -1;

                long millis1 = o1.loadInfo().lastSave().toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli();
                long millis2 = o2.loadInfo().lastSave().toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli();
                return Long.compare(millis2, millis1);
            });
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
