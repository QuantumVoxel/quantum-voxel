package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.DialogBuilder;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.ScrollableContainer;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.gui.widget.TextEntry;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.world.WorldSaveInfo;
import dev.ultreon.quantum.world.WorldStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class WorldEditScreen extends Screen {
    private final WorldStorage world;
    private String nameToSet;

    public WorldEditScreen(WorldStorage world) {
        super(TextObject.translation("quantum.screen.worlds.edit.title"));
        this.world = world;
    }

    public WorldStorage getWorld() {
        return world;
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        ScrollableContainer container = builder.add(new ScrollableContainer())
                .bounds(() -> new Bounds(size.width / 2 - 200, size.height / 2 - 100, 400, 200));

        container.add(TextEntry.of())
                .callback(this::updateName);

        builder.add(TextButton.of(TextObject.translation("quantum.screen.worlds.edit.save")))
                .setCallback(this::save)
                .bounds(() -> new Bounds(size.width / 2 - 100, size.height / 2 + 100, 200, 20));

        builder.add(TextButton.of(TextObject.translation("quantum.screen.worlds.edit.cancel")))
                .setCallback(this::cancel)
                .bounds(() -> new Bounds(size.width / 2 - 100, size.height / 2 + 130, 200, 20));

    }

    private void cancel(TextButton textButton) {
        back();
    }

    private void save(TextButton textButton) {
        WorldSaveInfo worldSaveInfo = this.world.loadInfo();
        worldSaveInfo.setName(this.nameToSet);
        try {
            worldSaveInfo.save(this.world);
        } catch (IOException e) {
            showDialog(new DialogBuilder(this)
                    .title(TextObject.translation("quantum.dialog.error.title"))
                    .message(TextObject.translation("quantum.dialog.error.message", e.getLocalizedMessage()))
                    .button(TextObject.translation("quantum.dialog.ok"), dialog -> {
                        QuantumClient.LOGGER.error(e.getMessage(), e);
                        closeDialog(dialog);
                    }));
        }
        back();
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }

    private void updateName(TextEntry textEntry) {
        this.nameToSet = textEntry.getValue();
    }
}
