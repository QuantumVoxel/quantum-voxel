package dev.ultreon.quantum.network;

import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public class S2CReplyPacket extends Packet<ClientPacketHandler> implements ReplyPacket {
    private final long sequenceId;

    public <T extends PacketHandler> S2CReplyPacket(long sequenceId) {
        super();

        this.sequenceId = sequenceId;
    }

    public S2CReplyPacket(PacketIO buffer) {
        this(buffer.readLong());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeLong(this.sequenceId);
    }

    @Override
    public void handle(PacketContext ctx, ClientPacketHandler handler) {
        handler.handleS2CReply(this.sequenceId);
    }
}
