package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SPlaceBlockPacket extends Packet<InGameServerPacketHandler> {
    private final int x;
    private final int y;
    private final int z;
    private final BlockProperties block;

    public C2SPlaceBlockPacket(PacketIO buffer) {
        x = buffer.readVarInt();
        y = buffer.readVarInt();
        z = buffer.readVarInt();
        block = BlockProperties.read(buffer);
    }

    public C2SPlaceBlockPacket(int x, int y, int z, BlockProperties block) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(x);
        buffer.writeVarInt(y);
        buffer.writeVarInt(z);
        block.write(buffer);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPlaceBlock(x, y, z, block);
    }
}
