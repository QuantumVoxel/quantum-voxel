package dev.ultreon.quantum.client.gui.screens.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.WorldSaveInfo;
import dev.ultreon.quantum.world.WorldStorage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
    private Rectangle shadowFar;
    private Rectangle shadowNear;
    private Platform buttonPlatform;
    private WorldInfoWidget worldInfo;

    public WorldSelectionScreen() {
        super(TextObject.translation("quantum.screen.world_selection.title"));
    }

    @Override
    public TitleRenderMode titleRenderMode() {
        return TitleRenderMode.First;
    }

    @Override
    public void init() {
        this.worldList = add(new SelectionList<WorldStorage>())
                .addEntries(this.locateWorlds())
                .withSelectable(true)
                .withCallback(this::selectWorld)
                .withItemRenderer(this::renderWorldItem)
                .withItemHeight(60)
                .withDrawBackground(true)
                .withDrawButtons(false)
                .withCutButtons(false);

        this.worldInfo = add(new WorldInfoWidget(worldList));

        shadowFar = add(Rectangle.create())
                .withBackgroundColor(RgbColor.BLACK.withAlpha(0x40));

        shadowNear = add(Rectangle.create())
                .withBackgroundColor(RgbColor.BLACK.withAlpha(0x40));

        buttonPlatform = add(Platform.create());

        createButton = add(TextButton.of(TextObject.translation("quantum.screen.world_selection.create"), 150)
                .withCallback(this::createWorld)
                .withType(Button.Type.DARK_EMBED));

        playButton = add(TextButton.of(TextObject.translation("quantum.screen.world_selection.play"), 150)
                .withCallback(this::playWorld)
                .withType(Button.Type.DARK_EMBED));

        deleteWorld = add(TextButton.of(TextObject.translation("quantum.screen.world_selection.delete"), 150)
                .withCallback(this::deleteWorld)
                .withType(Button.Type.DARK_EMBED));
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        worldList.setBounds(pos.x, pos.y - 3, 250, size.height - 35);
        worldInfo.setBounds(pos.x + 250, pos.y - 1, size.width-250, size.height - 37);
        shadowFar.setBounds(2, size.height - 37, size.width - 4, 46);
        shadowNear.setBounds(1, size.height - 36, size.width - 2, 44);

        buttonPlatform.setBounds(-5, size.height - 38, size.width + 10, 46);

        createButton.setPos(this.getWidth() / 2 - 227, this.getHeight() - 31);
        playButton.setPos(this.getWidth() / 2 - 75, this.getHeight() - 31);
        deleteWorld.setPos(this.getWidth() / 2 + 77, this.getHeight() - 31);
    }

    private void renderWorldItem(Renderer renderer, WorldStorage worldStorage, int y, boolean selected, float delta) {
        if (selected) {
            renderer.drawHighlightPlatform(-2, y - 1, size.width+4, 60, 2);
        } else {
            renderer.drawPlatform(-2, y - 3, size.width+4, 60, 4);
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
                        worldList.addEntries(this.locateWorlds());
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

    private static class WorldInfoWidget extends Widget {
        private final SelectionList<WorldStorage> worldList;
        private FileHandle currentPicFile;
        private Texture currentPic;
        private WorldSaveInfo info;
        private String lastSave;

        public WorldInfoWidget(SelectionList<WorldStorage> worldList) {
            super(0, 0);
            this.worldList = worldList;
        }

        @Override
        public void renderWidget(Renderer renderer, float deltaTime) {
            super.renderWidget(renderer, deltaTime);

            renderer.drawPlatform(pos.x, pos.y, size.width, size.height);

            WorldStorage selected = worldList.getSelected();
            if (selected == null) return;
            FileHandle child = selected.getDirectory().child("picture.png");
            if (!Objects.equals(child, currentPicFile)) {
                if (currentPic != null) {
                    currentPic = null;
                }
                currentPic = new Texture(child);
                currentPicFile = child;
                info = selected.loadInfo();
                lastSave = info.lastSave().toLocalDateTime().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG));
            }
            int scaledHeight = (size.width - 4) * currentPic.getHeight() / currentPic.getWidth();
            int displayHeight = Math.min(scaledHeight, size.height / 2);

            // Compute the source height we need to extract from the image
            int cropSourceHeight = (int)((float)displayHeight * currentPic.getHeight() / scaledHeight);

            // Center crop vertically in source image
            int sourceY = (currentPic.getHeight() - cropSourceHeight) / 2;

            renderer.blit(
                    currentPic,
                    pos.x + 2, pos.y,
                    size.width, displayHeight,
                    0, sourceY,
                    currentPic.getWidth(), cropSourceHeight,
                    currentPic.getWidth(), currentPic.getHeight()
            );

            renderer.textLeft(selected.getName(), 2, pos.x + 10, pos.y + displayHeight + 12);
            renderer.textLeft("[gray]" + selected.getMD5Name(), pos.x + 10, pos.y + displayHeight + 32);
            renderer.textLeft("[gold]Seed: [white]" + info.seed(), pos.x + 10, pos.y + displayHeight + 52);
            renderer.textLeft("[gold]Gamemode: [white]" + info.gamemode(), pos.x + 10, pos.y + displayHeight + 62);
            renderer.textLeft("[gold]Last Gamemode: [white]" + info.lastPlayedInMode(), pos.x + 10, pos.y + displayHeight + 72);
            renderer.textLeft("[gold]Last Saved: [white]" + lastSave, pos.x + 10, pos.y + displayHeight + 82);
            renderer.textLeft("[gold]Generator: [white]v" + info.generatorVersion(), pos.x + 10, pos.y + displayHeight + 92);
        }

        public void dispose() {

        }
    }
}
