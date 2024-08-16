package dev.ultreon.quantum.client.particle;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader.ParticleEffectLoadParameter;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.particles.ParticleType;

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
        NamespaceID namespaceID = this.type.getId().mapPath(path -> "particles/" + path + ".pfx");
        var param = new ParticleEffectLoadParameter(batches);

        AssetManager assetManager = QuantumClient.get().getAssetManager();
        assetManager.load(namespaceID.toString(), ParticleEffect.class, param);
        this.particleEffect = QuantumClient.invokeAndWait(() -> assetManager.finishLoadingAsset(namespaceID.toString()));

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
