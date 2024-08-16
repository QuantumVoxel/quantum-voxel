package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.world.BlockVec;

public class S2CBlockEntitySetPacket extends Packet<InGameClientPacketHandler> {
    private final BlockVec pos;
    private final int blockEntityId;

    public S2CBlockEntitySetPacket(BlockVec pos, int blockEntityId) {
        this.pos = pos;
        this.blockEntityId = blockEntityId;
    }

    public S2CBlockEntitySetPacket(PacketIO buffer) {
        this.pos = buffer.readBlockVec();
        this.blockEntityId = buffer.readVarInt();
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
}
