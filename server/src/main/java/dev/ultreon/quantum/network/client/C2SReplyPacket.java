package dev.ultreon.quantum.network.client;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.ReplyPacket;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.ServerPacketHandler;

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
