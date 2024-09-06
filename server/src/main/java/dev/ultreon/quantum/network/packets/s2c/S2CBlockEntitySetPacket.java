package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.world.vec.BlockVec;

public record S2CBlockEntitySetPacket(BlockVec pos, int blockEntityId) implements Packet<InGameClientPacketHandler> {
    public static S2CBlockEntitySetPacket read(PacketIO buffer) {
        var pos = buffer.readBlockVec();
        var blockEntityId = buffer.readVarInt();

        return new S2CBlockEntitySetPacket(pos, blockEntityId);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockVec(this.pos);
        buffer.writeVarInt(this.blockEntityId);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockEntitySet(this.pos, Registries.BLOCK_ENTITY_TYPE.byId(this.blockEntityId));
    }

    @Override
    public String toString() {
        return "S2CBlockEntitySetPacket{" +
               "pos=" + pos +
               ", blockEntityId=" + blockEntityId +
               '}';
    }
}
