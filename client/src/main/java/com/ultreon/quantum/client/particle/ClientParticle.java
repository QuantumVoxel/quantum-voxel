package com.ultreon.quantum.client.particle;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader.ParticleEffectLoadParameter;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.resources.ResourceFileHandle;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.particles.ParticleType;

public class ClientParticle {
    private ParticleEffect particleEffect;
    private final ParticleType type;
    private ParticleController particleController;
    private BillboardParticleBatch billboardParticleBatch;
    private PFXPool pool;

    public ClientParticle(ParticleType type) {
        this.type = type;
    }

    public void load(Array<ParticleBatch<?>> batches) {
        Identifier identifier = this.type.getId().mapPath(path -> "particles/" + path + ".pfx");
        var param = new ParticleEffectLoadParameter(batches);

        AssetManager assetManager = QuantumClient.get().getAssetManager();
        assetManager.load(identifier.toString(), ParticleEffect.class, param);
        this.particleEffect = QuantumClient.invokeAndWait(() -> assetManager.finishLoadingAsset(identifier.toString()));

        this.pool = new PFXPool(particleEffect);
    }

    public ParticleEffect getParticleEffect() {
        return particleEffect;
    }

    public ParticleType getType() {
        return type;
    }

    public ParticleController getParticleController() {
        if (this.particleController == null) {
            return this.particleController = ClientParticleRegistry.getController(type);
        }
        return this.particleController;
    }

    public PFXPool getPool() {
        return pool;
    }
}
