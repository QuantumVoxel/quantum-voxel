package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.BlockVec;

public record C2SBlockBreakingPacket(BlockVec pos,
                                     dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakingPacket.BlockStatus status) implements Packet<InGameServerPacketHandler> {

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
}
