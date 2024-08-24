package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.BlockVec;

public class C2SBlockBreakingPacket extends Packet<InGameServerPacketHandler> {
    private final BlockVec pos;
    private final BlockStatus status;

    public C2SBlockBreakingPacket(BlockVec pos, BlockStatus status) {
        this.status = status;
        this.pos = pos;
    }

    public C2SBlockBreakingPacket(PacketIO buffer) {
        this.status = BlockStatus.values()[buffer.readByte()];
        this.pos = buffer.readBlockVec();
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

    public BlockVec getPos() {
        return pos;
    }

    public BlockStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "C2SBlockBreakingPacket{pos=" + this.pos + ", status=" + this.status + '}';
    }
}
