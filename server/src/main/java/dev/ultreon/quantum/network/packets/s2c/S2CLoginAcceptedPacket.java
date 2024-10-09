package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.LoginClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.util.Vec3d;

import java.util.UUID;

public record S2CLoginAcceptedPacket(UUID uuid, Vec3d spawnPos, GameMode gameMode, float health,
                                     int hunger) implements Packet<LoginClientPacketHandler> {
    public static S2CLoginAcceptedPacket read(PacketIO buffer) {
        var uuid = buffer.readUuid();
        var spawnPos = buffer.readVec3d();
        var gameMode = buffer.readEnum(GameMode.SURVIVAL);
        var health = buffer.readFloat();
        var hunger = buffer.readInt();

        return new S2CLoginAcceptedPacket(uuid, spawnPos, gameMode, health, hunger);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUuid(uuid);
        buffer.writeVec3d(spawnPos);
        buffer.writeEnum(gameMode);
        buffer.writeFloat(health);
        buffer.writeInt(hunger);
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
