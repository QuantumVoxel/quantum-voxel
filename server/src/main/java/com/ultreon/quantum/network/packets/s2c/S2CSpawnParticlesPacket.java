package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.world.particles.ParticleType;

public class S2CSpawnParticlesPacket extends Packet<InGameClientPacketHandler> {
    private final int particleTypeId;
    private final Vec3d position;
    private final Vec3d motion;
    private final int count;

    public S2CSpawnParticlesPacket(ParticleType particleType, Vec3d position, Vec3d motion, int count) {
        this.particleTypeId = particleType.getRawId();
        this.position = position;
        this.motion = motion;
        this.count = count;
    }

    public S2CSpawnParticlesPacket(PacketIO packetIO) {
        this.particleTypeId = packetIO.readVarInt();
        this.position = packetIO.readVec3d();
        this.motion = packetIO.readVec3d();
        this.count = packetIO.readVarInt();
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
        handler.onSpawnParticles(Registries.PARTICLE_TYPES.byId(this.particleTypeId), this.position, this.motion, this.count);
    }
}
