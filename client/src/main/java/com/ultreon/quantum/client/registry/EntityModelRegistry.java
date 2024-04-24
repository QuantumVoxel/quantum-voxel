package com.ultreon.quantum.client.registry;

import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.api.events.ClientRegistrationEvents;
import com.ultreon.quantum.client.model.blockbench.BlockBenchModelImporter;
import com.ultreon.quantum.client.model.entity.EntityModel;
import com.ultreon.quantum.client.resources.ContextAwareReloadable;
import com.ultreon.quantum.client.resources.ResourceFileHandle;
import com.ultreon.quantum.resources.ReloadContext;
import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.entity.EntityType;
import com.ultreon.quantum.entity.EntityTypes;
import com.ultreon.quantum.resources.ResourceManager;
import com.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityModelRegistry implements ContextAwareReloadable {
    private final Map<EntityType<?>, EntityModel<?>> registry = new HashMap<>();
    private final Map<EntityType<?>, Identifier> g3dRegistry = new HashMap<>();
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

//        Model somethingModel = this.blockBenchModel(new Identifier("entity/something"));
//        this.registerFinished(EntityTypes.SOMETHING, somethingModel);

        // Call the onRegister method of the factory in ENTITY_MODELS
        ClientRegistrationEvents.ENTITY_MODELS.factory().onRegister();
    }

    private Model blockBenchModel(Identifier id) {
        return new BlockBenchModelImporter(id.mapPath(path -> "models/" + path + ".bbmodel")).createModel();
    }
}
