package dev.ultreon.quantum.world.gen.carver;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.Heightmap;
import dev.ultreon.quantum.world.HeightmapType;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import org.jetbrains.annotations.NotNull;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public class FlatWorldCarver implements Carver {
    @Override
    public int carve(@NotNull BuilderChunk chunk, int x, int z, double hilliness) {
        Heightmap worldSurface = chunk.getWorld().heightMapAt(x, z, HeightmapType.WORLD_SURFACE);
        Heightmap motionBlocking = chunk.getWorld().heightMapAt(x, z, HeightmapType.MOTION_BLOCKING);

        for (int y = chunk.getOffset().y; y < CHUNK_SIZE; y++) {
            if (y < 0) {
                chunk.set(x, y, z, Blocks.STONE.createMeta());
            } else if (y < 3) {
                chunk.set(x, y, z, Blocks.DIRT.createMeta());
            } else if (y == 3) {
                chunk.set(x, y, z, Blocks.GRASS_BLOCK.createMeta());
            }
        }

        BlockVec vec = new BlockVec(x, 3, z, BlockVecSpace.WORLD).chunkLocal();
        worldSurface.set(vec.x, vec.z, (short) 3);
        motionBlocking.set(vec.x, vec.z, (short) 3);

        return 3;
    }

    @Override
    public int getSurfaceHeightNoise(float x, float z) {
        return 3;
    }
}
