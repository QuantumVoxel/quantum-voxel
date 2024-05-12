package dev.ultreon.quantum.network;

import dev.ultreon.quantum.network.packets.Packet;

import java.util.Objects;

public final class PacketSequence<T extends PacketHandler> {
    private final long sequence;
    private final Packet<T> packet;

    public PacketSequence(long sequence, Packet<T> packet) {
        this.sequence = sequence;
        this.packet = packet;
    }

    public long sequence() {
        return sequence;
    }

    public Packet<T> packet() {
        return packet;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PacketSequence) obj;
        return this.sequence == that.sequence &&
               Objects.equals(this.packet, that.packet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequence, packet);
    }

    @Override
    public String toString() {
        return "PacketSequence[" +
               "sequence=" + sequence + ", " +
               "packet=" + packet + ']';
    }

}
