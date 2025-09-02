package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.Objects;

public final class S2CChunkUnloadPacket implements Packet<InGameClientPacketHandler> {
    private final ChunkVec chunkVec;

    public S2CChunkUnloadPacket(ChunkVec chunkVec) {
        this.chunkVec = chunkVec;
    }

    public static S2CChunkUnloadPacket read(PacketIO buffer) {
        return new S2CChunkUnloadPacket(buffer.readChunkVec());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.chunkVec);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkUnload(this.chunkVec);
    }

    @Override
    public String toString() {
        return "S2CChunkUnloadPacket{" +
               "chunkVec=" + chunkVec +
               '}';
    }

    public ChunkVec chunkVec() {
        return chunkVec;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CChunkUnloadPacket) obj;
        return Objects.equals(this.chunkVec, that.chunkVec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkVec);
    }

}
