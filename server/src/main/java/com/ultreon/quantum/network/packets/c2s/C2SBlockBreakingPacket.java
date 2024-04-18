package com.ultreon.quantum.network.packets.c2s;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.InGameServerPacketHandler;
import com.ultreon.quantum.world.BlockPos;

public class C2SBlockBreakingPacket extends Packet<InGameServerPacketHandler> {
    private final BlockPos pos;
    private final BlockStatus status;

    public C2SBlockBreakingPacket(BlockPos pos, BlockStatus status) {
        this.status = status;
        this.pos = pos;
    }

    public C2SBlockBreakingPacket(PacketIO buffer) {
        this.status = BlockStatus.values()[buffer.readByte()];
        this.pos = buffer.readBlockPos();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeByte(this.status.ordinal());
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onBlockBreaking(this.pos, this.status);
    }

    public enum BlockStatus {
        START,
        CONTINUE,
        STOP,
    }
}
