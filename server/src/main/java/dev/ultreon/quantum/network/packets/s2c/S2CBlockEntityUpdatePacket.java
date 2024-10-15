package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.ubo.types.MapType;

public record S2CBlockEntityUpdatePacket(BlockVec pos, MapType data) implements Packet<InGameClientPacketHandler> {
    public static S2CBlockEntityUpdatePacket read(PacketIO buffer) {
        BlockVec blockVec = buffer.readBlockVec();
        MapType data = buffer.readUbo();
        return new S2CBlockEntityUpdatePacket(blockVec, data);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockVec(pos);
        buffer.writeUbo(data);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockEntityUpdate(pos, data);
    }
}
