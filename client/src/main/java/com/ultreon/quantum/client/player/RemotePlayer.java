package com.ultreon.quantum.client.player;

import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.entity.EntityTypes;
import com.ultreon.quantum.network.packets.AbilitiesPacket;
import com.ultreon.quantum.util.GameMode;
import com.ultreon.quantum.world.World;
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
