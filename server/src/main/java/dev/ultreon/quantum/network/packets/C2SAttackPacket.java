package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.network.server.ServerPacketHandler;

public class C2SAttackPacket extends Packet<InGameServerPacketHandler> {
    private final int id;

    public C2SAttackPacket(Entity entity) {
        this.id = entity.getId();
    }

    public C2SAttackPacket(PacketIO buffer) {
        this.id = buffer.readInt();
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
