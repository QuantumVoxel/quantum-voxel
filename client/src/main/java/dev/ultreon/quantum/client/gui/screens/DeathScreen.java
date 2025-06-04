package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.network.packets.c2s.C2SRespawnPacket;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;

public class DeathScreen extends Screen {
    private final DamageSource source;
    private TextButton respawnButton;
    private TextButton exitWorldButton;

    public DeathScreen(@NotNull DamageSource source) {
        super(Language.translate("quantum.screen.death.title"));
        this.source = source;
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .scale(2)
                .withPositioning(() -> new Position(this.size.width / 2, this.size.height / 3 - 50)));

        builder.add(Label.of(source.getDescription(client.player != null ? client.player : null))
                .alignment(Alignment.CENTER)
                .withPositioning(() -> new Position(this.size.width / 2, this.size.height / 3 - 25)));

        this.respawnButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.death.respawn"))
                .withPositioning(() -> new Position(this.size.width / 2 - 100, this.size.height / 3))
                .withCallback(this::respawn)
                .translation("quantum.screen.death.respawn"));

        this.exitWorldButton = builder.add(TextButton.of(TextObject.translation("quantum.ui.exitWorld"))
                        .withPositioning(() -> new Position(this.size.width / 2 - 100, this.size.height / 3 + 25)))
                .withCallback(this::exitWorld)
                .translation("quantum.ui.exitWorld");
    }

    private void respawn(TextButton button) {
        this.client.connection.send(new C2SRespawnPacket());
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }

    @Override
    public void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);
    }

    private void exitWorld(TextButton caller) {
        this.client.exitWorldToTitle();
    }

    public TextButton getRespawnButton() {
        return this.respawnButton;
    }

    public TextButton getExitWorldButton() {
        return this.exitWorldButton;
    }
}
