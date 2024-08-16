package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.BlockVec;

public class C2SBlockBreakPacket extends Packet<InGameServerPacketHandler> {
    private final BlockVec pos;

    public C2SBlockBreakPacket(BlockVec pos) {
        this.pos = pos;
    }

    public C2SBlockBreakPacket(PacketIO buffer) {
        this.pos = buffer.readBlockVec();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockVec(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onBlockBroken(this.pos);
    }
}
