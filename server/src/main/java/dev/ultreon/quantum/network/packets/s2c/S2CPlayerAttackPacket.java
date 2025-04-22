package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.server.player.ServerPlayer;

import java.util.Objects;

public final class S2CPlayerAttackPacket implements Packet<InGameClientPacketHandler> {
    private final int playerId;
    private final int entityId;

    public S2CPlayerAttackPacket(int playerId, int entityId) {
        this.playerId = playerId;
        this.entityId = entityId;
    }

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

    public int playerId() {
        return playerId;
    }

    public int entityId() {
        return entityId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CPlayerAttackPacket) obj;
        return this.playerId == that.playerId &&
               this.entityId == that.entityId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, entityId);
    }

    @Override
    public String toString() {
        return "S2CPlayerAttackPacket[" +
               "playerId=" + playerId + ", " +
               "entityId=" + entityId + ']';
    }

}
