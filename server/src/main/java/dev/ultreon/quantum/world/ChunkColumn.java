package dev.ultreon.quantum.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import dev.ultreon.quantum.util.IVec2;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class ChunkColumn implements Column<ServerChunk> {
    private final IntMap<ServerChunk> chunks = new IntMap<>();
    private final ServerWorld world;
    private final IVec2 columnPos;
    private int firstY;
    private int lastY;

    public ChunkColumn(ServerWorld world, IVec2 columnPos) {
        this.world = world;
        this.columnPos = columnPos;
    }

    public @Nullable ServerChunk get(int regionY) {
        synchronized (this) {
            return this.chunks.get(regionY, null);
        }
    }
    
    public void set(int regionY, ServerChunk region) {
        if (region == null) {
            remove(regionY);
            return;
        }
        synchronized (this) {
            if (regionY < this.firstY) {
                this.firstY = regionY;
            }
            if (regionY > this.lastY) {
                this.lastY = regionY;
            }
            this.chunks.put(regionY, region);
        }
    }
    
    public ServerChunk remove(int regionY) {
        synchronized (this) {
            ServerChunk remove = this.chunks.remove(regionY);
            if (regionY == this.firstY) {
                this.firstY = findHighestY();
            }
            if (regionY == this.lastY) {
                this.lastY = findLowestY();
            }
            return remove;
        }
    }

    private int findHighestY() {
        int highest = 0;
        for (int y : this.chunks.keys().toArray().toArray()) {
            if (y > highest) {
                highest = y;
            }
        }
        return highest;
    }
    
    private int findLowestY() {
        int lowest = Integer.MAX_VALUE;
        for (int y : this.chunks.keys().toArray().toArray()) {
            if (y < lowest) {
                lowest = y;
            }
        }
        return lowest;
    }

    @Override
    public int getFirstY() {
        return firstY;
    }

    @Override
    public int getLastY() {
        return lastY;
    }

    @Override
    public int size() {
        return chunks.size;
    }

    public Array<ServerChunk> values() {
        synchronized (this) {
            return this.chunks.values().toArray();
        }
    }

    @Override
    public @NotNull Iterator<ServerChunk> iterator() {
        return this.chunks.values().iterator();
    }

    public void dispose() {
        synchronized (this) {
            for (ServerChunk chunk : this.chunks.values()) {
                chunk.dispose();
            }
            this.chunks.clear();
        }
    }

    public ServerWorld getWorld() {
        return world;
    }

    public IVec2 getColumnPos() {
        return columnPos;
    }

    public ServerChunk setIfAbsent(ChunkVec chunkVec, ServerChunk builtChunk) {
        synchronized (this) {
            ServerChunk chunk = this.get(chunkVec.y);
            if (chunk == null) {
                this.set(chunkVec.y, builtChunk);
                return null;
            } else {
                return chunk;
            }
        }
    }
}
