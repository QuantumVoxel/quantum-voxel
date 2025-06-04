package dev.ultreon.quantum.world.data;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class RegionDataChannelTest {

    /**
     * Tests writing and reading of a chunk in the RegionDataChannel.
     * Verifies that the written data matches the data read back from the file.
     */
    @Test
    void testWriteAndReadChunk() throws IOException {
        File testFile = Files.createTempFile("testWriteAndReadChunk", ".dat").toFile();
        testFile.deleteOnExit();

        RegionDataChannel regionDataChannel = new RegionDataChannel(testFile);

        byte[] testData = createTestData(800); // Create test data of size 800 bytes

        regionDataChannel.writeChunk(1, testData);
        regionDataChannel.writeSectorReferenceMap();

        byte[] readData = regionDataChannel.readChunk(1, testData.length);

        assertArrayEquals(testData, readData, "Written and read data do not match");

        regionDataChannel.close();
    }

    /**
     * Tests writing chunks with large data sizes in the RegionDataChannel.
     * Verifies the integrity of large data when written and read back.
     */
    @Test
    void testWriteAndReadLargeData() throws IOException {
        File testFile = Files.createTempFile("testWriteAndReadLargeData", ".dat").toFile();
        testFile.deleteOnExit();

        RegionDataChannel regionDataChannel = new RegionDataChannel(testFile);

        byte[] largeData = createTestData(RegionDataChannel.SECTOR_SIZE * 5); // Large data spanning multiple sectors

        regionDataChannel.writeChunk(2, largeData);
        regionDataChannel.writeSectorReferenceMap();

        byte[] readData = regionDataChannel.readChunk(2, largeData.length);

        assertArrayEquals(largeData, readData, "Large written and read data do not match");

        regionDataChannel.close();
    }

    /**
     * Tests that writing multiple chunks and reading them back works correctly.
     * Ensures data integrity for multiple chunks.
     */
    @Test
    void testWriteAndReadMultipleChunks() throws IOException {
        File testFile = Files.createTempFile("testWriteAndReadMultipleChunks", ".dat").toFile();
        testFile.deleteOnExit();

        RegionDataChannel regionDataChannel = new RegionDataChannel(testFile);

        byte[] data1 = createTestData(500);
        byte[] data2 = createTestData(1500);
        byte[] data3 = createTestData(1000);

        regionDataChannel.writeChunk(1, data1);
        regionDataChannel.writeChunk(2, data2);
        regionDataChannel.writeChunk(3, data3);

        regionDataChannel.writeSectorReferenceMap();

        byte[] readData1 = regionDataChannel.readChunk(1, data1.length);
        byte[] readData2 = regionDataChannel.readChunk(2, data2.length);
        byte[] readData3 = regionDataChannel.readChunk(3, data3.length);

        assertArrayEquals(data1, readData1, "Chunk 1 data mismatch");
        assertArrayEquals(data2, readData2, "Chunk 2 data mismatch");
        assertArrayEquals(data3, readData3, "Chunk 3 data mismatch");

        regionDataChannel.close();
    }

    /**
     * Tests if the class correctly handles very large data sizes.
     * Verifies that no unexpected errors or integrity issues occur when handling large amounts of data.
     */
    @Test
    void testWriteAndReadVeryLargeDataSize() throws IOException {
        File testFile = Files.createTempFile("testWriteAndReadVeryLargeDataSize", ".dat").toFile();
        testFile.deleteOnExit();

        RegionDataChannel regionDataChannel = new RegionDataChannel(testFile);

        byte[] veryLargeData = createTestData(122_880_000); // Very large data set

        regionDataChannel.writeChunk(4, veryLargeData);
        regionDataChannel.writeSectorReferenceMap();

        byte[] readData = regionDataChannel.readChunk(4, veryLargeData.length);

        assertArrayEquals(veryLargeData, readData, "Very large written and read data do not match");

        regionDataChannel.close();
    }

    private static final Random random = new Random();

    private byte[] createTestData(int size) {
        byte[] data = new byte[size];
        random.nextBytes(data);
        return data;
    }
}