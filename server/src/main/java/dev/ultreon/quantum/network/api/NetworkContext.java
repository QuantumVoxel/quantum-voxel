package dev.ultreon.quantum.network.api;

import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record NetworkContext(PacketIO buffer, PacketDestination direction, IConnection connection, @Nullable ServerPlayer sender) {
    public void enqueueWork(Runnable task) {

    }
}
