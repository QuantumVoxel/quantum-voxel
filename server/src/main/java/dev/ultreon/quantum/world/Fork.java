package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.BlockState;

public interface Fork extends BlockSetter {
    ChunkAccess getChunk();

    boolean isAir(int x, int y, int z);

    BlockState get(int x, int y, int z);
}
