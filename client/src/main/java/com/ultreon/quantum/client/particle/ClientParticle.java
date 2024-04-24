package com.ultreon.quantum.client.particle;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader.ParticleEffectLoadParameter;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.resources.ResourceFileHandle;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.particles.ParticleType;

public class ClientParticle {
    private ParticleEffect particleFx;
    private final ParticleType type;

    public ClientParticle(ParticleType type) {
        this.type = type;

    }

    public void load(Array<ParticleBatch<?>> batches) {
        Identifier identifier = this.type.getId().mapPath(path -> "particles/" + path);
        var param = new ParticleEffectLoadParameter(batches);

        this.particleFx = new ParticleEffectLoader(fileName -> new ResourceFileHandle(Identifier.parse(fileName)))
                .loadSync(QuantumClient.get().getAssetManager(), identifier.toString(), new ResourceFileHandle(identifier), param);
    }

    public ParticleEffect getParticleFx() {
        return particleFx;
    }

    public ParticleType getType() {
        return type;
    }
}
