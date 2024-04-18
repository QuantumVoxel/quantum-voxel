package com.ultreon.quantum.network.client;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.ReplyPacket;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.ServerPacketHandler;

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
