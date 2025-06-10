package dev.ultreon.quantum.world.gen.chunk;

import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.gen.carver.Carver;

public class VoidCarver implements Carver {
    @Override
    public float carve(BuilderChunk chunk, int x, int z) {
        return 0;
    }

    @Override
    public float getSurfaceHeightNoise(float x, float z) {
        return 0;
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        return y > 0;
    }
}
