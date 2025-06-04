package dev.ultreon.quantum.world.data;

import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.World;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class RegionChannelTest {
    static Path path = Path.of("build/test_folder");
    static {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * Tests for the saveChunk and loadChunk methods in the QVRegionChannel class.
     * <p>
     * The saveChunk method is responsible for saving a chunk of data identified by its x, y, z coordinates into a region file.
     * The loadChunk method is responsible for reading the saved chunk data from the region file.
     * These methods work together to write and retrieve chunk data in the QVRegionChannel system.
     */

    @Test
    public void testSaveChunk_ValidChunk_Success() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_0.qvr").toFile();
        RegionChannel channel = new RegionChannel(testFile);
        MapType mapType = new MapType();
        mapType.putString("key", "value");

        // Act
        channel.saveChunk(0, 0, 0, mapType);
        MapType loadedChunk = channel.loadChunk(0, 0, 0);

        // Assert
        assertNotNull(loadedChunk);
        assertEquals("value", loadedChunk.getString("key"));

        channel.close();
        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }

    @Test
    public void testSaveChunk_OverwriteExistingChunk_Success() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_1.qvr").toFile();
        RegionChannel channel = new RegionChannel(testFile);
        MapType mapType1 = new MapType();
        mapType1.putString("key1", "value1");
        MapType mapType2 = new MapType();
        mapType2.putString("key2", "value2");

        // Act
        channel.saveChunk(0, 0, 0, mapType1);
        channel.saveChunk(0, 0, 0, mapType2);
        MapType loadedChunk = channel.loadChunk(0, 0, 0);

        // Assert
        assertNotNull(loadedChunk);
        assertNull(loadedChunk.getString("key1", null));
        assertEquals("value2", loadedChunk.getString("key2"));

        channel.close();
    }

    @Test
    public void testSaveChunk_MultipleChunks_Success() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_2.qvr").toFile();
        RegionChannel channel = new RegionChannel(testFile);

        MapType mapType1 = new MapType();
        mapType1.putString("chunk1Key", "chunk1Value");

        MapType mapType2 = new MapType();
        mapType2.putString("chunk2Key", "chunk2Value");

        // Act
        channel.saveChunk(0, 0, 0, mapType1);
        channel.saveChunk(1, 0, 0, mapType2);

        MapType loadedChunk1 = channel.loadChunk(0, 0, 0);
        MapType loadedChunk2 = channel.loadChunk(1, 0, 0);

        // Assert
        assertNotNull(loadedChunk1);
        assertNotNull(loadedChunk2);
        assertEquals("chunk1Value", loadedChunk1.getString("chunk1Key"));
        assertEquals("chunk2Value", loadedChunk2.getString("chunk2Key"));

        channel.close();
        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }

    @Test
    public void testSaveChunk_EmptyChunk_Success() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_3.qvr").toFile();
        RegionChannel channel = new RegionChannel(testFile);
        MapType emptyChunk = new MapType();

        // Act
        channel.saveChunk(2, 2, 2, emptyChunk);
        MapType loadedChunk = channel.loadChunk(2, 2, 2);

        // Assert
        assertNotNull(loadedChunk);
        assertTrue(loadedChunk.isEmpty());

        channel.close();
        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }

    @Test
    public void testSaveChunk_InvalidCoordinates_Failure() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_4.qvr").toFile();
        RegionChannel channel = new RegionChannel(testFile);
        MapType mapType = new MapType();
        mapType.putString("key", "value");

        // Act and Assert
        assertThrows(IndexOutOfBoundsException.class, () -> channel.saveChunk(-1, 0, 0, mapType));

        channel.close();
        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }

    @Test
    public void testSaveAndLoadChunk_SameDataIntegrity() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_5.qvr").toFile();
        RegionChannel channel = new RegionChannel(testFile);

        MapType originalChunk = new MapType();
        originalChunk.putString("key1", "value1");
        originalChunk.putInt("key2", 42);
        originalChunk.putBoolean("key3", true);

        // Act
        channel.saveChunk(3, 3, 3, originalChunk);
        MapType loadedChunk = channel.loadChunk(3, 3, 3);

        // Assert
        assertNotNull(loadedChunk);
        assertEquals("value1", loadedChunk.getString("key1"));
        assertEquals(42, loadedChunk.getInt("key2"));
        assertTrue(loadedChunk.getBoolean("key3"));

        channel.close();
        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }

    @Test
    public void testSaveAndLoadChunk_ManagesMissingChunk() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_6.qvr").toFile();
        RegionChannel channel = new RegionChannel(testFile);

        // Act
        MapType loadedChunk = channel.loadChunk(4, 4, 4);

        // Assert
        assertNull(loadedChunk);

        channel.close();
        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }

    @Test
    public void testSaveAndLoadChunk_PersistsAcrossSessions() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_7.qvr").toFile();
        MapType originalChunk = new MapType();
        originalChunk.putString("sessionKey", "sessionValue");

        // Act
        try (RegionChannel channel = new RegionChannel(testFile)) {
            channel.saveChunk(5, 5, 5, originalChunk);
        }

        MapType loadedChunk;
        try (RegionChannel channel = new RegionChannel(testFile)) {
            loadedChunk = channel.loadChunk(5, 5, 5);
        }

        // Assert
        assertNotNull(loadedChunk);
        assertEquals("sessionValue", loadedChunk.getString("sessionKey"));

        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }

    @Test
    public void testSaveAndLoadChunk_PersistsAcrossSessionsLargeData() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_7.qvr").toFile();
        MapType originalChunk = new MapType();
        int[] largeData = new int[1024 * 1024];
        Random random = new Random();
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = random.nextInt();
        }
        originalChunk.putIntArray("largeData", largeData);

        // Act
        try (RegionChannel channel = new RegionChannel(testFile)) {
            channel.saveChunk(5, 5, 5, originalChunk);
        }

        MapType loadedChunk;
        try (RegionChannel channel = new RegionChannel(testFile)) {
            loadedChunk = channel.loadChunk(5, 5, 5);
        }

        // Assert
        assertNotNull(loadedChunk);
        assertArrayEquals(largeData, loadedChunk.getIntArray("largeData"));

        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }
    
    

    @Test
    public void testConcurrentChunkOperations() throws IOException, InterruptedException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_8.qvr").toFile();
        RegionChannel channel = new RegionChannel(testFile);

        // Act
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    MapType chunk = new MapType();
                    chunk.putInt("threadId", index);
                    channel.saveChunk(index, 0, 0, chunk);
                } catch (IOException e) {
                    fail("Concurrent operation failed: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        for (int i = 0; i < 10; i++) {
            MapType loadedChunk = channel.loadChunk(i, 0, 0);
            assertNotNull(loadedChunk);
            assertEquals(i, loadedChunk.getInt("threadId"));
        }

        channel.close();
        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }

    @Test
    public void testLargeDataChunk() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test_large.");
        File testFile = tempDir.resolve("region_0_0_9.qvr").toFile();
        RegionChannel channel = new RegionChannel(testFile);
        MapType largeChunk = new MapType();

        // Create large data (1MB)
        Random random = new Random();
        int[] largeData = new int[5 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = random.nextInt();
        }
        largeChunk.putIntArray("largeData", largeData);

        // Act
        channel.saveChunk(0, 0, 0, largeChunk);
        MapType loadedChunk = channel.loadChunk(0, 0, 0);
        assertEquals(largeChunk, loadedChunk);

        // Assert
        assertNotNull(loadedChunk);
        assertEquals(5 * 1024, loadedChunk.getIntArray("largeData").length);

        channel.close();
    }

    @Test
    public void testAllChunkOperationsAndPersistence() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_all_chunks.qvr").toFile();
        Random random = new Random();

        // First session - write all chunks
        try (RegionChannel channel = new RegionChannel(testFile)) {
            for (int x = 0; x < World.REGION_SIZE; x++) {
                for (int y = 0; y < World.REGION_SIZE; y++) {
                    for (int z = 0; z < World.REGION_SIZE; z++) {
                        MapType chunk = new MapType();
                        chunk.putString("pos", x + "," + y + "," + z);
                        int[] largeData = new int[16384];
                        for (int i = 0; i < largeData.length; i++) {
                            largeData[i] = random.nextInt();
                        }
                        chunk.putIntArray("data", largeData);
                        channel.saveChunk(x, y, z, chunk);
                    }
                }
            }
        }

        // Second session - verify all chunks
        try (RegionChannel channel = new RegionChannel(testFile)) {
            for (int x = 0; x < World.REGION_SIZE; x++) {
                for (int y = 0; y < World.REGION_SIZE; y++) {
                    for (int z = 0; z < World.REGION_SIZE; z++) {
                        MapType loadedChunk = channel.loadChunk(x, y, z);
                        assertNotNull(loadedChunk);
                        assertEquals(x + "," + y + "," + z, loadedChunk.getString("pos"));
                        assertNotNull(loadedChunk.getIntArray("data"));
                        assertEquals(16384, loadedChunk.getIntArray("data").length);
                    }
                }
            }
        }

