package dev.ultreon.quantum.world.gen.biome;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.gen.feature.*;
import dev.ultreon.quantum.world.gen.layer.*;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;
import kotlin.ranges.IntRange;

public class Biomes {
    public static final Biome VOID = Biomes.register("void", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
            .doesNotGenerate()
            .build());

    public static final Biome SNOWY_PLAINS = Biomes.register("snowy_plains", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(-2.0f, 0.0f)
            .humidityRange(-2.0f, 0.0f)
            .heightRange(65.5f, 108.0f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
            .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, 0.5f, 4))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new OreFeature(NoiseConfigs.ORE.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
            .feature(new FoliageFeature(NoiseConfigs.FIOLAGE, Blocks.TALL_GRASS, -0.15f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
            .build());

    public static final Biome DEEP_TAIGA = Biomes.register("deep_taiga", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(-2.0f, -1.0f)
            .humidityRange(0.0f, 1.0f)
            .heightRange(65.5f, 108.0f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
            .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, 0.5f, 4))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new OreFeature(NoiseConfigs.ORE.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
            .feature(new FoliageFeature(NoiseConfigs.FIOLAGE, Blocks.TALL_GRASS, -0.15f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
            .build());

    public static final Biome TAIGA = Biomes.register("taiga", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(-1.0f, 0.0f)
            .humidityRange(0.0f, 1.0f)
            .heightRange(65.5f, 108.0f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
            .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, 0.5f, 4))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new OreFeature(NoiseConfigs.ORE.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
            .feature(new FoliageFeature(NoiseConfigs.FIOLAGE, Blocks.TALL_GRASS, -0.15f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
            .build());

    public static final Biome PLAINS = Biomes.register("plains", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(0.0f, 1.0f)
            .humidityRange(0.0f, 1.0f)
            .heightRange(65.5f, 70.5f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
            .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, 0.5f, 4))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new OreFeature(NoiseConfigs.ORE.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
            .feature(new FoliageFeature(NoiseConfigs.FIOLAGE, Blocks.TALL_GRASS, -0.15f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
            .build());

    public static final Biome HILLS = Biomes.register("hills", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(0.0f, 1.0f)
            .humidityRange(0.0f, 1.0f)
            .heightRange(70.5f, 108.0f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
            .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, 0.5f, 4))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new OreFeature(NoiseConfigs.ORE.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
            .feature(new FoliageFeature(NoiseConfigs.FIOLAGE, Blocks.TALL_GRASS, -0.15f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
            .build());

    public static final Biome JUNGLE = Biomes.register("jungle", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(0.0f, 2.0f)
            .humidityRange(1.0f, 2.0f)
            .heightRange(65.5f, 108.0f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
            .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, 0.5f, 4))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new OreFeature(NoiseConfigs.ORE.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
            .feature(new FoliageFeature(NoiseConfigs.FIOLAGE, Blocks.TALL_GRASS, -0.15f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.007f, 4, 8))
            .build());

    public static final Biome FOREST = Biomes.register("forest", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(0.0f, 1.0f)
            .humidityRange(1.0f, 2.0f)
            .heightRange(65.5f, 108.0f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, 0.5f, 4))
            .feature(new FoliageFeature(NoiseConfigs.FIOLAGE, Blocks.TALL_GRASS, -0.15f))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.05f, 3, 6))
            .build());

    public static final Biome DESERT = Biomes.register("desert", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(1.0f, 2.0f)
            .humidityRange(-2.0f, 0.0f)
            .heightRange(64.0f, 320.0f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 7))
            .layer(new GroundTerrainLayer(Blocks.SANDSTONE, 3, 4))
            .layer(new SurfaceTerrainLayer(Blocks.SAND, 3))
            .layer(new WaterTerrainLayer(64))
            .feature(new CactiFeature(NoiseConfigs.TREE, Blocks.CACTUS, 0.01f, 1, 3))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.SANDSTONE, 0.1f, 4))
            .build());

    public static final Biome MOUNTAINS = Biomes.register("mountains", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(1.0f, 2.0f)
            .humidityRange(-2.0f, 0.0f)
            .heightRange(108.0f, 320.0f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 3))
            .layer(RandomBlocksLayer.surface(3, 108, 128, Blocks.COBBLESTONE, Blocks.STONE, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.GRAVEL))
            .layer(new WaterTerrainLayer(64))
            .feature(new CactiFeature(NoiseConfigs.TREE, Blocks.CACTUS, 0.01f, 1, 3))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.SANDSTONE, 0.1f, 4))
            .build());

    public static final Biome OCEAN = Biomes.register("ocean", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(0f, 0.5f)
            .heightRange(-64.0f, 64.0f)
            .layer(new SurfaceTerrainLayer(Blocks.DIRT, 4))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_1, Blocks.SAND, 0.3f, 4))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_2, Blocks.GRAVEL, 0.3f, 4))
            .ocean()
            .build());

    public static final Biome LUKE_WARM_OCEAN = Biomes.register("luke_warm_ocean", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(0.5f, 1.0f)
            .heightRange(-64.0f, 64.0f)
            .layer(new SurfaceTerrainLayer(Blocks.SAND, 4))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_1, Blocks.SAND, 0.3f, 4))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_2, Blocks.GRAVEL, 0.3f, 4))
            .ocean()
            .build());

    public static final Biome WARM_OCEAN = Biomes.register("warm_ocean", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(1.0f, 2.0f)
            .heightRange(-64.0f, 64.0f)
            .layer(new SurfaceTerrainLayer(Blocks.SANDSTONE, 4))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_1, Blocks.SAND, 0.3f, 4))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_2, Blocks.GRAVEL, 0.3f, 4))
            .ocean()
            .build());

    public static final Biome COLD_OCEAN = Biomes.register("cold_ocean", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureRange(-2f, 0.0f)
            .heightRange(-64.0f, 64.0f)
            .layer(new SurfaceTerrainLayer(Blocks.DIRT, 4))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_1, Blocks.SAND, 0.3f, 4))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_2, Blocks.GRAVEL, 0.3f, 4))
            .ocean()
            .build());

    private static Biome register(String name, Biome biome) {
        Registries.BIOME.register(new NamespaceID(name), biome);
        return biome;
    }

    public static void init() {
        // NOOP
    }
}
