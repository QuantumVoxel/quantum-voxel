package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.particles.ParticleType;

public record S2CSpawnParticlesPacket(int particleTypeId, Vec3d position, Vec3d motion,
                                      int count) implements Packet<InGameClientPacketHandler> {

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
        return Registries.PARTICLE_TYPES.byId(this.particleTypeId);
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
}
