package dev.ultreon.quantum.network.server;

import dev.ultreon.quantum.network.PacketHandler;
import dev.ultreon.quantum.network.S2CReplyPacket;
import dev.ultreon.quantum.network.api.PacketDestination;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.system.Connection;

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
