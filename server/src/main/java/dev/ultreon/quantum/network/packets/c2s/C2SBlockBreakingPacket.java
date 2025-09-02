package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.BlockVec;

import java.util.Objects;

public final class C2SBlockBreakingPacket implements Packet<InGameServerPacketHandler> {
    private final BlockVec pos;
    private final BlockStatus status;

    public C2SBlockBreakingPacket(BlockVec pos,
                                  BlockStatus status) {
        this.pos = pos;
        this.status = status;
    }

    public static C2SBlockBreakingPacket read(PacketIO buffer) {
        var status = BlockStatus.values()[buffer.readByte()];
        var pos = buffer.readBlockVec();

        return new C2SBlockBreakingPacket(pos, status);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeByte(this.status.ordinal());
        buffer.writeBlockVec(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onBlockBreaking(this.pos, this.status);
    }

    public enum BlockStatus {
        START,
        CONTINUE,
        STOP,
        BROKEN
    }

    @Override
    public String toString() {
        return "C2SBlockBreakingPacket{pos=" + this.pos + ", status=" + this.status + '}';
    }

    public BlockVec pos() {
        return pos;
    }

    public BlockStatus status() {
        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SBlockBreakingPacket) obj;
        return Objects.equals(this.pos, that.pos) &&
               Objects.equals(this.status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, status);
    }

}
