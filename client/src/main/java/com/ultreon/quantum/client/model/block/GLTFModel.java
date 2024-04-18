package com.ultreon.quantum.client.model.block;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.resources.ResourceLoader;
import com.ultreon.quantum.client.world.ClientChunk;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.BlockPos;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public class GLTFModel implements BlockModel {
    private final Identifier resource;
    private SceneAsset asset;

    public GLTFModel(Identifier resource) {
        this.resource = resource;
    }

    @Override
    public void load(QuantumClient client) {
        this.asset = client.deferDispose(ResourceLoader.loadGLTF(this.resource));
    }

    @Override
    public Identifier resourceId() {
        return this.resource;
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    @Override
    public void loadInto(BlockPos pos, ClientChunk chunk) {
        Model model = this.asset.scene.model;
        var instance = new ModelInstance(model, "GLTF_MODEL@" + resource.toString());
        chunk.addModel(pos, instance);
    }

    @Override
    public Model getModel() {
        return this.asset.scene.model;
    }

    public SceneAsset getAsset() {
        return this.asset;
    }

    @Override
    public void dispose() {
        this.asset.dispose();
    }
}
