package dev.ultreon.quantapi.networking.api;

import dev.ultreon.quantapi.networking.api.packet.IClientEndpoint;
import dev.ultreon.quantapi.networking.api.packet.IServerEndpoint;
import dev.ultreon.quantapi.networking.api.packet.Packet;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.List;

public interface INetChannel {
    NamespaceID id();

    <T extends Packet<T> & IClientEndpoint> void sendToClient(ServerPlayer player, T modPacket);

    <T extends Packet<T> & IClientEndpoint> void sendToClients(List<ServerPlayer> players, T modPacket);

    <T extends Packet<T> & IServerEndpoint> void sendToServer(T modPacket);
}
