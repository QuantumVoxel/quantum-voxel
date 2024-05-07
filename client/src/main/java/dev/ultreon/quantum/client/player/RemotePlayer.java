package dev.ultreon.quantum.client.player;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

public class RemotePlayer extends ClientPlayer {
    private String name = "<Player>";

    public RemotePlayer(World world) {
        super(EntityTypes.PLAYER, world);
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected void sendAbilities() {

    }

    @Override
    protected void onAbilities(@NotNull AbilitiesPacket packet) {

    }

    @Override
    public void setGameMode(@NotNull GameMode gamemode) {

    }

    public void onAttack(Entity entity) {
        // TODO
    }
}
