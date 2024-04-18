package com.ultreon.quantum.network.api;

import com.ultreon.quantum.network.system.IConnection;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record NetworkContext(PacketIO buffer, PacketDestination direction, IConnection connection, @Nullable ServerPlayer sender) {
    public void enqueueWork(Runnable task) {

    }
}
