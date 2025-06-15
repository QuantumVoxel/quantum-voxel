package dev.ultreon.quantum.world.gen.biome;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.gen.feature.*;
import dev.ultreon.quantum.world.gen.layer.*;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;
import kotlin.ranges.IntRange;

/**
 * The Biomes class represents a collection of biomes in the game.
 * These biomes include various landscapes and climatic regions such as forests, deserts, and oceans.
 * The class provides methods to initialize and register biomes.
 */
public class Biomes extends GameObject {
    public static final RegistryKey<Biome> VOID = RegistryKey.of(RegistryKeys.BIOME, NamespaceID.of("void"));

    private final QuantumServer server;

    public final Biome void_;
    public final Biome snowyPlains;
    public final Biome deepTaiga;
    public final Biome taiga;
    public final Biome plains;
    public final Biome dryPlains;
    public final Biome rockyPlains;
    public final Biome coldPlains;
    public final Biome frozenPlains;
    public final Biome hills;
    public final Biome jungle;
    public final Biome forest;
    public final Biome desert;
    public final Biome beach;
    public final Biome mountains;
    public final Biome ocean;
    public final Biome lukeWarmOcean;
    public final Biome warmOcean;
    public final Biome coldOcean;
    public final Biome space;

    private RegistryKey<Biome> defaultKey;

    /**
     * Initializes biome configurations for a QuantumServer instance.
     *
     * @param server the QuantumServer instance to which these biomes belong
     */
    public Biomes(QuantumServer server) {
        this.server = server;

        NoiseConfigs noiseConfigs = server.getNoiseConfigs();

        void_ = this.register("void", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
                .doesNotGenerate()
                .build());
        snowyPlains = this.register("snowy_plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(-1.0f, 0.0f)
                .humidityRange(-1.0f, 0.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SNOWY_SHORT_GRASS, 0.425f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        deepTaiga = this.register("deep_taiga", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(-2.0f, -1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SNOWY_SHORT_GRASS, 0.425f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.01f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        taiga = this.register("taiga", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(-1.0f, 0.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SNOWY_GRASS_BLOCK, 0.425f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        plains = this.register("plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.6f, 70.5f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS, 0.425f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        dryPlains = this.register("dry_plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(-2.0f, -1.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(RandomBlocksLayer.surface(1, 64, 108, Blocks.SAND, Blocks.GRASS_BLOCK))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS, 0.425f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        rockyPlains = this.register("rocky_plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(-1.0f, 0.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS, 0.425f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        coldPlains = this.register("cold_plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(-1.0f, 0.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS, 0.425f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.003f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        frozenPlains = this.register("frozen_plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(-2.0f, -1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(RandomBlocksLayer.surface(2, 64, 108, Blocks.ICE, Blocks.SNOW_BLOCK))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS, 0.425f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        hills = this.register("hills", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(70.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS, 0.425f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        jungle = this.register("jungle", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(0.0f, 2.0f)
                .humidityRange(1.0f, 2.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS, 0.425f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 4, 8))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        forest = this.register("forest", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(1.0f, 2.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS, 0.425f))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.05f, 3, 6))
                .build());
        desert = this.register("desert", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(1.0f, 2.0f)
                .humidityRange(-2.0f, 0.0f)
                .heightRange(64.0f, 320.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 7))
                .layer(new GroundTerrainLayer(Blocks.SANDSTONE, 3, 4))
                .layer(new SurfaceTerrainLayer(Blocks.SAND, 3))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new CactiFeature(noiseConfigs.tree, Blocks.CACTUS, 0.01f, 1, 3))
    //            .feature(new PatchFeature(noiseConfigs.PATCH, Blocks.SANDSTONE, 0.1f, 4))
                .build());
        beach = this.register("beach", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(-2.0f, 2.0f)
                .humidityRange(-2.0f, 2.0f)
                .heightRange(60.0f, 65.6f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SAND, 3))
                .layer(new WaterTerrainLayer(64))
                .build());
        mountains = this.register("mountains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(1.0f, 2.0f)
                .humidityRange(-2.0f, 0.0f)
                .heightRange(108.0f, 320.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 3))
                .layer(RandomBlocksLayer.surface(3, 108, 128, Blocks.COBBLESTONE, Blocks.STONE, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.GRAVEL))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new CactiFeature(noiseConfigs.tree, Blocks.CACTUS, 0.01f, 1, 3))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.SANDSTONE, 0.1f, 4))
                .build());
        ocean = this.register("ocean", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(0f, 0.5f)
                .heightRange(-64.0f, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.DIRT, 4))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.GRAVEL, 0.3f, 4))
                .ocean()
                .build());
        lukeWarmOcean = this.register("luke_warm_ocean", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(0.5f, 1.0f)
                .heightRange(-64.0f, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.SAND, 4))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.GRAVEL, 0.3f, 4))
                .ocean()
                .build());
        warmOcean = this.register("warm_ocean", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(1.0f, 2.0f)
                .heightRange(-64.0f, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.SANDSTONE, 4))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.GRAVEL, 0.3f, 4))
                .ocean()
                .build());
        coldOcean = this.register("cold_ocean", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .temperatureRange(-2f, 0.0f)
                .heightRange(-64.0f, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.DIRT, 4))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.GRAVEL, 0.3f, 4))
                .ocean()
                .build());

        space = this.register("space", Biome.builder()
                .noise(noiseConfigs.empty)
                .temperatureRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
                .heightRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
                .build());
    }

    /**
     * Registers a biome with the specified name in the server's biome registry.
     *
     * @param name the name of the biome to register
     * @param biome the biome instance to register
     * @return the registered biome instance
     */
    public Biome register(String name, Biome biome) {
        server.getRegistries().get(RegistryKeys.BIOME).register(new NamespaceID(name), biome);
        return biome;
    }

    public void init() {
        // NOOP
    }

    public RegistryKey<Biome> getDefaultKey() {
        if (defaultKey == null) {
            defaultKey = server.getRegistries().get(RegistryKeys.BIOME).getKey(plains);
        }
        return defaultKey;
    }
}
