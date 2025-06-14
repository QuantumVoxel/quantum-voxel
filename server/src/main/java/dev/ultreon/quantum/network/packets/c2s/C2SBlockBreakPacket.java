package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.BlockVec;

import java.util.Objects;

/**
 * Represents a C2S (Client to Server) packet indicating that a block break event has occurred.
 *
 */
public final class C2SBlockBreakPacket implements Packet<InGameServerPacketHandler> {
    private final BlockVec pos;

    /**
     * @param pos The position of the block that was broken.
     */
    public C2SBlockBreakPacket(BlockVec pos) {
        this.pos = pos;
    }

    public static C2SBlockBreakPacket read(PacketIO buffer) {
        var pos = buffer.readBlockVec();

        return new C2SBlockBreakPacket(pos);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockVec(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
//        handler.onBlockBroken(this.pos);
    }

    @Override
    public String toString() {
        return "C2SBlockBreakPacket{" +
               "pos=" + pos +
               '}';
    }

    public BlockVec pos() {
        return pos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SBlockBreakPacket) obj;
        return Objects.equals(this.pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }

}
