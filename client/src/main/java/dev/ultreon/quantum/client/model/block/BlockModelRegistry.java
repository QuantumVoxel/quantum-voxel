package dev.ultreon.quantum.client.model.block;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.atlas.TextureStitcher;
import dev.ultreon.quantum.client.model.model.Json5Model;
import dev.ultreon.quantum.client.model.model.Json5ModelLoader;
import dev.ultreon.quantum.client.resources.ContextAwareReloadable;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Suppliers;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BlockModelRegistry implements ContextAwareReloadable {
    private static final BlockModelRegistry INSTANCE = new BlockModelRegistry();
    private final Map<Block, List<Pair<Predicate<BlockState>, Supplier<CubeModel>>>> registry = new LinkedHashMap<>(CommonConstants.MAX_BLOCK_REGISTRY);
    private final Map<Block, List<Pair<Predicate<BlockState>, Supplier<BlockModel>>>> customRegistry = new LinkedHashMap<>(CommonConstants.MAX_BLOCK_REGISTRY);
    private final Set<NamespaceID> TEXTURES = new HashSet<>();
    private final Map<Block, List<Pair<Predicate<BlockState>, Supplier<BlockModel>>>> finishedRegistry = new LinkedHashMap<>(CommonConstants.MAX_BLOCK_REGISTRY);
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
        for (Pair<Predicate<BlockState>, Supplier<BlockModel>> p : this.customRegistry.getOrDefault(meta.getBlock(), new ArrayList<>())) {
            if (p.getFirst().test(meta)) {
                return p.getSecond().get();
            }
        }
        return null;
    }

    public void register(Block block, Predicate<BlockState> predicate, CubeModel model) {
        this.registry.computeIfAbsent(block, key -> new ArrayList<>()).add(new Pair<>(predicate, () -> model));
    }

    public void registerCustom(Block block, Predicate<BlockState> predicate, Supplier<BlockModel> model) {
        this.customRegistry.computeIfAbsent(block, key -> new ArrayList<>()).add(new Pair<>(predicate, Suppliers.memoize(model)));
    }

    public void register(Supplier<Block> block, Predicate<BlockState> predicate, Supplier<CubeModel> model) {
        this.registry.computeIfAbsent(block.get(), key -> new ArrayList<>()).add(new Pair<>(predicate, Suppliers.memoize(model)));
    }

    public void registerDefault(Block block) {
        NamespaceID key = Registries.BLOCK.getId(block);
        Preconditions.checkNotNull(key, "Block is not registered");
        this.register(block, meta -> true, CubeModel.of(key.mapPath(path -> "blocks/" + path), key.mapPath(path -> "blocks/" + path)));
    }

    public void registerDefault(Supplier<Block> block) {
        this.register(block, meta -> true, Suppliers.memoize(() -> {
            NamespaceID key = Registries.BLOCK.getId(block.get());
            Preconditions.checkNotNull(key, "Block is not registered");
            return CubeModel.of(key.mapPath(path -> "blocks/" + path), key.mapPath(path -> "blocks/" + path));
        }));
    }

    public TextureAtlas stitch(TextureManager textureManager) {
        TextureStitcher stitcher = new TextureStitcher(QuantumClient.id("block"));

        this.registry.values().stream().flatMap(Collection::stream).map(pair -> pair.getSecond().get().all()).forEach(this.TEXTURES::addAll);

        final int breakStages = 6;

        for (int i = 0; i < breakStages; i++) {
            NamespaceID texId = new NamespaceID("textures/misc/breaking" + (i + 1) + ".png");
            Pixmap tex = new Pixmap(QuantumClient.resource(texId));
            stitcher.add(texId, tex);
            tex.dispose();
        }

        for (NamespaceID texture : this.TEXTURES) {
            FileHandle emissiveRes = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".emissive.png"));
            FileHandle normalRes = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".normal.png"));
            FileHandle specularRes = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".specular.png"));
            FileHandle reflectiveRes = QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".reflective.png"));

            Pixmap diffuse = new Pixmap(QuantumClient.resource(texture.mapPath(path -> "textures/" + path + ".png")));
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

    public BakedModelRegistry bake(TextureAtlas atlas) {
        ImmutableMap.Builder<Block, List<Pair<Predicate<BlockState>, BakedCubeModel>>> bakedModels = new ImmutableMap.Builder<>();
        this.registry.forEach((block, models) -> {
            List<Pair<Predicate<BlockState>, BakedCubeModel>> modelList = new ArrayList<>();
            for (var modelPair : models) {
                var predicate = modelPair.getFirst();
                var model = modelPair.getSecond();
                BakedCubeModel baked = model.get().bake(model.get().resourceId(), atlas);

                modelList.add(new Pair<>(predicate, baked));
            }
            bakedModels.put(block, modelList);
        });

        return new BakedModelRegistry(atlas, bakedModels.build());
    }

    public void bakeJsonModels(QuantumClient client) {
        for (var entry : customRegistry.entrySet()) {
            List<Pair<Predicate<BlockState>, Supplier<BlockModel>>> models = new ArrayList<>();
            for (var pair : entry.getValue()) {
                BlockModel model = pair.getSecond().get();
                QuantumClient.invokeAndWait(() -> model.load(client));
                models.add(new Pair<>(pair.getFirst(), Suppliers.memoize(() -> model)));
            }
            finishedRegistry.put(entry.getKey(), models);
        }
    }

    public void load(Json5ModelLoader loader) {
        for (Block value : Registries.BLOCK.values()) {
            this.loadingBlock = value;
            if (!registry.containsKey(value)) {
                try {
                    Json5Model load = loader.load(value);
                    if (load != null) {
                        customRegistry.computeIfAbsent(value, key -> new ArrayList<>()).add(new Pair<>(meta -> true, () -> load));

                        load.getOverrides().cellSet().forEach((cell) -> customRegistry.computeIfAbsent(value, key -> new ArrayList<>()).add(new Pair<>(meta -> meta.get(cell.getRowKey()).equals(cell.getColumnKey()), cell::getValue)));
                    } else if (value.doesRender()) {
                        this.registerDefault(value);
                    }
                } catch (IOException e) {
                    QuantumClient.LOGGER.error("Failed to load block model for {}", value.getId(), e);
                }
            }

            this.loadingBlock = null;
        }

        for (var entry : customRegistry.entrySet()) {
            this.loadingBlock = entry.getKey();
            List<Pair<Predicate<BlockState>, Supplier<BlockModel>>> models = new ArrayList<>();
            for (var pair : entry.getValue()) {
                BlockModel model = pair.getSecond().get();
                models.add(new Pair<>(pair.getFirst(), Suppliers.memoize(() -> model)));
            }
            finishedRegistry.put(entry.getKey(), models);
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
                client.bakedBlockModels = this.bake(client.blocksTextureAtlas);
                if (client.bakedBlockModels == null) {
                    throw new RuntimeException("Failed to bake block models");
                }
            } catch (Exception e) {
                CrashLog crashLog = new CrashLog("Failed to load block models", e);
                CrashCategory model = new CrashCategory("Model");
                model.add("Block", this.loadingBlock.getId());
                model.add("Location", this.loadingBlock.getId().mapPath(path -> "models/blocks/" + path + ".json5"));
                crashLog.addCategory(model);
                QuantumClient.crash(crashLog);
            } catch (ApplicationCrash e) {
                QuantumClient.crash(e.getCrashLog());
            }
        });
    }
}
