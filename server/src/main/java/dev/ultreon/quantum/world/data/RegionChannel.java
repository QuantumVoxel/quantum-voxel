package dev.ultreon.quantum.world.data;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.World;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class RegionChannel implements AutoCloseable {
    private static final int HEADER_SIZE = 4096;

    private static final int CHUNK_GRID = World.REGION_SIZE;
    private static final int CHUNK_COUNT = CHUNK_GRID * CHUNK_GRID * CHUNK_GRID;
    private static final int INDEX_ENTRY_SIZE = 12;
    private static final int INDEX_TABLE_SIZE = CHUNK_COUNT * INDEX_ENTRY_SIZE;
    private static final int INDEX_OFFSET = HEADER_SIZE;

    private final RegionDataChannel channel;
    private final ByteBuffer[] allChunkInfo = new ByteBuffer[32];

    public RegionChannel(File path) throws IOException {
        if (Files.notExists(path.toPath().getParent())) {
            Files.createDirectories(path.toPath().getParent());
        }

        boolean exists = path.exists();

        this.channel = new RegionDataChannel(path);
        if (!exists) for (int i = 0; i < 32; i++) allChunkInfo[i] = channel.writeChunkData(i);
        else for (int i = 0; i < 32; i++) {
            channel.readSectorReferenceMap();
            allChunkInfo[i] = channel.getChunkData(i);
        }
        flush();
    }

    private int getChunkIndex(int cx, int cy, int cz) {
        return (cy * CHUNK_GRID + cz) * CHUNK_GRID + cx;
    }

    public void saveChunk(int cx, int cy, int cz, MapType chunk) throws IOException {
        if (cx < 0 || cx >= CHUNK_GRID || cy < 0 || cy >= CHUNK_GRID || cz < 0 || cz >= CHUNK_GRID)
            throw new IndexOutOfBoundsException("Chunk coordinates out of bounds: " + cx + ", " + cy + ", " + cz);

        synchronized (this) {
            byte[] raw = UboObject.write(chunk);
            byte[] compressed = compress(raw);

            System.out.println("\n :: SAVING CHUNK " + cx + ", " + cy + ", " + cz);

            this.channel.writeChunk(32 + getChunkIndex(cx, cy, cz), compressed);
            putLength(cx, cy, cz, compressed.length);
        }
    }

    public MapType loadChunk(int cx, int cy, int cz) throws IOException {
        if (cx < 0 || cx >= CHUNK_GRID || cy < 0 || cy >= CHUNK_GRID || cz < 0 || cz >= CHUNK_GRID)
            throw new IndexOutOfBoundsException("Chunk coordinates out of bounds: " + cx + ", " + cy + ", " + cz);

        synchronized (this) {
            int length = getLength(cx, cy, cz);
            byte[] input = channel.readChunk(32 + getChunkIndex(cx, cy, cz), length);
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

    private byte[] decompress(byte[] input) throws IOException {
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

    public void close() throws IOException {
        flush();
        channel.close();
    }

    public void flush() throws IOException {
        synchronized (this) {
            channel.writeSectorReferenceMap();
            CommonConstants.LOGGER.debug("Flushed region channel: " + channel.getTarget());
        }
    }
}
