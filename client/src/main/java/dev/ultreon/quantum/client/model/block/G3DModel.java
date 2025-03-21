package dev.ultreon.quantum.client.model.block;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.client.resources.ResourceLoader;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class G3DModel implements BlockModel {
    private final NamespaceID resource;
    private final ModelConfig config;
    private Model model;

    public G3DModel(NamespaceID resource) {
        this(resource, new ModelConfig());
    }

    public G3DModel(NamespaceID resource, ModelConfig config) {
        this.resource = resource;
        this.config = config;
    }

    @Override
    public void load(QuantumClient client) {
        Model loaded = ResourceLoader.loadG3D(this.resource);
        ModelManager.INSTANCE.add(this.resourceId(), loaded);
        this.model = loaded;
    }

    @Override
    public NamespaceID resourceId() {
        return this.resource;
    }

    @Override
    public String getRenderPass() {
        return "transparent";
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    @Override
    public void loadInto(BlockVec pos, ClientChunk chunk) {
        Model model = this.model;
        var instance = new ModelInstance(model);
        instance.transform.setToTranslationAndScaling(new Vector3(config.translation), new Vector3().add(config.scale));
        chunk.addModel(pos, instance);
    }

    @Override
    public Model getModel() {
        return this.model;
    }

    public static class ModelConfig {
        public static final ModelConfig BLOCKBENCH = new ModelConfig().scale(1 / 100f);

        private final Vector3 scale = new Vector3(1, 1, 1);
        private final Vector3 translation = new Vector3();

        public ModelConfig scale(float x, float y, float z) {
            this.scale.set(x, y, z);
            return this;
        }

        public ModelConfig scale(Vector3 scale) {
            this.scale.set(scale);
            return this;
        }

        public ModelConfig scale(float scale) {
            this.scale.set(scale, scale, scale);
            return this;
        }

        public ModelConfig translation(float x, float y, float z) {
            this.translation.set(x, y, z);
            return this;
        }

        public ModelConfig translation(Vector3 translation) {
            this.translation.set(translation);
            return this;
        }
    }
}
