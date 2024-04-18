package com.ultreon.quantum.network.packets.c2s;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.InGameServerPacketHandler;
import com.ultreon.quantum.world.BlockPos;

public class C2SBlockBreakPacket extends Packet<InGameServerPacketHandler> {
    private final BlockPos pos;

    public C2SBlockBreakPacket(BlockPos pos) {
        this.pos = pos;
    }

    public C2SBlockBreakPacket(PacketIO buffer) {
        this.pos = buffer.readBlockPos();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onBlockBroken(this.pos);
    }
}
