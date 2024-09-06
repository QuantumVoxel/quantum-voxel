package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CRemoveEntityPacket(int id) implements Packet<InGameClientPacketHandler> {

    public S2CRemoveEntityPacket(Entity entity) {
        this(entity.getId());
    }

    public static S2CRemoveEntityPacket read(PacketIO buffer) {
        var id = buffer.readVarInt();

        return new S2CRemoveEntityPacket(id);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(this.id);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRemoveEntity(this.id);
    }

    @Override
    public String toString() {
        return "S2CRemoveEntityPacket(id=" + this.id + ")";
    }
}
