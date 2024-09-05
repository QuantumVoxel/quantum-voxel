package dev.ultreon.quantum.world.gen.carver;

import dev.ultreon.quantum.world.BuilderChunk;

public interface Carver {
    int carve(BuilderChunk chunk, int x, int z, double hilliness);

    int getSurfaceHeightNoise(float x, float z);
}
