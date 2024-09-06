package dev.ultreon.quantum.client.particle;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.world.particles.ParticleType;

import java.util.HashMap;
import java.util.Map;

public class ClientParticleRegistry {
    private static final Map<ParticleType, ClientParticle> REGISTRY = new HashMap<>();
    private static final Map<ParticleType, ParticleController> CONTROLLER_REG = new HashMap<>();

    public static void registerAll() {
        for (ParticleType type : Registries.PARTICLE_TYPES.values()) {
            REGISTRY.put(type, new ClientParticle(type));
        }
    }

    public static void registerController(ParticleType type, ParticleController controller) {
        CONTROLLER_REG.put(type, controller);
    }

    public static ParticleController getController(ParticleType type) {
        return CONTROLLER_REG.get(type);
    }

    public static void loadAll(Array<ParticleBatch<?>> batches) {
        for (ClientParticle particle : REGISTRY.values()) {
            particle.load(batches);
        }
    }

    public static ClientParticle getParticle(ParticleType type) {
        return REGISTRY.get(type);
    }
}
