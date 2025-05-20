package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ModelManager is a singleton class responsible for managing 3D models.
 * It allows creation, loading, and unloading of models.
 * <p>
 * This is a part of the render pipeline. It is used to manage the models.
 * </p>
 *  
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class ModelManager {
    public static final ModelManager INSTANCE = new ModelManager();
    private final ThreadLocal<ModelBuilder> builder = ThreadLocal.withInitial(() -> new ModelBuilder());
    private final Map<NamespaceID, Model> models = new ConcurrentHashMap<>();

    private ModelManager() {

    }

    /**
     * Creates a 3D box model with the specified dimensions and material properties.
     *
     * @param width the width of the box
     * @param height the height of the box
     * @param depth the depth of the box
     * @param material the material to be used for the box
     * @return the created box model
     */
    public Model createBox(float width, float height, float depth, Material material) {
        builder.get().begin();
        builder.get().createBox(width, height, depth, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        return builder.get().end();
    }

    /**
     * Creates a cube model with the given size and material.
     *
     * @param size The size of the cube along each dimension.
     * @param material The material to be used for the cube.
     * @return A new {@link Model} instance representing the cube.
     */
    public Model createCube(float size, Material material) {
        return createBox(size, size, size, material);
    }

    /**
     * Creates a cylinder model with the specified dimensions, divisions, and material.
     *
     * @param width     the width (diameter) of the cylinder
     * @param height    the height of the cylinder
     * @param depth     the depth (perpendicular to the height) of the cylinder
     * @param divisions the number of divisions around the cylinder's circumference
     * @param material  the material to be used for the cylinder
     * @return a {@link Model} representing the created cylinder
     */
    public Model createCylinder(float width, float height, float depth, int divisions, Material material) {
        builder.get().begin();
        builder.get().createCylinder(width, height, depth, divisions, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        return builder.get().end();
    }

    /**
     * Creates a cone-shaped model with the specified dimensions and material.
     *
     * @param width The width of the base of the cone.
     * @param height The height of the cone.
     * @param depth The depth of the cone.
     * @param divisions The number of divisions along the circumference of the base.
     * @param material The material to be used for the cone.
     * @return A {@link Model} instance representing the created cone.
     */
    public Model createCone(float width, float height, float depth, int divisions, Material material) {
        builder.get().begin();
        builder.get().createCone(width, height, depth, divisions, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        return builder.get().end();
    }

    /**
     * Creates a spherical 3D model with the specified dimensions, divisions, and material attributes.
     *
     * @param width the width of the sphere
     * @param height the height of the sphere
     * @param depth the depth of the sphere
     * @param divisionsU the number of divisions along the horizontal axis
     * @param divisionsV the number of divisions along the vertical axis
     * @param material the material to be used for the sphere
     * @return the created spherical model
     */
    public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV, Material material) {
        builder.get().begin();
        builder.get().createSphere(width, height, depth, divisionsU, divisionsV, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        return builder.get().end();
    }

    /**
     * Loads a 3D model specified by the given NamespaceID.
     *
     * @param id the NamespaceID representing the specific model to load.
     * @return the loaded Model instance.
     */
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

    /**
     * Generates a model based on the given NamespaceID and a consumer to define the model using a ModelBuilder.
     *
     * @param id The NamespaceID representing the specific model to generate.
     * @param builder A consumer that accepts a ModelBuilder to define the model's attributes.
     * @return The generated Model instance.
     */
    public Model generateModel(NamespaceID id, Consumer<ModelBuilder> builder) {
        if (this.models.containsKey(id)) {
            QuantumClient.LOGGER.error("Model {} already exists, the model will be unloaded and regenerated.", id);
//            this.unloadModel(id);
            return this.models.get(id);
        }

        this.builder.get().begin();
        Model model;
        try {
            builder.accept(this.builder.get());
        } finally {
            model = this.builder.get().end();
        }
        this.models.put(id, model);
        return model;
    }

    /**
     * Generates a model using the provided builder function and associates it with the specified NamespaceID.
     *
     * @param id The NamespaceID to associate the generated model with.
     * @param builder A function that takes a ModelBuilder and returns a Model.
     * @return The generated Model instance.
     */
    public Model generateModel(NamespaceID id, Function<ModelBuilder, Model> builder) {
        Model model = builder.apply(this.builder.get());

        this.models.put(id, model);

        return model;
    }

    /**
     * Retrieves the model associated with the specified NamespaceID.
     *
     * @param id the NamespaceID representing the specific model to retrieve
     * @return the Model associated with the specified NamespaceID, or loads and returns it if not already loaded
     */
    public Model getModel(NamespaceID id) {
        if (this.models.containsKey(id)) {
            return this.models.get(id);
        }

        return this.loadModel(id);
    }

    /**
     * Unloads and disposes of a model associated with the given {@code NamespaceID}.
     *
     * @param id the {@code NamespaceID} representing the specific model to unload.
     * @return {@code true} if the model was successfully unloaded and disposed, {@code false} otherwise.
     */
    public boolean unloadModel(NamespaceID id) {
        Model removed = this.models.remove(id);
        if (removed != null) {
            try {
                removed.dispose();
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Reloads all models managed by this ModelManager.
     * This involves clearing the currently loaded models and then reloading them
     * based on their stored NamespaceIDs.
     */
    public void reload() {
        Set<NamespaceID> namespaceIDS = Set.copyOf(this.models.keySet());
        this.models.clear();

        for (NamespaceID id : namespaceIDS) {
            this.loadModel(id);
        }
    }

    /**
     * Releases all resources associated with the current models managed by this ModelManager.
     * This method iterates through the set of model identifiers and unloads each model,
     * effectively clearing the internal storage of models.
     */
    public void dispose() {
        for (NamespaceID id : this.models.keySet()) {
            unloadModel(id);
        }
    }

    /**
     * Adds a Model associated with the specified NamespaceID to the collection.
     *
     * @param resourceId the NamespaceID to associate with the model
     * @param model the Model to be added
     */
    public void add(NamespaceID resourceId, Model model) {
        this.models.put(resourceId, model);
    }
}
