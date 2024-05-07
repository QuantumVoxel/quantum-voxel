package dev.ultreon.quantum.server.player;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public class S2CPlayerAttackPacket extends Packet<InGameClientPacketHandler> {
    public final int playerId;
    public final int entityId;

    public S2CPlayerAttackPacket(ServerPlayer player, Entity entity) {
        this.playerId = player.getId();
        this.entityId = entity.getId();
    }

    public S2CPlayerAttackPacket(int playerId, int entityId) {
        this.playerId = playerId;
        this.entityId = entityId;
    }

    public S2CPlayerAttackPacket(PacketIO buffer) {
        this.playerId = buffer.readVarInt();
        this.entityId = buffer.readVarInt();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(playerId);
        buffer.writeVarInt(entityId);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerAttack(this.playerId, this.entityId);
    }
}
