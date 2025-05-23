package dev.ultreon.quantum.client.registry;

import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientRegistrationEvents;
import dev.ultreon.quantum.client.model.entity.EntityModel;
import dev.ultreon.quantum.client.resources.ContextAwareReloadable;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static dev.ultreon.quantum.CommonConstants.LOGGER;

/**
 * Represents the registry of entity models.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class EntityModelRegistry implements ContextAwareReloadable, Disposable {
    /**
     * The model loader for G3D models.
     */ 
    public static final G3dModelLoader MODEL_LOADER = new G3dModelLoader(new JsonReader());
    /**
     * The registry of entity models.
     */
    private final Map<EntityType<?>, EntityModel<?>> registry = new HashMap<>();
    /**
     * The registry of G3D models.
     */
    private final Map<EntityType<?>, NamespaceID> g3dRegistry = new HashMap<>();
    /**
     * The registry of finished models.
     */
    private final Map<EntityType<?>, Model> finishedRegistry = new HashMap<>();
    final ModelLoader<ModelLoader.ModelParameters> modelLoader;
    private final QuantumClient client;

    /**
     * Constructs a new entity model registry.
     *
     * @param modelLoader the model loader.
     * @param client the client.
     */
    public EntityModelRegistry(ModelLoader<ModelLoader.ModelParameters> modelLoader, QuantumClient client) {
        this.modelLoader = modelLoader;
        this.client = client;
    }

    /**
     * Registers an entity model.
     *
     * @param entityType the entity type.
     * @param model the model.
     */
    public <T extends Entity> void register(EntityType<@NotNull T> entityType, EntityModel<T> model) {
        this.registry.put(entityType, model);
    }

    /**
     * Registers a G3D model.
     *
     * @param entityType the entity type.
     * @param id the id.
     */
    public <T extends Entity> void registerG3d(EntityType<@NotNull T> entityType, NamespaceID id) {
        this.g3dRegistry.put(entityType, id);
    }

    /**
     * Gets an entity model.
     *
     * @param entityType the entity type.
     * @return the entity model.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Entity> EntityModel<T> get(EntityType<@NotNull T> entityType) {
        return (EntityModel<T>) this.registry.get(entityType);
    }

    /**
     * Registers a finished model.
     *
     * @param value the entity type.
     * @param finished the finished model.
     */
    public void registerFinished(EntityType<?> value, Model finished) {
        this.finishedRegistry.put(value, finished);
    }

    /**
     * Gets a finished model.
     *
     * @param value the entity type.
     * @return the finished model.
     */
    public Model getFinished(EntityType<?> value) {
        return this.finishedRegistry.get(value);
    }

    /**
     * Gets all finished models.
     *
     * @return the finished models.
     */
    public Collection<Model> getAll() {
        return this.finishedRegistry.values();
    }

    /**
     * Gets the registry of finished models.
     *
     * @return the registry of finished models.
     */
    public Map<EntityType<?>, Model> getRegistry() {
        return Collections.unmodifiableMap(this.finishedRegistry);
    }

    /**
     * Reloads the entity model registry.
     *
     * @param resourceManager the resource manager.
     * @param context the reload context.
     */
    @Override
    public void reload(ResourceManager resourceManager, ReloadContext context) {
        this.registry.clear();
        this.finishedRegistry.clear();

        for (Map.Entry<EntityType<?>, NamespaceID> e : this.g3dRegistry.entrySet()) {
            NamespaceID id = e.getValue();
            NamespaceID mappedId = id.mapPath(path -> "models/entity/" + path + ".g3dj");

            Model model = QuantumClient.invokeAndWait(() -> MODEL_LOADER.loadModel(QuantumClient.resource(mappedId), fileName -> client.getTextureManager().getTexture(new NamespaceID(fileName).mapPath(path -> {
                if (path.startsWith("models/entity/")) {
                    path = path.substring("models/entity/".length());
                } else if (path.startsWith(mappedId.toString())) {
                    path = path.substring(mappedId.toString().length());
                } else {
                    String string = mappedId.toString();
                    System.out.println(string);
                    System.out.println(path);
                    int len = string.length() - path.length() - ".g3dj".length();
                    System.out.println(len);
                    if (path.startsWith(string.substring(0, len))) {
                        path = path.substring(len);
                    }
                }

                return "textures/entity/" + path;
            }))));
            LOGGER.warn("TOOD: Implement model loader for: {}", mappedId);
            this.finishedRegistry.put(e.getKey(), model);
        }

        // Call the onRegister method of the factory in ENTITY_MODELS
        ClientRegistrationEvents.ENTITY_MODELS.factory().onRegister();
    }
    
    /**
     * Disposes of the entity model registry.
     */
    @Override
    public void dispose() {
        for (Model model : this.finishedRegistry.values()) {
            model.dispose();
        }
    }
}
