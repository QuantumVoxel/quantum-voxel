package com.ultreon.quantum.network.server;

import com.ultreon.quantum.network.PacketHandler;
import com.ultreon.quantum.network.S2CReplyPacket;
import com.ultreon.quantum.network.api.PacketDestination;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.system.Connection;

public interface ServerPacketHandler extends PacketHandler {
    @Override
    default PacketDestination destination() {
        return PacketDestination.SERVER;
    }

    @Override
    default Packet<?> reply(long sequenceId) {
        return new S2CReplyPacket(sequenceId);
    }

    default void handleC2SReply(long sequenceId) {
        Connection.handleReply(sequenceId);
    }
}
