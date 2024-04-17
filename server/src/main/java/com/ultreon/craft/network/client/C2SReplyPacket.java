package com.ultreon.craft.network.client;

import com.ultreon.craft.network.PacketIO;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.ReplyPacket;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.ServerPacketHandler;

public class C2SReplyPacket extends Packet<ServerPacketHandler> implements ReplyPacket {
    private final long sequenceId;

    public C2SReplyPacket(long sequenceId) {
        super();

        this.sequenceId = sequenceId;
    }

    public C2SReplyPacket(PacketIO buffer) {
        this(buffer.readLong());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeLong(this.sequenceId);
    }

    @Override
    public void handle(PacketContext ctx, ServerPacketHandler handler) {
        handler.handleC2SReply(this.sequenceId);
    }
}
