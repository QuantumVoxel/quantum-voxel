package dev.ultreon.quantum.network.partial;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class PartialPacket {
    private final int packetId;
    private final long sequenceId;
    private final int dataOffset;
    private final int dataLength;
    private final ByteBuf data;

    public PartialPacket(int packetId, long sequenceId, int dataOffset, int dataLength, ByteBuf data) {
        this.packetId = packetId;
        this.sequenceId = sequenceId;
        this.dataOffset = dataOffset;
        this.dataLength = dataLength;
        this.data = data;
    }

    public PartialPacket(int packetId, long sequenceId, int dataOffset, int dataLength, ByteBuffer data) {
        this(packetId, sequenceId, dataOffset, dataLength, Unpooled.wrappedBuffer(data));
    }

    public void encode(ByteBuf byteBuf) {
        data.writeBytes(byteBuf);
    }

    public int packetId() {
        return packetId;
    }

    public long sequenceId() {
        return sequenceId;
    }

    public int dataOffset() {
        return dataOffset;
    }

    public int dataLength() {
        return dataLength;
    }

    public ByteBuf data() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PartialPacket) obj;
        return this.packetId == that.packetId &&
               this.sequenceId == that.sequenceId &&
               this.dataOffset == that.dataOffset &&
               this.dataLength == that.dataLength &&
               Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packetId, sequenceId, dataOffset, dataLength, data);
    }

    @Override
    public String toString() {
        return "PartialPacket[" +
               "packetId=" + packetId + ", " +
               "sequenceId=" + sequenceId + ", " +
               "dataOffset=" + dataOffset + ", " +
               "dataLength=" + dataLength + ", " +
               "data=" + data + ']';
    }

}
