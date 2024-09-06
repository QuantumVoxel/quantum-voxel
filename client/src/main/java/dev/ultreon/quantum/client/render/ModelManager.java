package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModelManager {
    public static final ModelManager INSTANCE = new ModelManager();
    private final ModelBuilder builder = new ModelBuilder();
    private final Map<NamespaceID, Model> models = new ConcurrentHashMap<>();

    private ModelManager() {

    }

    public Model createBox(float width, float height, float depth, Material material) {
        builder.begin();
        builder.createBox(width, height, depth, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        return builder.end();
    }

    public Model createCube(float size, Material material) {
        return createBox(size, size, size, material);
    }

    public Model createCylinder(float width, float height, float depth, int divisions, Material material) {
        builder.begin();
        builder.createCylinder(width, height, depth, divisions, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        return builder.end();
    }

    public Model createCone(float width, float height, float depth, int divisions, Material material) {
        builder.begin();
        builder.createCone(width, height, depth, divisions, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        return builder.end();
    }

    public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV, Material material) {
        builder.begin();
        builder.createSphere(width, height, depth, divisionsU, divisionsV, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        return builder.end();
    }

    public Model loadModel(NamespaceID id) {
        QuantumClient client = QuantumClient.get();
        Model model = client.modelLoader.loadModel(new ResourceFileHandle(id.mapPath(s -> "models/" + s + ".g3dj")),
                fileName -> client.getTextureManager().getTexture(new NamespaceID(fileName).mapPath(s -> {
                    if (fileName.startsWith("models/")) {
                        return "textures/" + fileName.substring("models/".length());
                    }

                    return fileName;
                }))
        );

        this.models.put(id, model);

        return model;
    }

    public Model generateModel(NamespaceID id, Consumer<ModelBuilder> builder) {
        if (this.models.containsKey(id)) {
            QuantumClient.LOGGER.error("Model {} already exists, the model will be unloaded and regenerated.", id);
//            this.unloadModel(id);
            return this.models.get(id);
        }

        this.builder.begin();
        builder.accept(this.builder);
        Model model = this.builder.end();

        this.models.put(id, model);

        return model;
    }

    public Model generateModel(NamespaceID id, Function<ModelBuilder, Model> builder) {
        Model model = builder.apply(this.builder);

        this.models.put(id, model);

        return model;
    }

    public Model getModel(NamespaceID id) {
        if (this.models.containsKey(id)) {
            return this.models.get(id);
        }

        return this.loadModel(id);
    }

    @CanIgnoreReturnValue
    public boolean unloadModel(NamespaceID id) {
        Model removed = this.models.remove(id);
        if (removed != null) {
            try {
                removed.dispose();
            } catch (Exception e) {
                QuantumClient.LOGGER.debug("Error unloading model {}:", id, e);
                return false;
            }
            return true;
        }
        return false;
    }

    public void reload() {
        Set<NamespaceID> namespaceIDS = Set.copyOf(this.models.keySet());
        this.models.clear();

        for (NamespaceID id : namespaceIDS) {
            this.loadModel(id);
        }
    }

    public void dispose() {
        for (NamespaceID id : this.models.keySet()) {
            unloadModel(id);
        }
    }

    public void add(NamespaceID resourceId, Model model) {
        this.models.put(resourceId, model);
    }
}
