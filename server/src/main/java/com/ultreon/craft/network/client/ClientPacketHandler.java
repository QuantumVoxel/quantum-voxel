package com.ultreon.craft.network.client;

import com.ultreon.craft.network.PacketEncoder;
import com.ultreon.craft.network.PacketHandler;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.ServerPacketHandler;

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
        PacketEncoder.handleReply(sequenceId);
    }
}
