package dev.ultreon.quantum.world.gen.chunk;

import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.carver.Carver;
import dev.ultreon.quantum.world.gen.carver.HellLandscapeCarver;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

/**
 * HellGenerator is a specialized chunk generator designed to create a hellish
 * terrain in a Quantum Voxel world. It extends the SimpleChunkGenerator to leverage
 * common chunk generation functionalities, and introduces specific terrain
 * carving mechanisms for a hell-like environment.
 */
@ApiStatus.Experimental
public class HellGenerator extends SimpleChunkGenerator {
    private Carver carver;

    public HellGenerator(Registry<Biome> biomeRegistry) {
        super(biomeRegistry);
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {
        super.create(world, seed);

        this.carver = new HellLandscapeCarver(seed);
    }

    @Override
    protected void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver) {
        BlockVec offset = chunk.getOffset();

        for (int x = offset.x; x < offset.x + CHUNK_SIZE; x++) {
            for (int z = offset.z; z < offset.z + CHUNK_SIZE; z++) {
                this.carver.carve(chunk, x, z);
            }
        }
    }

    @Override
    @NotNull
    public Carver getCarver() {
        return carver;
    }
}
