package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.LoginClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.util.Vec3d;

import java.util.UUID;

public record S2CLoginAcceptedPacket(UUID uuid, Vec3d spawnPos, GameMode gameMode, float health,
                                     float hunger) implements Packet<LoginClientPacketHandler> {
    public static S2CLoginAcceptedPacket read(PacketIO buffer) {
        var uuid = buffer.readUuid();
        var spawnPos = buffer.readVec3d();
        var gameMode = buffer.readEnum(GameMode.SURVIVAL);
        var health = buffer.readFloat();
        var hunger = buffer.readFloat();

        return new S2CLoginAcceptedPacket(uuid, spawnPos, gameMode, health, hunger);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUuid(this.uuid);
        buffer.writeVec3d(this.spawnPos);
        buffer.writeEnum(this.gameMode);
        buffer.writeFloat(this.health);
        buffer.writeFloat(this.hunger);
    }

    @Override
    public void handle(PacketContext ctx, LoginClientPacketHandler handler) {
        handler.onLoginAccepted(this);
    }

    @Override
    public String toString() {
        return "S2CLoginAcceptedPacket{" +
               "uuid=" + uuid +
               '}';
    }
}
