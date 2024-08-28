package dev.ultreon.quantum.client.registry;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.utils.BaseJsonReader;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
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
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityModelRegistry implements ContextAwareReloadable, Disposable {
    public static final G3dModelLoader MODEL_LOADER = new G3dModelLoader(new JsonReader());
    private final Map<EntityType<?>, EntityModel<?>> registry = new HashMap<>();
    private final Map<EntityType<?>, NamespaceID> g3dRegistry = new HashMap<>();
    private final Map<EntityType<?>, NamespaceID> gltfRegistry = new HashMap<>();
    private final Map<EntityType<?>, NamespaceID> bbModelRegistry = new HashMap<>();
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

    public <T extends Entity> void registerG3d(EntityType<@NotNull T> entityType, NamespaceID id) {
        this.g3dRegistry.put(entityType, id);
    }

    /**
     * @param entityType
     * @param id
     * @param <T>
     * @deprecated Use {@link #registerBBModel(EntityType, NamespaceID)} or {@link #registerG3d(EntityType, NamespaceID)} instead.
     *             This method breaks models when having multiple in a scene and will be removed in the future.
     */
    @Deprecated(forRemoval = true)
    public <T extends Entity> void registerGltf(EntityType<@NotNull T> entityType, NamespaceID id) {
        this.gltfRegistry.put(entityType, id);
    }

    public <T extends Entity> void registerBBModel(EntityType<@NotNull T> player, NamespaceID player1) {
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

        for (Map.Entry<EntityType<?>, NamespaceID> e : this.g3dRegistry.entrySet()) {
            NamespaceID id = e.getValue();
            Model model = QuantumClient.invokeAndWait(() -> this.modelLoader.loadModel(new ResourceFileHandle(id.mapPath(path -> "models/entity/" + path + ".g3dj")),fileName -> client.getTextureManager().getTexture(new NamespaceID(fileName).mapPath(path -> {
                if (path.startsWith("models/entity/")) {
                    path = path.substring("models/entity/".length());
                }
                return "textures/entity/" + path;
            }))));
            this.finishedRegistry.put(e.getKey(), model);
        }

        for (Map.Entry<EntityType<?>, NamespaceID> e : this.bbModelRegistry.entrySet()) {
            NamespaceID id = e.getValue();
            NamespaceID mappedId = id.mapPath(path -> "models/entity/" + path + ".g3dj");

            Model model = QuantumClient.invokeAndWait(() -> MODEL_LOADER.loadModel(new ResourceFileHandle(mappedId), fileName -> client.getTextureManager().getTexture(new NamespaceID(fileName).mapPath(path -> {
                if (path.startsWith("models/entity/")) {
                    path = path.substring("models/entity/".length());
                } else if (path.startsWith(mappedId.toString())) {
                    path = path.substring(mappedId.toString().length());
                } else {
                    String string = mappedId.toString();
                    int len = string.length() - path.length() - ".g3dj".length();
                    if (path.startsWith(string.substring(0, len))) {
                        path = path.substring(len);
                    }
                }

                return "textures/entity/" + path;
            }))));
            this.finishedRegistry.put(e.getKey(), model);
        }

        // Call the onRegister method of the factory in ENTITY_MODELS
        ClientRegistrationEvents.ENTITY_MODELS.factory().onRegister();
    }

    private Model blockBenchModel(NamespaceID id) {
        NamespaceID mappedId = id.mapPath(path -> "models/" + path + ".g3dj");

        return MODEL_LOADER.loadModel(new ResourceFileHandle(mappedId), fileName -> client.getTextureManager().getTexture(new NamespaceID(fileName).mapPath(path -> {
            if (path.startsWith("models/")) {
                path = path.substring("models/".length());
            } else if (path.startsWith(mappedId.toString())) {
                path = path.substring(mappedId.toString().length());
            } else {
                String string = mappedId.toString();
                int len = string.length() - path.length() - ".g3dj".length();
                if (path.startsWith(string.substring(0, len))) {
                    path = path.substring(len);
                }
            }

            return "textures/" + path;
        })));
    }

    @Override
    public void dispose() {
        for (Model model : this.finishedRegistry.values()) {
            model.dispose();
        }
    }
}
