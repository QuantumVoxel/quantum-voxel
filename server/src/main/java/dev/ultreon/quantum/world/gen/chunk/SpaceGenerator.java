package dev.ultreon.quantum.world.gen.chunk;

import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.biome.BiomeGenerator;
import dev.ultreon.quantum.world.gen.carver.Carver;
import dev.ultreon.quantum.world.gen.carver.FloatingIslandsCarver;
import org.jetbrains.annotations.NotNull;

import static dev.ultreon.quantum.world.World.CS;

/**
 * The SpaceGenerator class extends the functionality of SimpleChunkGenerator to create
 * a custom terrain generator that produces space-like floating islands.
 */
public class SpaceGenerator extends SimpleChunkGenerator {
    private Carver carver;
    private BiomeGenerator space;

    public SpaceGenerator(Registry<Biome> biomeRegistry) {
        super(biomeRegistry);
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {
        super.create(world, seed);

        carver = new FloatingIslandsCarver(seed);
        space = world.getServer().getBiomes().space.create(world, seed);
    }

    @Override
    protected void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver) {
        for (int x = 0; x < CS; x++) {
            for (int z = 0; z < CS; z++) {
                carver.carve(chunk, x, z);

                for (int y = 0; y < CS; y++) {
                    // Set biomes to registry key "quantum:space"
                    chunk.setBiomeGenerator(x, z, space);
                }
            }
        }
    }

    @Override
    @NotNull
    public Carver getCarver() {
        return carver;
    }

    @Override
    public double getTemperature(int x, int z) {
        return -4.0;
    }
}