//        Files.delete(testFile.toPath());
//        Files.delete(tempDir);
    }

    @Test
    public void testBoundaryCoordinates() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_10.qvr").toFile();
        RegionChannel channel = new RegionChannel(testFile);
        MapType chunk = new MapType();
        chunk.putString("test", "boundary");

        // Act & Assert
        assertThrows(IndexOutOfBoundsException.class, () -> channel.saveChunk(World.REGION_SIZE, 0, 0, chunk));
        assertThrows(IndexOutOfBoundsException.class, () -> channel.saveChunk(0, World.REGION_SIZE, 0, chunk));
        assertThrows(IndexOutOfBoundsException.class, () -> channel.saveChunk(0, 0, World.REGION_SIZE, chunk));

        channel.saveChunk(World.REGION_SIZE - 1, World.REGION_SIZE - 1, World.REGION_SIZE - 1, chunk);
        MapType loadedChunk = channel.loadChunk(World.REGION_SIZE - 1, World.REGION_SIZE - 1, World.REGION_SIZE - 1);
        assertNotNull(loadedChunk);
        assertEquals("boundary", loadedChunk.getString("test"));

        channel.close();
        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }

    @Test
    public void testCorruptedFileHandling() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(path, "qvregion_test");
        File testFile = tempDir.resolve("region_0_0_11.qvr").toFile();

        // Create corrupted file
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write("corrupted data".getBytes());
        }

        // Act & Assert
        assertThrows(IOException.class, () -> {
            try (RegionChannel channel = new RegionChannel(testFile)) {
                channel.loadChunk(0, 0, 0);
            }
        });

        Files.delete(testFile.toPath());
        Files.delete(tempDir);
    }
}