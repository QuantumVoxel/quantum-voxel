package dev.ultreon.quantum.client.model.item;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.atlas.TextureStitcher;
import dev.ultreon.quantum.client.management.TextureAtlasManager;
import dev.ultreon.quantum.client.model.block.CubeModel;
import dev.ultreon.quantum.client.model.model.JsonModel;
import dev.ultreon.quantum.client.model.model.JsonModelLoader;
import dev.ultreon.quantum.client.resources.ContextAwareReloadable;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Suppliers;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class ItemModelRegistry implements ContextAwareReloadable {
    private static final ItemModelRegistry INSTANCE = new ItemModelRegistry();
    private final Map<Item, Supplier<ItemModel>> customRegistry = new LinkedHashMap<>(CommonConstants.MAX_BLOCK_REGISTRY);
    private final Set<NamespaceID> TEXTURES = new HashSet<>();
    private Item loadingItem = null;
    private final Map<Item, ItemModel> baked = new HashMap<>();

    public ItemModelRegistry() {
        this.TEXTURES.add(new NamespaceID("misc/breaking1"));
        this.TEXTURES.add(new NamespaceID("misc/breaking2"));
        this.TEXTURES.add(new NamespaceID("misc/breaking3"));
        this.TEXTURES.add(new NamespaceID("misc/breaking4"));
        this.TEXTURES.add(new NamespaceID("misc/breaking5"));
        this.TEXTURES.add(new NamespaceID("misc/breaking6"));
    }

    public static ItemModelRegistry get() {
        return INSTANCE;
    }

    public @Nullable ItemModel get(Item item) {
        return baked.get(item);
    }

    public void register(Item item, ItemModel model) {
        this.customRegistry.putIfAbsent(item, () -> model);
    }

    public void register(Item item, CubeModel model) {
        this.customRegistry.putIfAbsent(item, () -> JsonModel.cubeOf(model));
    }

    public void registerCustom(Item item, Supplier<ItemModel> model) {
        this.customRegistry.put(item, Suppliers.memoize(model));
    }

    public void register(Supplier<Item> item, Supplier<CubeModel> model) {
        this.customRegistry.put(item.get(), Suppliers.memoize(() -> JsonModel.cubeOf(model.get())));
    }

    public TextureAtlas stitch(TextureManager textureManager) {
        TextureStitcher stitcher = new TextureStitcher(TextureAtlasManager.BLOCK_ATLAS_ID);

        final int breakStages = 6;

        for (int i = 0; i < breakStages; i++) {
            NamespaceID texId = new NamespaceID("textures/misc/breaking" + (i + 1) + ".png");
            Pixmap tex = new Pixmap(QuantumClient.resource(texId));
            stitcher.add(texId, tex);
            tex.dispose();
        }
        NamespaceID texId = NamespaceID.of("textures/blocks/error.png");
        Pixmap tex = new Pixmap(QuantumClient.resource(texId));
        stitcher.add(NamespaceID.of("blocks/error"), tex);
        tex.dispose();

        for (NamespaceID texture : this.TEXTURES) {
            FileHandle emissiveRes = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".emissive.png"));
            FileHandle normalRes = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".normal.png"));
            FileHandle specularRes = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".specular.png"));
            FileHandle reflectiveRes = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".reflective.png"));

            FileHandle resource = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".png"));
            if (!resource.exists()) continue;

            Pixmap diffuse = new Pixmap(resource);
            Pixmap emissive = emissiveRes.exists() ? new Pixmap(emissiveRes) : null;
            Pixmap normal = normalRes.exists() ? new Pixmap(normalRes) : null;
            Pixmap specular = specularRes.exists() ? new Pixmap(specularRes) : null;
            Pixmap reflective = reflectiveRes.exists() ? new Pixmap(reflectiveRes) : null;
            if (emissive != null || normal != null || specular != null || reflective != null) {
                stitcher.add(texture, diffuse, emissive, normal, specular, reflective);
            } else {
                stitcher.add(texture, diffuse);
            }
        }

        return stitcher.stitch();
    }

    public void bakeJsonModels(QuantumClient client) {
        for (var entry : customRegistry.entrySet()) {
            ItemModel model = entry.getValue().get();
            if (model == null) {
                QuantumClient.LOGGER.error("Failed to load item model for {}: {}", entry.getKey().getId(), entry.getKey());
                continue;
            }
            QuantumClient.invokeAndWait(() -> model.load(client));
            this.baked.put(entry.getKey(), model);
        }
    }

    public void load(JsonModelLoader loader) {
        for (Item value : Registries.ITEM.values()) {
            if (value == Items.AIR) continue;

            this.loadingItem = value;
            try {
                if (customRegistry.containsKey(value)) continue;
                JsonModel load = loader.load(value);
                customRegistry.computeIfAbsent(value, key -> () -> Objects.requireNonNullElseGet(load, () -> new FlatItemModel(value)));
            } catch (Exception e) {
                QuantumClient.LOGGER.error("Failed to load item model for {}: {}", value.getId(), e.toString());
            }
            this.loadingItem = null;
        }

        for (var entry : customRegistry.entrySet()) {
            if (entry.getKey() == Items.AIR) continue;
            this.loadingItem = entry.getKey();
            try {
                ItemModel model = entry.getValue().get();

                if (entry.getValue() != null) {
                    TEXTURES.addAll(model.getAllTextures());
                }
            } catch (Exception e) {
                QuantumClient.LOGGER.error("Failed to load item model for {}: {}", entry.getKey().getId(), e.toString());
            }
            this.loadingItem = null;
        }
    }

    @Override
    public void reload(ResourceManager resourceManager, ReloadContext context) {
        QuantumClient client = QuantumClient.get();
        context.submit(() -> {
            try {
                this.load(client.j5ModelLoader);

                QuantumClient.LOGGER.info("Baking item models");
                this.bakeJsonModels(client);
            } catch (Exception e) {
                QuantumClient.LOGGER.error("Failed to reload item models", e);
                CrashLog crashLog = new CrashLog("Failed to load item models", e);
                if (loadingItem != null) {
                    CrashCategory model = new CrashCategory("Model");
                    model.add("item", this.loadingItem.getId());
                    model.add("Location", this.loadingItem.getId().mapPath(path -> "models/items/" + path + ".quant"));
                    crashLog.addCategory(model);
                }
                QuantumClient.crash(crashLog);
            } catch (ApplicationCrash e) {
                QuantumClient.crash(e.getCrashLog());
            }
        });
    }
}
