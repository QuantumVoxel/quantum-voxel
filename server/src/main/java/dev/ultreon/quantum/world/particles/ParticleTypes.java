package dev.ultreon.quantum.world.particles;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

public class ParticleTypes {
    public static final ParticleType ENTITY_SMOKE = register("entity_smoke", new ParticleType());

    private static ParticleType register(String name, ParticleType particleType) {
        Registries.PARTICLE_TYPES.register(new NamespaceID(name), particleType);
        return particleType;
    }

    public static void init() {

    }
}
