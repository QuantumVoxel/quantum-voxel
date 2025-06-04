package dev.ultreon.quantum.world.data;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class RegionDataChannel {
    static final int SECTOR_SIZE = 4096;       // Updated sector size
    private static final int SECTOR_MAP_START = 97;    // Starting sector of reference map
    private static final int SECTOR_MAP_COUNT = 64;     // Enough to store 4096 entries
    private static final int HEADER_SECTORS = 1;
    private static final int ENTRY_SIZE = 8;           // 4 bytes chunkId + 4 bytes sector

    private final BitSet usedSectors = new BitSet();
    private final Map<Integer, Integer> chunkSectorMap = new HashMap<>();
    private final RandomAccessFile file;
    private final File target;

    public RegionDataChannel(File target) throws IOException {
        this.target = target;
        this.file = new RandomAccessFile(target, "rw");
        usedSectors.set(0, HEADER_SECTORS); // Reserve header
    }

    public void writeChunk(int chunkId, byte[] data) throws IOException {
        synchronized (this) {
            int sectorsNeeded = (data.length + SECTOR_SIZE - 1) / SECTOR_SIZE;
            int startSector = findFreeSectors(sectorsNeeded);

            usedSectors.set(startSector, startSector + sectorsNeeded);

            file.seek((long) startSector * SECTOR_SIZE);
            file.write(data);

            // Pad to full sector size
            int padding = sectorsNeeded * SECTOR_SIZE - data.length;
            if (padding > 0) file.write(new byte[padding]);

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
            file.seek((long) SECTOR_MAP_START * SECTOR_SIZE);
            file.write(mapData);

            // Pad to fill entire reserved map space
            if (mapData.length < totalBytes) {
                file.write(new byte[totalBytes - mapData.length]);
            }

            usedSectors.set(SECTOR_MAP_START, SECTOR_MAP_START + SECTOR_MAP_COUNT);
        }
    }

    public void readSectorReferenceMap() throws IOException {
        synchronized (this) {
            file.seek((long) SECTOR_MAP_START * SECTOR_SIZE);
            byte[] header = new byte[4];
            file.readFully(header);
            int entryCount = bytesToInt(header, 0);

            int totalBytes = entryCount * ENTRY_SIZE + 4;
            int totalSectors = (totalBytes + SECTOR_SIZE - 1) / SECTOR_SIZE;

            byte[] mapData = new byte[totalSectors * SECTOR_SIZE];
            file.seek((long) SECTOR_MAP_START * SECTOR_SIZE);
            file.readFully(mapData);

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

    public byte[] readChunk(int chunkId, int expectedSize) throws IOException {
        synchronized (this) {
            Integer sector = chunkSectorMap.get(chunkId);
            if (sector == null) return null;

            int sectorsNeeded = (expectedSize + SECTOR_SIZE - 1) / SECTOR_SIZE;
            file.seek((long) sector * SECTOR_SIZE);
            byte[] data = new byte[sectorsNeeded * SECTOR_SIZE];
            file.readFully(data);

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

    public void close() throws IOException {
        file.close();
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

    // Demo
    public static void main(String[] args) throws IOException {
        File file = new File("chunks4096.dat");
        RegionDataChannel cf = new RegionDataChannel(file);

        for (int i = 0; i < 4096; i++) {
            cf.writeChunk(i, testData(200 + i % 100));
        }

        cf.writeSectorReferenceMap();
        cf.close();

        RegionDataChannel reader = new RegionDataChannel(file);
        reader.readSectorReferenceMap();

        byte[] data = reader.readChunk(4095, 200 + 4095 % 100);
        System.out.println("Read chunk 4095: length = " + data.length + ", checksum = " + checksum(data));

        reader.close();
    }

    private static byte[] testData(int size) {
        byte[] d = new byte[size];
        for (int i = 0; i < size; i++) d[i] = (byte) (i & 0xFF);
        return d;
    }

    private static int checksum(byte[] data) {
        int sum = 0;
        for (byte b : data) sum += (b & 0xFF);
        return sum;
    }
    
    // Read chunk data by ID
    public ByteBuffer getChunkData(int index) throws IOException {
        Integer sector = chunkSectorMap.get(index);
        if (sector == null) throw new IOException("Chunk ID not found: " + 0);

        file.seek((long) sector * SECTOR_SIZE);
        return file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, SECTOR_SIZE);
    }

    public ByteBuffer writeChunkData(int index) throws IOException {
        writeChunk(index, new byte[4096]);
        return getChunkData(index);
    }

    public File getTarget() {
        return target;
    }
}