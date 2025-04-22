package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.Objects;

public final class S2CChunkCancelPacket implements Packet<InGameClientPacketHandler> {
    private final ChunkVec pos;

    public S2CChunkCancelPacket(ChunkVec pos) {
        this.pos = pos;
    }

    public static S2CChunkCancelPacket read(PacketIO buffer) {
        return new S2CChunkCancelPacket(buffer.readChunkVec());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkCancel(this.pos);
    }

    @Override
    public String toString() {
        return "S2CChunkCancelPacket{pos=" + this.pos + '}';
    }

    public ChunkVec pos() {
        return pos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CChunkCancelPacket) obj;
        return Objects.equals(this.pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }

}
