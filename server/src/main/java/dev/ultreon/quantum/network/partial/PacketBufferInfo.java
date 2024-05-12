package dev.ultreon.quantum.network.partial;

import dev.ultreon.quantum.network.PacketIO;

import java.util.Objects;

public final class PacketBufferInfo {
    private final int packetId;
    private final long sequence;
    private final PacketIO buffer;

    public PacketBufferInfo(int packetId, long sequence, PacketIO buffer) {
        this.packetId = packetId;
        this.sequence = sequence;
        this.buffer = buffer;
    }

    public int packetId() {
        return packetId;
    }

    public long sequence() {
        return sequence;
    }

    public PacketIO buffer() {
        return buffer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PacketBufferInfo) obj;
        return this.packetId == that.packetId &&
               this.sequence == that.sequence &&
               Objects.equals(this.buffer, that.buffer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packetId, sequence, buffer);
    }

    @Override
    public String toString() {
        return "PacketBufferInfo[" +
               "packetId=" + packetId + ", " +
               "sequence=" + sequence + ", " +
               "buffer=" + buffer + ']';
    }


}
