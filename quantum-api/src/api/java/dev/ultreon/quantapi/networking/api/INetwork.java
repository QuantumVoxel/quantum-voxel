package dev.ultreon.quantapi.networking.api;

import dev.ultreon.quantapi.networking.api.packet.IClientEndpoint;
import dev.ultreon.quantapi.networking.api.packet.IServerEndpoint;
import dev.ultreon.quantapi.networking.api.packet.Packet;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.NamespaceID;

public interface INetwork {
    <T extends Packet<T> & IClientEndpoint> void sendPlayer(T packet, ServerPlayer player);

    <T extends Packet<T> & IClientEndpoint> void sendAll(T packet, ServerPlayer player);

    <T extends Packet<T> & IServerEndpoint> void sendToServer(T packet);

    NamespaceID getId();
}
