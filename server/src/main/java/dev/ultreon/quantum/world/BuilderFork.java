package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;
import dev.ultreon.quantum.world.structure.BlockPoint;
import dev.ultreon.quantum.world.vec.BlockVec;

import java.util.ArrayList;
import java.util.List;

public class BuilderFork implements Fork {
    private final List<BlockPoint> positions = new ArrayList<>();
    private final BuilderChunk chunk;
    private final int x;
    private final int y;
    private final int z;
    private final ChunkGenerator generator;

    public BuilderFork(BuilderChunk chunk, int x, int y, int z, ChunkGenerator generator) {
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
        this.generator = generator;
    }

    @Override
    public boolean set(int x, int y, int z, BlockState block) {
        return this.positions.add(new BlockPoint(new BlockVec(this.x + x, this.y + y, this.z + z), () -> block));
    }

    public List<BlockPoint> getPositions() {
        return this.positions;
    }

    @Override
    public BuilderChunk getChunk() {
        return chunk;
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        return this.generator.getCarver().isAir(x, y, z);
    }
}
