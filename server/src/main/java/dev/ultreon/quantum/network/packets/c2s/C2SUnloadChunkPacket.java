package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.Objects;

public final class C2SUnloadChunkPacket implements Packet<InGameServerPacketHandler> {
    private final ChunkVec vec;

    public C2SUnloadChunkPacket(ChunkVec vec) {
        this.vec = vec;
    }

    public static C2SUnloadChunkPacket read(PacketIO buffer) {
        var vec = buffer.readChunkVec();

        return new C2SUnloadChunkPacket(vec);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.vec);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.handleUnloadChunk(this);
    }

    public ChunkVec vec() {
        return vec;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SUnloadChunkPacket) obj;
        return Objects.equals(this.vec, that.vec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vec);
    }

    @Override
    public String toString() {
        return "C2SUnloadChunkPacket[" +
               "vec=" + vec + ']';
    }

}
