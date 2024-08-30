package dev.ultreon.quantum.world.dimension;

import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.gen.ChunkGenerator;
import dev.ultreon.quantum.world.gen.OverworldGenerator;

import java.util.function.Function;

public class DimensionType {
    public static final DimensionType OVERWORLD = new DimensionType(OverworldGenerator::new);

    public DimensionType(Function<Registry<Biome>, ChunkGenerator> func) {

    }
}
