package com.ultreon.quantum.network.client;

import com.ultreon.quantum.network.PacketHandler;
import com.ultreon.quantum.network.api.PacketDestination;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.ServerPacketHandler;
import com.ultreon.quantum.network.system.Connection;

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
