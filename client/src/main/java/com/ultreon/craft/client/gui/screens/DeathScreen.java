package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.text.Language;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.network.packets.c2s.C2SRespawnPacket;
import com.ultreon.craft.text.TextObject;
import org.jetbrains.annotations.NotNull;

public class DeathScreen extends Screen {
    private final DamageSource source;
    private TextButton respawnButton;
    private TextButton exitWorldButton;

    public DeathScreen(@NotNull DamageSource source) {
        super(Language.translate("ultracraft.screen.death.title"));
        this.source = source;
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3 - 50)));

        builder.add(Label.of(source.getDescription(client.player != null ? client.player : null))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3 - 25)));

        this.respawnButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.death.respawn"))
                .position(() -> new Position(this.size.width / 2 - 100, this.size.height / 3))
                .callback(this::respawn)
                .translation("ultracraft.screen.death.respawn"));

        this.exitWorldButton = builder.add(TextButton.of(TextObject.translation("ultracraft.ui.exitWorld"))
                        .position(() -> new Position(this.size.width / 2 - 100, this.size.height / 3 + 25)))
                .callback(this::exitWorld)
                .translation("ultracraft.ui.exitWorld");
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
