package dev.ultreon.quantum.world.gen.biome;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.gen.feature.*;
import dev.ultreon.quantum.world.gen.layer.*;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.gen.noise.NoiseConfigs;
import kotlin.ranges.IntRange;

public class Biomes {
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

    private RegistryKey<Biome> defaultKey;

    public Biomes(QuantumServer server) {
        this.server = server;

        NoiseConfigs noiseConfigs = server.getNoiseConfigs();

        void_ = this.register("void", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
                .doesNotGenerate()
                .build());
        snowyPlains = this.register("snowy_plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(-1.0f, 0.0f)
                .humidityRange(-1.0f, 0.0f)
                .heightRange(65.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .feature(new OreFeature(noiseConfigs.ore.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .build());
        deepTaiga = this.register("deep_taiga", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(-2.0f, -1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .feature(new OreFeature(noiseConfigs.ore.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.01f, 3, 5))
                .build());
        taiga = this.register("taiga", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(-1.0f, 0.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .feature(new OreFeature(noiseConfigs.ore.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .build());
        plains = this.register("plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.5f, 70.5f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .feature(new OreFeature(noiseConfigs.ore.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .build());
        dryPlains = this.register("dry_plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(-2.0f, -1.0f)
                .heightRange(65.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(RandomBlocksLayer.surface(1, 64, 108, Blocks.SAND, Blocks.GRASS_BLOCK))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .feature(new OreFeature(noiseConfigs.ore.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .build());
        rockyPlains = this.register("rocky_plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(-1.0f, 0.0f)
                .heightRange(65.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.005f))
                .feature(new OreFeature(noiseConfigs.ore.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .build());
        coldPlains = this.register("cold_plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(-1.0f, 0.0f)
                .heightRange(65.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.005f))
                .feature(new OreFeature(noiseConfigs.ore.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.003f, 3, 5))
                .build());
        frozenPlains = this.register("frozen_plains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(-2.0f, -1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(RandomBlocksLayer.surface(2, 64, 108, Blocks.ICE, Blocks.SNOW_BLOCK))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.005f))
                .feature(new OreFeature(noiseConfigs.ore.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .build());
        hills = this.register("hills", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(70.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .feature(new OreFeature(noiseConfigs.ore.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
                .build());
        jungle = this.register("jungle", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(0.0f, 2.0f)
                .humidityRange(1.0f, 2.0f)
                .heightRange(65.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .feature(new OreFeature(noiseConfigs.ore.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.007f, 4, 8))
                .build());
        forest = this.register("forest", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(1.0f, 2.0f)
                .heightRange(65.5f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .feature(new FoliageFeature(noiseConfigs.foliage, Blocks.TALL_GRASS, -0.15f))
                .feature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .feature(new TreeFeature(noiseConfigs.tree, Blocks.LOG, Blocks.LEAVES, 0.05f, 3, 6))
                .build());
        desert = this.register("desert", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(1.0f, 2.0f)
                .humidityRange(-2.0f, 0.0f)
                .heightRange(64.0f, 320.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 7))
                .layer(new GroundTerrainLayer(Blocks.SANDSTONE, 3, 4))
                .layer(new SurfaceTerrainLayer(Blocks.SAND, 3))
                .layer(new WaterTerrainLayer(64))
                .feature(new CactiFeature(noiseConfigs.tree, Blocks.CACTUS, 0.01f, 1, 3))
    //            .feature(new PatchFeature(noiseConfigs.PATCH, Blocks.SANDSTONE, 0.1f, 4))
                .build());
        beach = this.register("beach", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(-2.0f, 2.0f)
                .humidityRange(-2.0f, 2.0f)
                .heightRange(60.0f, 65.5f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SAND, 3))
                .layer(new WaterTerrainLayer(64))
                .build());
        mountains = this.register("mountains", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(1.0f, 2.0f)
                .humidityRange(-2.0f, 0.0f)
                .heightRange(108.0f, 320.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 3))
                .layer(RandomBlocksLayer.surface(3, 108, 128, Blocks.COBBLESTONE, Blocks.STONE, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.GRAVEL))
                .layer(new WaterTerrainLayer(64))
                .feature(new CactiFeature(noiseConfigs.tree, Blocks.CACTUS, 0.01f, 1, 3))
                .feature(new PatchFeature(noiseConfigs.patch, Blocks.SANDSTONE, 0.1f, 4))
                .build());
        ocean = this.register("ocean", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(0f, 0.5f)
                .heightRange(-64.0f, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.DIRT, 4))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .feature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.GRAVEL, 0.3f, 4))
                .ocean()
                .build());
        lukeWarmOcean = this.register("luke_warm_ocean", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(0.5f, 1.0f)
                .heightRange(-64.0f, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.SAND, 4))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .feature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.GRAVEL, 0.3f, 4))
                .ocean()
                .build());
        warmOcean = this.register("warm_ocean", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(1.0f, 2.0f)
                .heightRange(-64.0f, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.SANDSTONE, 4))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .feature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.GRAVEL, 0.3f, 4))
                .ocean()
                .build());
        coldOcean = this.register("cold_ocean", Biome.builder()
                .noise(noiseConfigs.genericNoise)
                .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(noiseConfigs.layerX.create(seed)), QuantumServer.get().disposeOnClose(noiseConfigs.layerY.create(seed))))
                .temperatureRange(-2f, 0.0f)
                .heightRange(-64.0f, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.DIRT, 4))
                .layer(new WaterTerrainLayer(64))
                .feature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .feature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.GRAVEL, 0.3f, 4))
                .ocean()
                .build());
    }

    private Biome register(String name, Biome biome) {
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
