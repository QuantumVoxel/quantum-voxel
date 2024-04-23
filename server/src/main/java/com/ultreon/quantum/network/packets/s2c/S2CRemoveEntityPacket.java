package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.entity.EntityType;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.registry.Registries;

public class S2CRemoveEntityPacket extends Packet<InGameClientPacketHandler> {
    private final int id;;

    public S2CRemoveEntityPacket(int id) {
        this.id = id;
    }

    public S2CRemoveEntityPacket(PacketIO buffer) {
        this.id = buffer.readVarInt();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(this.id);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRemoveEntity(this.id);
    }
}
