package dev.ultreon.quantum.client;

import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.util.GameMode;

import java.util.UUID;

public class CameraPlayer extends LocalPlayer {
    public CameraPlayer(ClientWorldAccess world, UUID uuid) {
        super(EntityTypes.PLAYER, world, uuid);

        this.health = 20;
        this.setMaxHealth(20);
        this.abilities.invincible = true;
        this.abilities.blockBreak = false;
        this.abilities.instaMine = false;
        this.abilities.allowFlight = true;
        this.abilities.flying = true;
        this.setGameMode(GameMode.SPECTATOR);

        this.abilities.invincible = true;
        this.abilities.blockBreak = false;
        this.abilities.instaMine = false;
        this.abilities.allowFlight = true;
        this.abilities.flying = true;
        this.noClip = true;
        this.noGravity = true;
    }
}
