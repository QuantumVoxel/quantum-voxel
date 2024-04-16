package com.ultreon.craft.network;

import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;

public class S2CReplyPacket extends Packet<ClientPacketHandler> implements ReplyPacket {
    private final long sequenceId;

    public <T extends PacketHandler> S2CReplyPacket(long sequenceId) {
        super();

        this.sequenceId = sequenceId;
    }

    public S2CReplyPacket(PacketBuffer buffer) {
        this(buffer.readLong());
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeLong(this.sequenceId);
    }

    @Override
    public void handle(PacketContext ctx, ClientPacketHandler handler) {
        handler.handleS2CReply(this.sequenceId);
    }
}
