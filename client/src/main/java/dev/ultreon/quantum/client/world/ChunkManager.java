package dev.ultreon.quantum.client.world;

import dev.ultreon.quantum.world.Chunk;
import org.jetbrains.annotations.Nullable;

public interface ChunkManager<T extends Chunk> extends Iterable<T> {
    @Nullable T get(int x, int y, int z);

    @Nullable T remove(int x, int y, int z);
    
    void add(T chunk);
    
    boolean remove(T chunk);
}
