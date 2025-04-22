package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.LoginClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.util.Vec3d;

import java.util.Objects;
import java.util.UUID;

public final class S2CLoginAcceptedPacket implements Packet<LoginClientPacketHandler> {
    private final UUID uuid;
    private final Vec3d spawnPos;
    private final GameMode gameMode;
    private final float health;
    private final int hunger;

    public S2CLoginAcceptedPacket(UUID uuid, Vec3d spawnPos, GameMode gameMode, float health,
                                  int hunger) {
        this.uuid = uuid;
        this.spawnPos = spawnPos;
        this.gameMode = gameMode;
        this.health = health;
        this.hunger = hunger;
    }

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

    public UUID uuid() {
        return uuid;
    }

    public Vec3d spawnPos() {
        return spawnPos;
    }

    public GameMode gameMode() {
        return gameMode;
    }

    public float health() {
        return health;
    }

    public int hunger() {
        return hunger;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CLoginAcceptedPacket) obj;
        return Objects.equals(this.uuid, that.uuid) &&
               Objects.equals(this.spawnPos, that.spawnPos) &&
               Objects.equals(this.gameMode, that.gameMode) &&
               Float.floatToIntBits(this.health) == Float.floatToIntBits(that.health) &&
               this.hunger == that.hunger;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, spawnPos, gameMode, health, hunger);
    }

}
