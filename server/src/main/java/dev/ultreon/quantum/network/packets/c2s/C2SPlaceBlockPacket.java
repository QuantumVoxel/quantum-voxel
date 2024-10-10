package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public record C2SPlaceBlockPacket(int x, int y, int z, BlockState block) implements Packet<InGameServerPacketHandler> {

    public static C2SPlaceBlockPacket read(PacketIO buffer) {
        var x = buffer.readVarInt();
        var y = buffer.readVarInt();
        var z = buffer.readVarInt();
        var block = buffer.readBlockState();

        return new C2SPlaceBlockPacket(x, y, z, block);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(x);
        buffer.writeVarInt(y);
        buffer.writeVarInt(z);
        buffer.writeBlockState(block);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPlaceBlock(x, y, z, block);
    }

    @Override
    public String toString() {
        return "C2SPlaceBlockPacket{" +
               "x=" + x +
               ", y=" + y +
               ", z=" + z +
               ", block=" + block +
               '}';
    }
}
