package com.ultreon.quantum.client.particle;

import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.world.particles.ParticleType;

import java.util.HashMap;
import java.util.Map;

public class ClientParticleRegistry {
    private static final Map<ParticleType, ClientParticle> REGISTRY = new HashMap<>();

    public static void registerAll() {
        for (ParticleType type : Registries.PARTICLE_TYPES.values()) {
            REGISTRY.put(type, new ClientParticle(type));
        }
    }

    public static void loadAll(Array<ParticleBatch<?>> batches) {
        for (ClientParticle particle : REGISTRY.values()) {
            particle.load(batches);
        }
    }

    public static ClientParticle get(ParticleType type) {
        return REGISTRY.get(type);
    }
}
