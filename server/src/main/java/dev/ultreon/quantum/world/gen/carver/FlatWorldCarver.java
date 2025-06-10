package dev.ultreon.quantum.world.gen.carver;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.Heightmap;
import dev.ultreon.quantum.world.HeightmapType;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import org.jetbrains.annotations.NotNull;

import static dev.ultreon.quantum.world.World.CS;

/**
 * FlatWorldCarver is an implementation of the Carver interface designed to carve a flat terrain in a chunk.
 */
public class FlatWorldCarver implements Carver {
    @Override
    public float carve(@NotNull BuilderChunk chunk, int x, int z) {
        Heightmap worldSurface = chunk.getWorld().heightMapAt(x, z, HeightmapType.WORLD_SURFACE);
        Heightmap motionBlocking = chunk.getWorld().heightMapAt(x, z, HeightmapType.MOTION_BLOCKING);

        for (int y = chunk.getOffset().y; y < CS; y++) {
            BlockVec vec = new BlockVec(x, y, z, BlockVecSpace.WORLD).chunkLocal();

            if (y < 0) chunk.set(vec.x, vec.y, vec.z, Blocks.STONE.getDefaultState());
            else if (y < 3) chunk.set(vec.x, vec.y, vec.z, Blocks.DIRT.getDefaultState());
            else if (y == 3) chunk.set(vec.x, vec.y, vec.z, Blocks.GRASS_BLOCK.getDefaultState());
        }

        BlockVec vec = new BlockVec(x, 3, z, BlockVecSpace.WORLD).chunkLocal();
        worldSurface.set(vec.x, vec.z, (short) 3);
        motionBlocking.set(vec.x, vec.z, (short) 3);

        return 3;
    }

    @Override
    public float getSurfaceHeightNoise(float x, float z) {
        return 3;
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        return y <= 3;
    }
}
