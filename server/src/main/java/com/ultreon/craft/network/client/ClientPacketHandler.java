package com.ultreon.craft.network.client;

import com.ultreon.craft.network.PacketHandler;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.system.Connection;

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
