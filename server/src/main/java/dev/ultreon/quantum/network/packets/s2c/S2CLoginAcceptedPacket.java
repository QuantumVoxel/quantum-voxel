package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.LoginClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.util.Vec3d;

import java.util.UUID;

public class S2CLoginAcceptedPacket extends Packet<LoginClientPacketHandler> {
    private final UUID uuid;
    private final Vec3d spawnPos;
    private final GameMode gameMode;
    private final float health;
    private final float hunger;

    public S2CLoginAcceptedPacket(UUID uuid, Vec3d spawnPos, GameMode gameMode, float health, float hunger) {
        this.uuid = uuid;
        this.spawnPos = spawnPos;
        this.gameMode = gameMode;
        this.health = health;
        this.hunger = hunger;
    }

    public S2CLoginAcceptedPacket(PacketIO buffer) {
        this.uuid = buffer.readUuid();
        this.spawnPos = buffer.readVec3d();
        this.gameMode = buffer.readEnum(GameMode.SURVIVAL);
        this.health = buffer.readFloat();
        this.hunger = buffer.readFloat();
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

    public Vec3d getSpawnPos() {
        return this.spawnPos;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public float getHealth() {
        return this.health;
    }

    public float getHunger() {
        return this.hunger;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String toString() {
        return "S2CLoginAcceptedPacket{" +
                "uuid=" + uuid +
                '}';
    }
}
