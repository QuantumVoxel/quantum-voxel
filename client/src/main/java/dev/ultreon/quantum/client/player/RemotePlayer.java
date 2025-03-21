package dev.ultreon.quantum.client.player;

import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.util.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is used to represent a remote player.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class RemotePlayer extends ClientPlayer {
    private String name = "<Player>";

    /**
     * This constructor is used to create a new remote player.
     *
     * @param world The world to create the player in.
     */
    public RemotePlayer(@Nullable ClientWorldAccess world) {
        super(EntityTypes.PLAYER, world);
    }

    /**
     * This method is used to get the name of the player.
     *
     * @return The name of the player.
     */
    @Override
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * This method is used to set the name of the player.
     *
     * @param name The name to set the player to.
     */ 
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method is used to send the abilities of the player.
     */
    @Override
    protected void sendAbilities() {

    }

    /**
     * This method is used to handle the abilities of the player.
     *
     * @param packet The packet to handle.
     */ 
    @Override
    protected void onAbilities(@NotNull AbilitiesPacket packet) {

    }

    /**
     * This method is used to set the game mode of the player.
     *
     * @param gamemode The game mode to set the player to.
     */
    @Override
    public void setGameMode(@NotNull GameMode gamemode) {

    }

    /**
     * This method is used to handle the attack of the player.
     *
     * @param entity The entity to attack.
     */ 
    public void onAttack(Entity entity) {
        
    }

    /**
     * This method is used to set the rotation of the player.
     *
     * @param xRot The x rotation to set the player to.
     * @param yRot The y rotation to set the player to.
     */
    public void setRotation(float xRot, float yRot) {
        this.xRot = xRot;
        this.yRot = yRot;
    }
}
