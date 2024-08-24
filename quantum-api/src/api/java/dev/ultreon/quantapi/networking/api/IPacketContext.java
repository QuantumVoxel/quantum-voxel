package dev.ultreon.quantapi.networking.api;

import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Env;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;

public interface IPacketContext {
    Env getDestination();

    void queue(Runnable function);

    @Nullable
    ServerPlayer getPlayer();
}
