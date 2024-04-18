package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.world.BlockPos;

public class S2CBlockEntitySetPacket extends Packet<InGameClientPacketHandler> {
    private final BlockPos pos;
    private final int blockEntityId;

    public S2CBlockEntitySetPacket(BlockPos pos, int blockEntityId) {
        this.pos = pos;
        this.blockEntityId = blockEntityId;
    }

    public S2CBlockEntitySetPacket(PacketIO buffer) {
        this.pos = buffer.readBlockPos();
        this.blockEntityId = buffer.readVarInt();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeVarInt(this.blockEntityId);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockEntitySet(this.pos, Registries.BLOCK_ENTITY_TYPE.byId(this.blockEntityId));
    }
}
