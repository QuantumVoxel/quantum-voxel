package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class C2SRequestChunkLoadPacket implements Packet<InGameServerPacketHandler> {
    private final ChunkVec pos;

    public C2SRequestChunkLoadPacket(@NotNull ChunkVec pos) {
        this.pos = pos;
    }

    public static C2SRequestChunkLoadPacket read(PacketIO buffer) {
        var pos = buffer.readChunkVec();

        return new C2SRequestChunkLoadPacket(pos);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onRequestChunkLoad(pos);
    }

    public ChunkVec pos() {
        return pos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SRequestChunkLoadPacket) obj;
        return Objects.equals(this.pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }

    @Override
    public String toString() {
        return "C2SRequestChunkLoadPacket[" +
               "pos=" + pos + ']';
    }

}
