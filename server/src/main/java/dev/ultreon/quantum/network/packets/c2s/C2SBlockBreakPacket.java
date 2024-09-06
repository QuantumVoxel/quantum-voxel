package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.world.vec.BlockVec;

@Deprecated
public record C2SBlockBreakPacket(BlockVec pos) implements Packet<InGameServerPacketHandler> {

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
}
