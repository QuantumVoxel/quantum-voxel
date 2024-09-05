package dev.ultreon.quantum.world.dimension;

import dev.ultreon.quantum.registry.ServerRegistry;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;
import dev.ultreon.quantum.world.gen.chunk.OverworldGenerator;

import java.util.function.Function;

public class DimensionType {
    public static final DimensionType OVERWORLD = new DimensionType(OverworldGenerator::new);
    private final Function<ServerRegistry<Biome>, ChunkGenerator> func;

    public DimensionType(Function<ServerRegistry<Biome>, ChunkGenerator> func) {
        this.func = func;
    }

    public ChunkGenerator createGenerator(ServerRegistry<Biome> biomeRegistry) {
        return this.func.apply(biomeRegistry);
    }
}
