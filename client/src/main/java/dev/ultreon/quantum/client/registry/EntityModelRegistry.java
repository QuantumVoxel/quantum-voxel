package dev.ultreon.quantum.client.registry;

import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientRegistrationEvents;
import dev.ultreon.quantum.client.model.blockbench.BBModelLoader;
import dev.ultreon.quantum.client.model.entity.EntityModel;
import dev.ultreon.quantum.client.resources.ContextAwareReloadable;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityModelRegistry implements ContextAwareReloadable, Disposable {
    private final Map<EntityType<?>, EntityModel<?>> registry = new HashMap<>();
    private final Map<EntityType<?>, Identifier> g3dRegistry = new HashMap<>();
    private final Map<EntityType<?>, Identifier> gltfRegistry = new HashMap<>();
    private final Map<EntityType<?>, Identifier> bbModelRegistry = new HashMap<>();
    private final Map<EntityType<?>, Model> finishedRegistry = new HashMap<>();
    final ModelLoader<ModelLoader.ModelParameters> modelLoader;
    private final QuantumClient client;

    public EntityModelRegistry(ModelLoader<ModelLoader.ModelParameters> modelLoader, QuantumClient client) {
        this.modelLoader = modelLoader;
        this.client = client;
    }

    public <T extends Entity> void register(EntityType<@NotNull T> entityType, EntityModel<T> model) {
        this.registry.put(entityType, model);
    }

    public <T extends Entity> void registerG3d(EntityType<@NotNull T> entityType, Identifier id) {
        this.g3dRegistry.put(entityType, id);
    }

    /**
     * @param entityType
     * @param id
     * @param <T>
     * @deprecated Use {@link #registerBBModel(EntityType, Identifier)} or {@link #registerG3d(EntityType, Identifier)} instead.
     *             This method breaks models when having multiple in a scene and will be removed in the future.
     */
    @Deprecated(forRemoval = true)
    public <T extends Entity> void registerGltf(EntityType<@NotNull T> entityType, Identifier id) {
        this.gltfRegistry.put(entityType, id);
    }

    public <T extends Entity> void registerBBModel(EntityType<@NotNull T> player, Identifier player1) {
        this.bbModelRegistry.put(player, player1);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Entity> EntityModel<T> get(EntityType<@NotNull T> entityType) {
        return (EntityModel<T>) this.registry.get(entityType);
    }

    public void registerFinished(EntityType<?> value, Model finished) {
        this.finishedRegistry.put(value, finished);
    }

    public Model getFinished(EntityType<?> value) {
        return this.finishedRegistry.get(value);
    }

    public Collection<Model> getAll() {
        return this.finishedRegistry.values();
    }

    public Map<EntityType<?>, Model> getRegistry() {
        return Collections.unmodifiableMap(this.finishedRegistry);
    }

    @Override
    public void reload(ResourceManager resourceManager, ReloadContext context) {
        this.registry.clear();
        this.finishedRegistry.clear();

        for (Map.Entry<EntityType<?>, Identifier> e : this.g3dRegistry.entrySet()) {
            Identifier id = e.getValue();
            Model model = QuantumClient.invokeAndWait(() -> this.modelLoader.loadModel(new ResourceFileHandle(id.mapPath(path -> "models/entity/" + path + ".g3dj")),fileName -> client.getTextureManager().getTexture(new Identifier(fileName).mapPath(path -> {
                if (path.startsWith("models/entity/")) {
                    path = path.substring("models/entity/".length());
                }
                return "textures/entity/" + path;
            }))));
            this.finishedRegistry.put(e.getKey(), model);
        }

        for (Map.Entry<EntityType<?>, Identifier> e : this.bbModelRegistry.entrySet()) {
            Identifier id = e.getValue();
            Model model = blockBenchModel(id.mapPath(path -> "entity/" + path));
            this.finishedRegistry.put(e.getKey(), model);
        }

        // Call the onRegister method of the factory in ENTITY_MODELS
        ClientRegistrationEvents.ENTITY_MODELS.factory().onRegister();
    }

    private Model blockBenchModel(Identifier id) {
        return new BBModelLoader(id.mapPath(path -> "models/" + path + ".bbmodel")).createModel();
    }

    @Override
    public void dispose() {
        for (Model model : this.finishedRegistry.values()) {
            model.dispose();
        }
    }
}
