package com.ultreon.craft.network.api;

import com.ultreon.craft.network.system.IConnection;
import com.ultreon.craft.network.PacketIO;
import com.ultreon.craft.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record NetworkContext(PacketIO buffer, PacketDestination direction, IConnection connection, @Nullable ServerPlayer sender) {
    public void enqueueWork(Runnable task) {

    }
}
