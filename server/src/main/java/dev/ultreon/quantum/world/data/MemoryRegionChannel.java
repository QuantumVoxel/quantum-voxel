package dev.ultreon.quantum.world.data;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class MemoryRegionChannel implements RegionChannelLike {
    private final MemoryByteChannel channel = new MemoryByteChannel();
    private final FileHandle file;

    private final BitSet usedSectors = new BitSet();
    private final Map<Integer, Integer> chunkSectorMap = new HashMap<>();
    private final ByteBuffer[] allChunkInfo = new ByteBuffer[32];

    public MemoryRegionChannel(FileHandle file) {
        this.file = file;
        usedSectors.set(0, HEADER_SECTORS); // Reserve header
        for (int i = 0; i < allChunkInfo.length; i++) {
            allChunkInfo[i] = ByteBuffer.allocate(4096);
        }
    }

    public void writeChunk(int chunkId, byte[] data) {
        synchronized (this) {
            int sectorsNeeded = (data.length + SECTOR_SIZE - 1) / SECTOR_SIZE;
            int startSector = findFreeSectors(sectorsNeeded);

            usedSectors.set(startSector, startSector + sectorsNeeded);

            channel.position((long) startSector * SECTOR_SIZE);
            channel.write(ByteBuffer.wrap(data));

            // Pad to full sector size
            int padding = sectorsNeeded * SECTOR_SIZE - data.length;
            if (padding > 0) channel.write(ByteBuffer.wrap(new byte[padding]));

            chunkSectorMap.put(chunkId, startSector);
            System.out.printf("Wrote chunk %d at sector %d (%d sectors)%n", chunkId, startSector, sectorsNeeded);
        }
    }

    public void writeSectorReferenceMap() throws IOException {
        synchronized (this) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(intToBytes(chunkSectorMap.size()));

            for (Map.Entry<Integer, Integer> entry : chunkSectorMap.entrySet()) {
                out.write(intToBytes(entry.getKey()));
                out.write(intToBytes(entry.getValue()));
            }

            byte[] mapData = out.toByteArray();
            int totalBytes = SECTOR_MAP_COUNT * SECTOR_SIZE;
            if (mapData.length > totalBytes) {
                throw new IOException("Chunk map too large! Exceeds reserved size.");
            }

            // Write map to file
            channel.position((long) SECTOR_MAP_START * SECTOR_SIZE);
            channel.write(ByteBuffer.wrap(mapData));

            // Pad to fill entire reserved map space
            if (mapData.length < totalBytes) {
                channel.write(ByteBuffer.wrap(new byte[totalBytes - mapData.length]));
            }

            usedSectors.set(SECTOR_MAP_START, SECTOR_MAP_START + SECTOR_MAP_COUNT);
        }
    }

    public void readSectorReferenceMap() {
        synchronized (this) {
            channel.position((long) SECTOR_MAP_START * SECTOR_SIZE);
            byte[] header = new byte[4];
            channel.read(ByteBuffer.wrap(header));
            int entryCount = bytesToInt(header, 0);

            int totalBytes = entryCount * ENTRY_SIZE + 4;
            int totalSectors = (totalBytes + SECTOR_SIZE - 1) / SECTOR_SIZE;

            byte[] mapData = new byte[totalSectors * SECTOR_SIZE];
            channel.position((long) SECTOR_MAP_START * SECTOR_SIZE);
            channel.read(ByteBuffer.wrap(mapData));

            chunkSectorMap.clear();
            usedSectors.set(SECTOR_MAP_START, SECTOR_MAP_START + SECTOR_MAP_COUNT);

            for (int i = 0; i < entryCount; i++) {
                int base = 4 + i * ENTRY_SIZE;
                int chunkId = bytesToInt(mapData, base);
                int sector = bytesToInt(mapData, base + 4);
                chunkSectorMap.put(chunkId, sector);
                usedSectors.set(sector);
            }
        }
    }

    public byte[] readChunk(int chunkId, int expectedSize) {
        synchronized (this) {
            Integer sector = chunkSectorMap.get(chunkId);
            if (sector == null) return null;

            int sectorsNeeded = (expectedSize + SECTOR_SIZE - 1) / SECTOR_SIZE;
            channel.position((long) sector * SECTOR_SIZE);
            byte[] data = new byte[sectorsNeeded * SECTOR_SIZE];
            channel.read(ByteBuffer.wrap(data));

            return Arrays.copyOf(data, expectedSize);
        }
    }

    private int findFreeSectors(int count) {
        for (int i = HEADER_SECTORS; i < 0xFFFFF; i++) {
            if (i >= SECTOR_MAP_START && i < SECTOR_MAP_START + SECTOR_MAP_COUNT) continue;

            boolean free = true;
            for (int j = 0; j < count; j++) {
                if (usedSectors.get(i + j)) {
                    free = false;
                    break;
                }
            }

            if (free) return i;
        }

        throw new RuntimeException("No free sectors available for allocation.");
    }

    private static byte[] intToBytes(int v) {
        return new byte[] {
                (byte) (v >>> 24),
                (byte) (v >>> 16),
                (byte) (v >>> 8),
                (byte) v
        };
    }

    private static int bytesToInt(byte[] d, int i) {
        return ((d[i] & 0xFF) << 24)
                | ((d[i + 1] & 0xFF) << 16)
                | ((d[i + 2] & 0xFF) << 8)
                | (d[i + 3] & 0xFF);
    }

    // Read chunk data by ID
    public ByteBuffer getChunkData(int index) throws IOException {
        Integer sector = chunkSectorMap.get(index);
        if (sector == null) throw new IOException("Chunk ID not found: " + 0);

        channel.position((long) sector * SECTOR_SIZE);
        return channel.buffer();
    }

    public ByteBuffer writeChunkData(int index) throws IOException {
        writeChunk(index, new byte[4096]);
        return getChunkData(index);
    }

    public int getChunkIndex(int cx, int cy, int cz) {
        return (cy * CHUNK_GRID + cz) * CHUNK_GRID + cx;
    }

    @Override
    public void saveChunk(int cx, int cy, int cz, @NotNull MapType chunk) throws IOException {
        if (cx < 0 || cx >= CHUNK_GRID || cy < 0 || cy >= CHUNK_GRID || cz < 0 || cz >= CHUNK_GRID)
            throw new IndexOutOfBoundsException("Chunk coordinates out of bounds: " + cx + ", " + cy + ", " + cz);

        synchronized (this) {
            byte[] raw = UboObject.write(chunk);
            byte[] compressed = compress(raw);

            System.out.println("\n :: SAVING CHUNK " + cx + ", " + cy + ", " + cz);

            this.writeChunk(32 + getChunkIndex(cx, cy, cz), compressed);
            putLength(cx, cy, cz, compressed.length);
        }
    }

    @Override
    public @Nullable MapType loadChunk(int cx, int cy, int cz) throws IOException {
        if (cx < 0 || cx >= CHUNK_GRID || cy < 0 || cy >= CHUNK_GRID || cz < 0 || cz >= CHUNK_GRID)
            throw new IndexOutOfBoundsException("Chunk coordinates out of bounds: " + cx + ", " + cy + ", " + cz);

        synchronized (this) {
            int length = getLength(cx, cy, cz);
            byte[] input = readChunk(32 + getChunkIndex(cx, cy, cz), length);
            if (input == null) return null;
            byte[] decompress = decompress(input);
            return UboObject.fromBytes(decompress);
        }
    }

    private void putLength(int cx, int cy, int cz, int length) {
        int newPosition = getChunkIndex(cx, cy, cz) * 4;
        ByteBuffer chunkInfoSector = allChunkInfo[newPosition / 4096];
        chunkInfoSector.position(newPosition % 4096);
        chunkInfoSector.putInt(length);
    }

    private int getLength(int cx, int cy, int cz) {
        int newPosition = getChunkIndex(cx, cy, cz) * 4;
        ByteBuffer chunkInfoSector = allChunkInfo[newPosition / 4096];
        chunkInfoSector.position(newPosition % 4096);
        return chunkInfoSector.getInt();
    }

    private byte[] compress(byte[] input) {
//        Deflater deflater = new Deflater();
//        deflater.setInput(input);
//        deflater.finish();
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        while (!deflater.finished()) {
//            int len = deflater.deflate(buffer);
//            bos.write(buffer, 0, len);
//        }
//        deflater.end();
        return input;
    }

    private byte[] decompress(byte[] input) {
//        Inflater inflater = new Inflater();
//        inflater.setInput(input);
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        try {
//            while (!inflater.finished()) {
//                int len = inflater.inflate(buffer);
//                if (len == 0 && inflater.needsInput()) break;
//                bos.write(buffer, 0, len);
//            }
//        } catch (Exception e) {
//            throw new IOException("Decompression failed", e);
//        }
//        inflater.end();
        return input;
    }

    @Override
    public FileHandle getTarget() {
        return file;
    }
    
    @Override
    public void flush() throws IOException {
        channel.writeTo(file);
    }

    @Override
    public void close() throws IOException {
        flush();
        channel.close();
    }
}
