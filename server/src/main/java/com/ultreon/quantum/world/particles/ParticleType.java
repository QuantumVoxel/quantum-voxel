package com.ultreon.quantum.world.particles;

import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.util.Identifier;

public class ParticleType {
    public ParticleType() {

    }

    public Identifier getId() {
        return Registries.PARTICLE_TYPES.getId(this);
    }

    public int getRawId() {
        return Registries.PARTICLE_TYPES.getRawId(this);
    }
}
