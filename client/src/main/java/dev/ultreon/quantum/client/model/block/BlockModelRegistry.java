package dev.ultreon.quantum.client.model.block;

import com.badlogic.gdx.graphics.Texture;
import com.google.common.base.Preconditions;
import dev.ultreon.quantum.client.resources.ContextAwareReloadable;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.Suppliers;
import com.google.common.collect.ImmutableMap;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.atlas.TextureStitcher;
import dev.ultreon.quantum.client.model.model.Json5Model;
import dev.ultreon.quantum.client.model.model.Json5ModelLoader;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Identifier;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BlockModelRegistry implements ContextAwareReloadable {
    private static final BlockModelRegistry INSTANCE = new BlockModelRegistry();
    private final Map<Block, List<Pair<Predicate<BlockProperties>, Supplier<CubeModel>>>> registry = new LinkedHashMap<>(CommonConstants.MAX_BLOCK_REGISTRY);
    private final Map<Block, List<Pair<Predicate<BlockProperties>, Supplier<BlockModel>>>> customRegistry = new LinkedHashMap<>(CommonConstants.MAX_BLOCK_REGISTRY);
    private final Set<Identifier> TEXTURES = new HashSet<>();
    private final Map<Block, List<Pair<Predicate<BlockProperties>, Supplier<BlockModel>>>> finishedRegistry = new LinkedHashMap<>(CommonConstants.MAX_BLOCK_REGISTRY);

    public BlockModelRegistry() {
        this.TEXTURES.add(new Identifier("misc/breaking1"));
        this.TEXTURES.add(new Identifier("misc/breaking2"));
        this.TEXTURES.add(new Identifier("misc/breaking3"));
        this.TEXTURES.add(new Identifier("misc/breaking4"));
        this.TEXTURES.add(new Identifier("misc/breaking5"));
        this.TEXTURES.add(new Identifier("misc/breaking6"));
    }

    public static BlockModelRegistry get() {
        return INSTANCE;
    }

    public BlockModel get(BlockProperties meta) {
        for (Pair<Predicate<BlockProperties>, Supplier<BlockModel>> p : this.customRegistry.getOrDefault(meta.getBlock(), new ArrayList<>())) {
            if (p.getFirst().test(meta)) {
                return p.getSecond().get();
            }
        }
        return null;
    }

    public void register(Block block, Predicate<BlockProperties> predicate, CubeModel model) {
        this.registry.computeIfAbsent(block, key -> new ArrayList<>()).add(new Pair<>(predicate, () -> model));
    }

    public void registerCustom(Block block, Predicate<BlockProperties> predicate, Supplier<BlockModel> model) {
        this.customRegistry.computeIfAbsent(block, key -> new ArrayList<>()).add(new Pair<>(predicate, Suppliers.memoize(model)));
    }

    public void register(Supplier<Block> block, Predicate<BlockProperties> predicate, Supplier<CubeModel> model) {
        this.registry.computeIfAbsent(block.get(), key -> new ArrayList<>()).add(new Pair<>(predicate, Suppliers.memoize(model)));
    }

    public void registerDefault(Block block) {
        Identifier key = Registries.BLOCK.getId(block);
        Preconditions.checkNotNull(key, "Block is not registered");
        this.register(block, meta -> true, CubeModel.of(key.mapPath(path -> "blocks/" + path), key.mapPath(path -> "blocks/" + path)));
    }

    public void registerDefault(Supplier<Block> block) {
        this.register(block, meta -> true, Suppliers.memoize(() -> {
            Identifier key = Registries.BLOCK.getId(block.get());
            Preconditions.checkNotNull(key, "Block is not registered");
            return CubeModel.of(key.mapPath(path -> "blocks/" + path), key.mapPath(path -> "blocks/" + path));
        }));
    }

    public TextureAtlas stitch(TextureManager textureManager) {
        TextureStitcher stitcher = new TextureStitcher(QuantumClient.id("block"));

        this.registry.values().stream().flatMap(Collection::stream).map(pair -> pair.getSecond().get().all()).forEach(this.TEXTURES::addAll);

        final int breakStages = 6;

        for (int i = 0; i < breakStages; i++) {
            Identifier texId = new Identifier("textures/misc/breaking" + (i + 1) + ".png");
            Texture tex = textureManager.getTexture(texId);
            stitcher.add(texId, tex);
        }

        for (Identifier texture : this.TEXTURES) {
            Texture emissive = textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".emissive.png"), null);
            Texture normal = textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".normal.png"), null);
            Texture specular = textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".specular.png"), null);
            Texture reflective = textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".reflective.png"), null);
            if (emissive != null) {
                stitcher.add(texture, textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".png")), emissive, normal, specular, reflective);
            } else {
                stitcher.add(texture, textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".png")));
            }
        }

        return stitcher.stitch();
    }

    public BakedModelRegistry bake(TextureAtlas atlas) {
        ImmutableMap.Builder<Block, List<Pair<Predicate<BlockProperties>, BakedCubeModel>>> bakedModels = new ImmutableMap.Builder<>();
        this.registry.forEach((block, models) -> {
            List<Pair<Predicate<BlockProperties>, BakedCubeModel>> modelList = new ArrayList<>();
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
            List<Pair<Predicate<BlockProperties>, Supplier<BlockModel>>> models = new ArrayList<>();
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
            if (!registry.containsKey(value)) {
                try {
                    Json5Model load = loader.load(value);
                    if (load != null) {
                        customRegistry.computeIfAbsent(value, key -> new ArrayList<>()).add(new Pair<>(meta -> true, () -> load));

                        load.getOverrides().cellSet().forEach((cell) -> customRegistry.computeIfAbsent(value, key -> new ArrayList<>()).add(new Pair<>(meta -> meta.getEntryUnsafe(cell.getRowKey()).equals(cell.getColumnKey()), cell::getValue)));
                    } else if (value.doesRender()) {
                        this.registerDefault(value);
                    }
                } catch (IOException e) {
                    QuantumClient.LOGGER.error("Failed to load block model for " + value.getId(), e);
                }
            }
        }

        for (var entry : customRegistry.entrySet()) {
            List<Pair<Predicate<BlockProperties>, Supplier<BlockModel>>> models = new ArrayList<>();
            for (var pair : entry.getValue()) {
                BlockModel model = pair.getSecond().get();
                models.add(new Pair<>(pair.getFirst(), Suppliers.memoize(() -> model)));
            }
            finishedRegistry.put(entry.getKey(), models);
        }
    }

    @Override
    public void reload(ResourceManager resourceManager, ReloadContext context) {
        QuantumClient client = QuantumClient.get();
        context.submit(() -> {
            this.load(client.j5ModelLoader);

            QuantumClient.LOGGER.info("Baking models");
            this.bakeJsonModels(client);
            client.bakedBlockModels = this.bake(client.blocksTextureAtlas);
        });
    }
}
