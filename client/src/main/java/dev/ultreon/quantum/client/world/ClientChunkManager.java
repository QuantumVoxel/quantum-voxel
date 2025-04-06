package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.utils.LongMap;
import com.google.common.collect.ImmutableList;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.InvalidThreadException;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

public class ClientChunkManager extends GameObject implements ChunkManager<ClientChunk> {
    private final LongMap<ClientChunk> chunks = new LongMap<>();
    private final ClientWorld world;

    public ClientChunkManager(ClientWorld world) {
        this.world = world;
    }

    public ClientWorld getWorld() {
        return world;
    }

    public void add(ClientChunk chunk) {
        synchronized (this) {
            this.chunks.put(chunkKey(chunk.vec.x, chunk.vec.y, chunk.vec.z), chunk);
        }
    }

    @Override
    public boolean remove(ClientChunk chunk) {
        synchronized (this) {
            ClientChunk clientChunk = this.chunks.get(chunkKey(chunk.vec.x, chunk.vec.y, chunk.vec.z));
            if (clientChunk == chunk) {
                this.chunks.remove(chunkKey(chunk.vec.x, chunk.vec.y, chunk.vec.z));
                return true;
            }
            return false;
        }
    }

    public @Nullable ClientChunk remove(int x, int y, int z) {
        synchronized (this) {
            return this.chunks.remove(chunkKey(x, y, z));
        }
    }

    public @Nullable ClientChunk get(int x, int y, int z) {
        synchronized (this) {
            return this.chunks.get(chunkKey(x, y, z));
        }
    }

    public ClientChunk get(ChunkVec pos) {
        synchronized (this) {
            return this.chunks.get(chunkKey(pos.x, pos.y, pos.z));
        }
    }

    public boolean contains(int x, int y, int z) {
        synchronized (this) {
            return this.chunks.containsKey(chunkKey(x, y, z));
        }
    }

    public boolean contains(ChunkVec pos) {
        synchronized (this) {
            return this.chunks.containsKey(chunkKey(pos.x, pos.y, pos.z));
        }
    }

    public int size() {
        synchronized (this) {
            return this.chunks.size;
        }
    }

    public void dispose() {
        if (!QuantumClient.isOnRenderThread()) throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        super.dispose();
    }

    private static long chunkKey(int x, int y, int z) {
        return (((long) x) & 0xFFFFF) | ((((long) y) & 0xFFFFF) << 20) | ((((long) z) & 0xFFFFF) << 40);
    }

    public ImmutableList<ClientChunk> getAllChunks() {
        synchronized (this) {
            var chunkList = new ImmutableList.Builder<ClientChunk>();
            for (var chunk : this.chunks.values()) chunkList.add(chunk);
            return chunkList.build();
        }
    }

    public boolean contains(ClientChunk clientChunk) {
        synchronized (this) {
            return this.chunks.containsValue(clientChunk, true);
        }
    }

    public Iterator<ClientChunk> iterator() {
        return chunks.values().iterator();
    }
}
