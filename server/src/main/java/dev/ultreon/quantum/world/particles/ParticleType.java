package dev.ultreon.quantum.world.particles;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

public class ParticleType {
    public ParticleType() {

    }

    public NamespaceID getId() {
        return Registries.PARTICLE_TYPES.getId(this);
    }

    public int getRawId() {
        return Registries.PARTICLE_TYPES.getRawId(this);
    }
}
