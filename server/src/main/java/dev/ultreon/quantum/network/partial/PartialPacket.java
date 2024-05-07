package dev.ultreon.quantum.network.partial;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

public record PartialPacket(int packetId, long sequenceId, int dataOffset, int dataLength, ByteBuf data) {
    public PartialPacket(int packetId, long sequenceId, int dataOffset, int dataLength, ByteBuffer data) {
        this(packetId, sequenceId, dataOffset, dataLength, Unpooled.wrappedBuffer(data));
    }

    public void encode(ByteBuf byteBuf) {
        data.writeBytes(byteBuf);
    }
}
