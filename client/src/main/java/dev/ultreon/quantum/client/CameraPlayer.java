package dev.ultreon.quantum.client;

import java.util.UUID;

import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.util.GameMode;

/**
 * A class that extends LocalPlayer to create a camera player.
 * <p>
 * This class extends LocalPlayer to create a camera player.
 * </p>
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class CameraPlayer extends LocalPlayer {
    /**
     * Constructs a new CameraPlayer.
     * @param world The client world access.
     * @param uuid The UUID of the player.
     */
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
