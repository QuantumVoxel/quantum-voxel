package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.particles.ParticleType;

import java.util.Objects;

public final class S2CSpawnParticlesPacket implements Packet<InGameClientPacketHandler> {
    private final int particleTypeId;
    private final Vec3d position;
    private final Vec3d motion;
    private final int count;

    public S2CSpawnParticlesPacket(int particleTypeId, Vec3d position, Vec3d motion,
                                   int count) {
        this.particleTypeId = particleTypeId;
        this.position = position;
        this.motion = motion;
        this.count = count;
    }

    public S2CSpawnParticlesPacket(ParticleType particleType, Vec3d position, Vec3d motion, int count) {
        this(particleType.getRawId(), position, motion, count);
    }

    public static S2CSpawnParticlesPacket read(PacketIO packetIO) {
        var particleTypeId = packetIO.readVarInt();
        var position = packetIO.readVec3d();
        var motion = packetIO.readVec3d();
        var count = packetIO.readVarInt();

        return new S2CSpawnParticlesPacket(particleTypeId, position, motion, count);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(this.particleTypeId);
        buffer.writeVec3d(this.position);
        buffer.writeVec3d(this.motion);
        buffer.writeVarInt(this.count);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onSpawnParticles(particleType(), this.position, this.motion, this.count);
    }

    public ParticleType particleType() {
        return Registries.PARTICLE_TYPES.byRawId(this.particleTypeId);
    }

    @Override
    public String toString() {
        return "S2CSpawnParticlesPacket{" +
               "particleTypeId=" + this.particleTypeId +
               ", position=" + this.position +
               ", motion=" + this.motion +
               ", count=" + this.count +
               '}';
    }

    public int particleTypeId() {
        return particleTypeId;
    }

    public Vec3d position() {
        return position;
    }

    public Vec3d motion() {
        return motion;
    }

    public int count() {
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CSpawnParticlesPacket) obj;
        return this.particleTypeId == that.particleTypeId &&
               Objects.equals(this.position, that.position) &&
               Objects.equals(this.motion, that.motion) &&
               this.count == that.count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(particleTypeId, position, motion, count);
    }

}
