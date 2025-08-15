package dev.ultreon.quantum.desktop.platform.region;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.data.RegionChannelLike;
import dev.ultreon.quantum.world.data.UboObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class RegionChannel implements RegionChannelLike {
    private final RegionDataChannel channel;
    private final ByteBuffer[] allChunkInfo = new ByteBuffer[32];

    public RegionChannel(FileHandle path) throws IOException {
        if (Files.notExists(path.file().toPath().getParent())) {
            Files.createDirectories(path.file().toPath().getParent());
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

            this.channel.writeChunk(32 + getChunkIndex(cx, cy, cz), compressed);
            putLength(cx, cy, cz, compressed.length);
        }
    }

    @Override
    public @Nullable MapType loadChunk(int cx, int cy, int cz) throws IOException {
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

    @Override
    public FileHandle getTarget() {
        return channel.getTarget();
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
    public void close() throws IOException {
        flush();
        channel.close();
    }

    @Override
    public void flush() throws IOException {
        synchronized (this) {
            channel.writeSectorReferenceMap();
            CommonConstants.LOGGER.debug("Flushed region channel: " + channel.getTarget());
        }
    }
}
