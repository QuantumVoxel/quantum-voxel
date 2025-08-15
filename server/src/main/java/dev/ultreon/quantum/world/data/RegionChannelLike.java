package dev.ultreon.quantum.world.data;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface RegionChannelLike extends AutoCloseable {
    int HEADER_SIZE = 4096;

    int CHUNK_GRID = World.REGION_SIZE;
    int CHUNK_COUNT = CHUNK_GRID * CHUNK_GRID * CHUNK_GRID;
    int INDEX_ENTRY_SIZE = 12;
    int INDEX_TABLE_SIZE = CHUNK_COUNT * INDEX_ENTRY_SIZE;
    int INDEX_OFFSET = HEADER_SIZE;

    int SECTOR_SIZE = 4096;       // Updated sector size
    int SECTOR_MAP_START = 97;    // Starting sector of reference map
    int SECTOR_MAP_COUNT = 64;     // Enough to store 4096 entries
    int HEADER_SECTORS = 1;
    int ENTRY_SIZE = 8;           // 4 bytes chunkId + 4 bytes sector

    void saveChunk(int cx, int cy, int cz, @NotNull MapType chunk) throws IOException;

    @Nullable MapType loadChunk(int cx, int cy, int cz) throws IOException;

    FileHandle getTarget();

    void flush() throws IOException;

    @Override
    void close() throws IOException;
}
