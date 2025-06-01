package dev.ultreon.quantum.client.model.block;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import dev.ultreon.libs.collections.v0.tables.Table;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.block.property.BlockDataEntry;
import dev.ultreon.quantum.block.property.StatePropertyKey;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.atlas.TextureStitcher;
import dev.ultreon.quantum.client.management.TextureAtlasManager;
import dev.ultreon.quantum.client.model.model.JsonModel;
import dev.ultreon.quantum.client.model.model.JsonModelLoader;
import dev.ultreon.quantum.client.resources.ContextAwareReloadable;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.SanityCheckException;
import dev.ultreon.quantum.util.Suppliers;

import java.util.*;
import java.util.function.Supplier;

public class BlockModelRegistry implements ContextAwareReloadable {
    private static final BlockModelRegistry INSTANCE = new BlockModelRegistry();
    private final Map<Block, Map<BlockState, Supplier<BlockModel>>> customRegistry = new LinkedHashMap<>(CommonConstants.MAX_BLOCK_REGISTRY);
    private final Set<NamespaceID> TEXTURES = new HashSet<>();
    private Block loadingBlock = null;

    public BlockModelRegistry() {
        this.TEXTURES.add(new NamespaceID("misc/breaking1"));
        this.TEXTURES.add(new NamespaceID("misc/breaking2"));
        this.TEXTURES.add(new NamespaceID("misc/breaking3"));
        this.TEXTURES.add(new NamespaceID("misc/breaking4"));
        this.TEXTURES.add(new NamespaceID("misc/breaking5"));
        this.TEXTURES.add(new NamespaceID("misc/breaking6"));
    }

    public static BlockModelRegistry get() {
        return INSTANCE;
    }

    public BlockModel get(BlockState meta) {
        for (Map.Entry<BlockState, Supplier<BlockModel>> p : this.customRegistry.getOrDefault(meta.getBlock(), Collections.emptyMap()).entrySet()) {
            if (p.getKey().equals(meta)) {
                return p.getValue().get();
            }
        }
        return null;
    }

    public void register(Block block, BlockState predicate, CubeModel model) {
        this.customRegistry.computeIfAbsent(block, key -> new HashMap<>()).put(predicate, () -> JsonModel.cubeOf(model));
    }

    public void register(Block block, CubeModel model) {
        this.customRegistry.computeIfAbsent(block, key -> new HashMap<>()).put(block.getDefaultState(), () -> JsonModel.cubeOf(model));
    }

    public void registerCustom(Block block, BlockState predicate, Supplier<BlockModel> model) {
        this.customRegistry.computeIfAbsent(block, key -> new HashMap<>()).put(predicate, Suppliers.memoize(model));
    }

    public void register(Supplier<Block> block, BlockState predicate, Supplier<CubeModel> model) {
        this.customRegistry.computeIfAbsent(block.get(), key -> new HashMap<>()).put(predicate, Suppliers.memoize(() -> JsonModel.cubeOf(model.get())));
    }

    public void registerDefault(Block block) {
        NamespaceID key = Registries.BLOCK.getId(block);
        if (key == null) throw new SanityCheckException("Fabricated block!");
        this.register(block, block.getDefaultState(), CubeModel.of(key.mapPath(path -> "blocks/" + path), key.mapPath(path -> "blocks/" + path)));
    }

    public void registerDefault(Supplier<Block> block) {
        Block blk = block.get();
        this.register(block, blk.getDefaultState(), Suppliers.memoize(() -> {
            NamespaceID key = Registries.BLOCK.getId(blk);
            if (key == null) {
                throw new IllegalStateException("Block not registered: " + blk);
            }
            return CubeModel.of(key.mapPath(path -> "blocks/" + path), key.mapPath(path -> "blocks/" + path));
        }));
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
            Map<BlockState, Supplier<BlockModel>> models = new HashMap<>();
            for (var pair : entry.getValue().entrySet()) {
                BlockModel model = pair.getValue().get();
                if (model == null) {
                    QuantumClient.LOGGER.error("Failed to load block model for {}: {}", entry.getKey().getId(), pair.getKey());
                    continue;
                }
                QuantumClient.invokeAndWait(() -> model.load(client));
                models.put(pair.getKey(), Suppliers.memoize(() -> model));
            }
        }
    }

    public void load(JsonModelLoader loader) {
        for (Block value : Registries.BLOCK.values()) {
            if (value == Blocks.AIR) continue;

            this.loadingBlock = value;
            try {
                if (customRegistry.containsKey(value)) continue;
                JsonModel load = loader.load(value);
                if (load != null) {
                    customRegistry.computeIfAbsent(value, key -> new HashMap<>()).put(value.getDefaultState(), () -> load);

                    Table<String, BlockDataEntry<?>, JsonModel> overrides = load.getOverrides();
                    if (overrides == null) continue;
                    overrides.cellSet().forEach((cell) -> customRegistry.computeIfAbsent(value, key -> new HashMap<>()).put(value.getDefaultState().with((StatePropertyKey) value.getDefinition().keyByName(cell.getRow()), cell.getColumn().value), cell::getValue));
                } else if (value.doesRender()) {
                    this.registerDefault(value);
                }
            } catch (Exception e) {
                QuantumClient.LOGGER.error("Failed to load block model for {}: {}", value.getId(), e.toString());
            }
            this.loadingBlock = null;
        }

        for (var entry : customRegistry.entrySet()) {
            if (entry.getKey() == Blocks.AIR) continue;
            this.loadingBlock = entry.getKey();
            try {
                Map<BlockState, Supplier<BlockModel>> models = new HashMap<>();
                for (var pair : entry.getValue().entrySet()) {
                    BlockModel model = pair.getValue().get();
                    models.put(pair.getKey(), Suppliers.memoize(() -> model));

                    if (entry.getValue() != null) {
                        TEXTURES.addAll(model.getAllTextures());
                    }
                }
            } catch (Exception e) {
                QuantumClient.LOGGER.error("Failed to load block model for {}: {}", entry.getKey().getId(), e.toString());
            }
            this.loadingBlock = null;
        }
    }

    @Override
    public void reload(ResourceManager resourceManager, ReloadContext context) {
        QuantumClient client = QuantumClient.get();
        context.submit(() -> {
            try {
                this.load(client.j5ModelLoader);

                QuantumClient.LOGGER.info("Baking models");
                this.bakeJsonModels(client);
            } catch (Exception e) {
                QuantumClient.LOGGER.error("Failed to reload block models", e);
                CrashLog crashLog = new CrashLog("Failed to load block models", e);
                CrashCategory model = new CrashCategory("Model");
                model.add("Block", this.loadingBlock.getId());
                model.add("Location", this.loadingBlock.getId().mapPath(path -> "models/blocks/" + path + ".quant"));
                crashLog.addCategory(model);
                QuantumClient.crash(crashLog);
            } catch (ApplicationCrash e) {
                QuantumClient.crash(e.getCrashLog());
            }
        });
    }
}
