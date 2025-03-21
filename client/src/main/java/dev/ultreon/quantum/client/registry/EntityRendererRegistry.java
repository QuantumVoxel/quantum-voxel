package dev.ultreon.quantum.client.registry;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.utils.Disposable;

import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.entity.EntityModel;
import dev.ultreon.quantum.client.model.entity.renderer.EntityRenderer;
import dev.ultreon.quantum.client.resources.ContextAwareReloadable;
import dev.ultreon.quantum.collection.OrderedMap;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

import dev.ultreon.quantum.registry.RegistryKey;

/**
 * Represents the registry of entity renderers.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class EntityRendererRegistry implements ContextAwareReloadable, Disposable {
    /**
     * The registry of entity renderers.
     */
    public static final Map<EntityType<?>, BiFunction<EntityModel<?>, Model, EntityRenderer<?>>> REGISTRY = new HashMap<>();
    /**
     * The registry of finished entity renderers.
     */
    public final Map<EntityType<?>, EntityRenderer<?>> finishedRegistry = new OrderedMap<>();
    private final EntityModelRegistry modelManager;

    /**
     * Constructs a new entity renderer registry.
     *
     * @param modelManager the model manager.
     */
    public EntityRendererRegistry(EntityModelRegistry modelManager) {
        this.modelManager = modelManager;
    }

    /**
     * Registers an entity renderer.
     *
     * @param entityType the entity type.
     * @param factory the factory.
     * @param <T> the entity type.
     * @param <M> the entity model type.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity, M extends EntityModel<T>> void register(EntityType<@NotNull T> entityType, BiFunction<M, Model, EntityRenderer<T>> factory) {
        REGISTRY.put(entityType, (entityModel, model) -> factory.apply((M) entityModel, model));
    }

    /**
     * Loads the entity renderer registry.
     */
    public void load() {
    }

    /**
     * Gets an entity renderer.
     *
     * @param entityType the entity type.
     * @return the entity renderer.
     */
    public <T extends Entity> EntityRenderer<?> get(EntityType<@NotNull T> entityType) {
        return this.finishedRegistry.get(entityType);
    }

    /**
     * Reloads the entity renderer registry.
     *
     * @param resourceManager the resource manager.
     * @param context the reload context.
     */
    @Override
    public void reload(ResourceManager resourceManager, ReloadContext context) {
        for (EntityRenderer<?> renderer : this.finishedRegistry.values()) {
            context.submit(renderer::dispose);
        }

        this.finishedRegistry.clear();

        // Register entity renderers
        for (var entry : REGISTRY.entrySet()) {
            EntityType<?> entityType = entry.getKey();
            Model finished = modelManager.getFinished(entityType);

            EntityRenderer<?> renderer = entry.getValue().apply(this.modelManager.get(entityType), finished);
            this.finishedRegistry.put(entityType, renderer);
        }

        // Register entity renderers
        for (Map.Entry<EntityType<?>, BiFunction<EntityModel<?>, Model, EntityRenderer<?>>> e : EntityRendererRegistry.REGISTRY.entrySet()) {
            QuantumClient.LOGGER.debug("Registering renderer for entity {}", e.getKey().getId());
            if (this.finishedRegistry.containsKey(e.getKey())) {
                QuantumClient.LOGGER.warn("Renderer for entity {} is already registered", e.getKey().getId());
                continue;
            }
            if (this.modelManager.get(e.getKey()) == null) {
                QuantumClient.LOGGER.warn("Model for entity {} is null", e.getKey().getId());
                continue;
            }
            if (this.modelManager.getFinished(e.getKey()) == null) {
                QuantumClient.LOGGER.warn("Finished model for entity {} is null", e.getKey().getId());
                continue;
            }
            this.finishedRegistry.put(e.getKey(), e.getValue().apply(this.modelManager.get(e.getKey()), this.modelManager.getFinished(e.getKey())));
        }

        // Iterate through all entity types and register their models and renderers
        for (ObjectMap.Entry<RegistryKey<EntityType<?>>, EntityType<?>> e : Registries.ENTITY_TYPE.entries()) {
            // Get the necessary data
            EntityType<?> type = e.value;
            EntityRenderer<?> renderer = this.get(type);
            EntityModel<?> entityModel = this.modelManager.get(type);
            NamespaceID key = e.key.id();
            FileHandle handle = QuantumClient.resource(key.mapPath(path -> "models/entity/" + path + ".g3dj"));
            // Load and register the model if it exists
            if (handle.exists()) {
                Model model = QuantumClient.invokeAndWait(() -> this.modelManager.modelLoader.loadModel(handle, fileName -> {
                    String filePath = fileName.substring(("assets/" + key.getDomain() + "/models/entity/").length());
                    return new Texture(QuantumClient.resource(key.mapPath(path -> "textures/entity/" + filePath)));
                }));
                if (model == null)
                    throw new RuntimeException("Failed to load entity model: " + key.mapPath(path -> "models/entity/" + path + ".g3dj"));
                // Set blending and alpha test attributes for the model materials
                model.materials.forEach(modelModel -> {
                    modelModel.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
                    modelModel.set(FloatAttribute.createAlphaTest(0.5f));
                });
                this.modelManager.registerFinished(type, model);
            } else {
                // If the model does not exist, use the entity model and renderer
                if (entityModel == null) {
                    QuantumClient.LOGGER.warn("Model not found for entity {}", type.getId());
                    continue;
                }

                if (renderer == null) {
                    QuantumClient.LOGGER.warn("Renderer not found for entity {}", type.getId());
                    continue;
                }

                this.modelManager.registerFinished(type, entityModel.finish(renderer.getTextures()));
            }
        }
    }

    /**
     * Disposes of the entity renderer registry.
     */
    @Override
    public void dispose() {
        for (EntityRenderer<?> renderer : this.finishedRegistry.values()) {
            renderer.dispose();
        }
    }
}
