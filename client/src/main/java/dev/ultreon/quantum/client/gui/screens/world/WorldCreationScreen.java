package dev.ultreon.quantum.client.gui.screens.world;

import dev.ultreon.libs.datetime.v0.DateTime;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.icon.GenericIcon;
import dev.ultreon.quantum.client.gui.icon.MessageIcon;
import dev.ultreon.quantum.client.gui.screens.UntestedAreaScreen;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.client.text.WordGenerator;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldSaveInfo;
import dev.ultreon.quantum.world.WorldStorage;
import it.unimi.dsi.fastutil.longs.LongHash;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static dev.ultreon.quantum.CommonConstants.RANDOM;

public class WorldCreationScreen extends Screen {
    private static final WordGenerator WORD_GEN = new WordGenerator(new WordGenerator.Config().minSize(4).maxSize(6).named());
    private TextEntry worldNameEntry;
    private IconButton reloadButton;
    private TextButton createButton;
    private String worldName = "";
    private long seed = RANDOM.nextLong();
    private final GameMode gameMode = GameMode.SURVIVAL;
    private Label titleLabel;
    private Platform platform;
    private TextButton cancelButton;
    private TextEntry seedEntry;

    public WorldCreationScreen() {
        super(TextObject.translation("quantum.screen.world_creation.title"));
    }

    @Override
    public void init() {
        worldName = WorldCreationScreen.WORD_GEN.generate() + " " + WorldCreationScreen.WORD_GEN.generate();
        titleLabel = add(Label.of(title)
                .withAlignment(Alignment.CENTER)
                .withScale(2));

        titleLabel.text().set(getTitle());
        titleLabel.scale().set(2);

        platform = add(Platform.create());

        worldNameEntry = add(TextEntry.create()
                .withCallback(this::updateWorldName)
                .withValue(worldName)
                .withHint(TextObject.translation("quantum.screen.world_creation.name")));

        seedEntry = add(TextEntry.create()
                .withCallback(this::setSeed)
                .withValue(String.valueOf(seed))
                .withHint(TextObject.translation("quantum.screen.world_creation.name")));

        reloadButton = add(IconButton.of(GenericIcon.RELOAD))
                .withType(Button.Type.DARK_EMBED)
                .withCallback(this::regenerateName);

        createButton = add(TextButton.of(TextObject.translation("quantum.screen.world_creation.create"), 95)
                .withCallback(caller -> {
                    if (GamePlatform.get().isWeb()) {
                        client.showScreen(new UntestedAreaScreen("World creation on web is experimental", this::createWorld));
                        return;
                    }
                    createWorld();
                }));

        cancelButton = add(TextButton.of(UITranslations.CANCEL, 95)
                .withCallback(this::onBack));
    }

    private static long murmurHash64(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        long h = 0;

        for (byte b : data) {
            h ^= b;
            h *= 0x5bd1e9955bd1e995L;
            h ^= (h >>> 47);
        }

        return h;
    }

    private void setSeed(TextEntry textEntry) {
        String value = textEntry.getValue();
        if (value.isEmpty()) {
            seed = RANDOM.nextLong();
            return;
        }
        try {
            seed = Long.parseLong(value);
        } catch (NumberFormatException e) {
            seed = murmurHash64(value);
        }
    }

    @Override
    public void resized(int width, int height) {
        titleLabel.setPos(getWidth() / 2, getHeight() / 2 - 60);
        platform.setBounds(getWidth() / 2 - 105, getHeight() / 2 - 27, 240, 85);

        worldNameEntry.setPos(getWidth() / 2 - 100, getHeight() / 2 - 20);
        seedEntry.setPos(getWidth() / 2 - 100, getHeight() / 2 + 5);
        reloadButton.setPos(getWidth() / 2 + 105, getHeight() / 2 - 24);
        createButton.setPos(getWidth() / 2 - 100, getHeight() / 2 + 30);
        cancelButton.setPos(getWidth() / 2 + 5, getHeight() / 2 + 30);
    }

    private void regenerateName(IconButton iconButton) {
        worldName = WorldCreationScreen.WORD_GEN.generate() + " " + WorldCreationScreen.WORD_GEN.generate();
        worldNameEntry.setValue(worldName);
    }

    private void onBack(TextButton caller) {
        back();
    }

    private void createWorld() {
        try {
            String folderName = WorldStorage.createFolderName();
            WorldStorage storage = new WorldStorage(QuantumClient.data("worlds").child(folderName));


            if (storage.exists(".")) {
                client.notifications.add(Notification.builder(TextObject.literal("Failed to create world"), TextObject.literal("World already exists")).icon(MessageIcon.ERROR).build());
                return;
            }

            storage.createDir(".");

            try {
                storage.saveInfo(new WorldSaveInfo(
                        seed,
                        World.REGION_DATA_VERSION,
                        gameMode,
                        gameMode,
                        worldName,
                        DateTime.current()
                ));
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to save world info", e);
                String localizedMessage = e.getLocalizedMessage();
                client.notifications.add(Notification.builder(TextObject.literal("Failed to save world info"), TextObject.literal(localizedMessage.substring(0, Math.min(localizedMessage.length() - 1, 50)))).icon(MessageIcon.ERROR).build());
                return;
            }

            storage.createWorld();
            client.startWorld(storage);
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to create world", e);
            client.notifications.add(Notification.builder(TextObject.literal("Failed to create world"), TextObject.literal(e.getLocalizedMessage())).icon(MessageIcon.ERROR).build());
        }
    }

    private void updateWorldName(TextEntry caller) {
        worldName = caller.getValue();
    }

    public TextEntry getWorldNameEntry() {
        return worldNameEntry;
    }

    public TextButton getCreateButton() {
        return createButton;
    }

    public IconButton getReloadButton() {
        return reloadButton;
    }

    public String getWorldName() {
        return worldName;
    }

    public TextEntry getSeedEntry() {
        return seedEntry;
    }
}
