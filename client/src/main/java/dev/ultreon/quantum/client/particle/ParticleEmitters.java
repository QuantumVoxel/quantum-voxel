package dev.ultreon.quantum.client.particle;

import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import dev.ultreon.quantum.client.ClientRegistries;
import dev.ultreon.quantum.util.Identifier;

public class ParticleEmitters {
    public static final RegularEmitter REGULAR = register("regular", new RegularEmitter());

    private static <T extends Emitter> T register(String name, T value) {
        ClientRegistries.PARTICLE_EMITTER.register(new Identifier(name), value);
        return value;
    }

    public static void init() {

    }
}
