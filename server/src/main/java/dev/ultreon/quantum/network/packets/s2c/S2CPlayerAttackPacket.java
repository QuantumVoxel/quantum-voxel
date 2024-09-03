package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.server.player.ServerPlayer;

public record S2CPlayerAttackPacket(int playerId, int entityId) implements Packet<InGameClientPacketHandler> {

    public S2CPlayerAttackPacket(ServerPlayer player, Entity entity) {
        this(player.getId(), entity.getId());
    }

    public static S2CPlayerAttackPacket read(PacketIO buffer) {
        var playerId = buffer.readVarInt();
        var entityId = buffer.readVarInt();

        return new S2CPlayerAttackPacket(playerId, entityId);
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
