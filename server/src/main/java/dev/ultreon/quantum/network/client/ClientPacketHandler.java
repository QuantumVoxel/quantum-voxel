package dev.ultreon.quantum.network.client;

import dev.ultreon.quantum.network.PacketHandler;
import dev.ultreon.quantum.network.api.PacketDestination;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.Connection;

public interface ClientPacketHandler extends PacketHandler {
    @Override
    default PacketDestination destination() {
        return PacketDestination.SERVER;
    }

    @Override
    default Packet<ServerPacketHandler> reply(long sequenceId) {
        return new C2SReplyPacket(sequenceId);
    }

    default void handleS2CReply(long sequenceId) {
        Connection.handleReply(sequenceId);
    }
}
