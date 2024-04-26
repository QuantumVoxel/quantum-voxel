package com.ultreon.quantum.world.gen.biome;

import com.ultreon.quantum.block.Blocks;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.server.QuantumServer;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.Biome;
import com.ultreon.quantum.world.gen.feature.*;
import com.ultreon.quantum.world.gen.layer.*;
import com.ultreon.quantum.world.gen.noise.DomainWarping;
import com.ultreon.quantum.world.gen.noise.NoiseConfigs;
import kotlin.ranges.IntRange;

public class Biomes {
    public static final Biome VOID = Biomes.register("void", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(Float.NEGATIVE_INFINITY)
            .temperatureEnd(Float.POSITIVE_INFINITY)
            .doesNotGenerate()
            .build());

    public static final Biome PLAINS = Biomes.register("plains", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(-2.0f)
            .temperatureEnd(1.0f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
            .layer(new StoneyPeaksTerrainLayer(Blocks.STONE, 108))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, -0.5f, 4))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new OreFeature(NoiseConfigs.ORE.seed(), Blocks.IRON_ORE, 20, new IntRange(4, 6), new IntRange(24, 72)))
            .feature(new FoliageFeature(NoiseConfigs.FIOLAGE, Blocks.TALL_GRASS, -0.15f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
            .build());

    public static final Biome FOREST = Biomes.register("forest", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(1.0f)
            .temperatureEnd(1.5f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, -0.5f, 4))
            .feature(new FoliageFeature(NoiseConfigs.FIOLAGE, Blocks.TALL_GRASS, -0.15f))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.2f, 3, 6))
            .build());

    public static final Biome DESERT = Biomes.register("desert", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(1.5f)
            .temperatureEnd(2f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 7))
            .layer(new GroundTerrainLayer(Blocks.SANDSTONE, 3, 4))
            .layer(new SurfaceTerrainLayer(Blocks.SAND, 3))
            .layer(new WaterTerrainLayer(64))
            .feature(new CactiFeature(NoiseConfigs.TREE, Blocks.CACTUS, 0.01f, 1, 3))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.SANDSTONE, -0.9f, 4))
            .build());

    public static final Biome OCEAN = Biomes.register("ocean", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), QuantumServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(-4f)
            .temperatureEnd(4f)
            .layer(new SurfaceTerrainLayer(Blocks.DIRT, 4))
            .layer(new WaterTerrainLayer(64))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_1, Blocks.SAND, -0.3f, 4))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_2, Blocks.GRAVEL, -0.3f, 4))
            .ocean()
            .build());

    private static Biome register(String name, Biome biome) {
        Registries.BIOME.register(new Identifier(name), biome);
        return biome;
    }

    public static void init() {
        // NOOP
    }
}
