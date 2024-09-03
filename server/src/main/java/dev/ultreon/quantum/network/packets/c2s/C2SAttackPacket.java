package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public record C2SAttackPacket(int id) implements Packet<InGameServerPacketHandler> {

    public C2SAttackPacket(Entity entity) {
        this(entity.getId());
    }

    public static C2SAttackPacket read(PacketIO buffer) {
        var id = buffer.readInt();

        return new C2SAttackPacket(id);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(id);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onAttack(id);
    }
}
