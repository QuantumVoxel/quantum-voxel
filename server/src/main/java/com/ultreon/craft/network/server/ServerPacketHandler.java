package com.ultreon.craft.network.server;

import com.ultreon.craft.network.PacketEncoder;
import com.ultreon.craft.network.PacketHandler;
import com.ultreon.craft.network.S2CReplyPacket;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.Packet;

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
        PacketEncoder.handleReply(sequenceId);
    }
}
