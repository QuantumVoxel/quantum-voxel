package dev.ultreon.quantum.world;

public interface Fork extends BlockSetter {
    ChunkAccess getChunk();

    boolean isAir(int x, int y, int z);
}
