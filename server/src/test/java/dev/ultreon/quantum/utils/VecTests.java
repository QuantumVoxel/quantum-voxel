//package dev.ultreon.quantum.utils;
//
//import dev.ultreon.quantum.world.vec.*;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static dev.ultreon.quantum.world.World.CHUNK_SIZE;
//import static dev.ultreon.quantum.world.World.REGION_SIZE;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class VecTests {
//    @Test
//    @DisplayName("BlockVec world space validation test")
//    public void blockVecWorldSpaceValidationTest() {
//        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
//        assertEquals(0, vec.getX());
//        assertEquals(0, vec.getY());
//        assertEquals(0, vec.getZ());
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(15, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 15, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(15, 0, 15, BlockVecSpace.WORLD));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(511, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 511, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(511, 0, 511, BlockVecSpace.WORLD));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(4095, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 4095, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(4095, 0, 4095, BlockVecSpace.WORLD));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(65535, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 65535, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(65535, 0, 65535, BlockVecSpace.WORLD));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(-15, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, -15, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(-15, 0, -15, BlockVecSpace.WORLD));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(-511, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, -511, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(-511, 0, -511, BlockVecSpace.WORLD));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(-4095, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, -4095, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(-4095, 0, -4095, BlockVecSpace.WORLD));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(-65535, 0, 0, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, -65535, BlockVecSpace.WORLD));
//        assertDoesNotThrow(() -> new BlockVec(-65535, 0, -65535, BlockVecSpace.WORLD));
//    }
//
//    @Test
//    @DisplayName("BlockVec chunk space validation test")
//    public void blockVecChunkSpaceValidationTest() {
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 0, 0, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 0, -1, BlockVecSpace.CHUNK));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, 0, 0, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(15, 0, 0, BlockVecSpace.CHUNK));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 0, 16, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 15, BlockVecSpace.CHUNK));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, 0, 16, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(15, 0, 15, BlockVecSpace.CHUNK));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 0, 0, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.CHUNK));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 0, -1, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.CHUNK));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 0, -1, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.CHUNK));
//
//
//
//        assertDoesNotThrow(() -> new BlockVec(0, 256, 0, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, 256, 0, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 256, 16, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, 256, 16, BlockVecSpace.CHUNK));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 255, 0, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(15, 255, 0, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(0, 255, 15, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(15, 255, 15, BlockVecSpace.CHUNK));
//
//        assertDoesNotThrow(() -> new BlockVec(0, -1, 0, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, -1, 0, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, -1, 16, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, -1, 16, BlockVecSpace.CHUNK));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(15, 0, 0, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 15, BlockVecSpace.CHUNK));
//        assertDoesNotThrow(() -> new BlockVec(15, 0, 15, BlockVecSpace.CHUNK));
//
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 256, -1, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, 256, -1, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 256, 16, BlockVecSpace.CHUNK));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 255, -1, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(15, 255, -1, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 255, 15, BlockVecSpace.CHUNK));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, -1, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, -1, -1, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, 16, BlockVecSpace.CHUNK));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, -1, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(15, -1, -1, BlockVecSpace.CHUNK));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, 15, BlockVecSpace.CHUNK));
//    }
//
//    @Test
//    @DisplayName("BlockVec section space validation test")
//    public void blockVecSectionSpaceValidationTest() {
//        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
//        assertEquals(0, vec.getX());
//        assertEquals(0, vec.getY());
//        assertEquals(0, vec.getZ());
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 0, 0, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 0, -1, BlockVecSpace.SECTION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, 0, 0, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(15, 0, 0, BlockVecSpace.SECTION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 0, 16, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 15, BlockVecSpace.SECTION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, 0, 16, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(15, 0, 15, BlockVecSpace.SECTION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 0, 0, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.SECTION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 0, -1, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.SECTION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 0, -1, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.SECTION));
//
//
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 16, 0, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, 16, 0, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 16, 16, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, 16, 16, BlockVecSpace.SECTION));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 15, 0, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(15, 15, 0, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(0, 15, 15, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(15, 15, 15, BlockVecSpace.SECTION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, -1, 0, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, -1, 0, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, -1, 16, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, -1, 16, BlockVecSpace.SECTION));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(15, 0, 0, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 15, BlockVecSpace.SECTION));
//        assertDoesNotThrow(() -> new BlockVec(15, 0, 15, BlockVecSpace.SECTION));
//
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 16, -1, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, 16, -1, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 16, 16, BlockVecSpace.SECTION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 15, -1, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(15, 15, -1, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 15, 15, BlockVecSpace.SECTION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, -1, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(16, -1, -1, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, 16, BlockVecSpace.SECTION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, -1, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(15, -1, -1, BlockVecSpace.SECTION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, 15, BlockVecSpace.SECTION));
//    }
//
//    @Test
//    @DisplayName("BlockVec region space validation test")
//    public void blockVecRegionSpaceValidationTest() {
//        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
//        assertEquals(0, vec.getX());
//        assertEquals(0, vec.getY());
//        assertEquals(0, vec.getZ());
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 0, 0, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 0, -1, BlockVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(512, 0, 0, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(511, 0, 0, BlockVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 0, 512, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 511, BlockVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(512, 0, 512, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(511, 0, 511, BlockVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 0, 0, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 0, -1, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 0, -1, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.REGION));
//
//
//
//        assertDoesNotThrow(() -> new BlockVec(0, 256, 0, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(512, 256, 0, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, 256, 512, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(512, 256, 512, BlockVecSpace.REGION));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 255, 0, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(511, 255, 0, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(0, 255, 511, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(511, 255, 511, BlockVecSpace.REGION));
//
//        assertDoesNotThrow(() -> new BlockVec(0, -1, 0, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(512, -1, 0, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(0, -1, 512, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(512, -1, 512, BlockVecSpace.REGION));
//
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 0, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(511, 0, 0, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(0, 0, 511, BlockVecSpace.REGION));
//        assertDoesNotThrow(() -> new BlockVec(511, 0, 511, BlockVecSpace.REGION));
//
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 256, -1, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(512, 256, -1, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 256, 512, BlockVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 255, -1, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(511, 255, -1, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, 255, 511, BlockVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, -1, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(512, -1, -1, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, 512, BlockVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, -1, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(511, -1, -1, BlockVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new BlockVec(-1, -1, 511, BlockVecSpace.REGION));
//    }
//
//    @Test
//    @DisplayName("ChunkVec region space validation test")
//    public void chunkVecRegionSpaceValidationTest() {
//        ChunkVec vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
//        assertEquals(0, vec.getX());
//        assertEquals(0, vec.getY());
//        assertEquals(0, vec.getZ());
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, 0, 0, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(0, 0, -1, ChunkVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(32, 0, 0, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(31, 0, 0, ChunkVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(0, 0, 32, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(0, 0, 31, ChunkVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(32, 0, 32, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(31, 0, 31, ChunkVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, 0, 0, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(0, 0, 0, ChunkVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(0, 0, -1, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(0, 0, 0, ChunkVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, 0, -1, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(0, 0, 0, ChunkVecSpace.REGION));
//
//
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(0, 32, 0, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(32, 32, 0, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(0, 32, 32, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(32, 32, 32, ChunkVecSpace.REGION));
//
//        assertDoesNotThrow(() -> new ChunkVec(0, 31, 0, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(31, 31, 0, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(0, 31, 31, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(31, 31, 31, ChunkVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(0, -1, 0, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(32, -1, 0, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(0, -1, 32, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(32, -1, 32, ChunkVecSpace.REGION));
//
//        assertDoesNotThrow(() -> new ChunkVec(0, 0, 0, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(31, 0, 0, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(0, 0, 31, ChunkVecSpace.REGION));
//        assertDoesNotThrow(() -> new ChunkVec(31, 0, 31, ChunkVecSpace.REGION));
//
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, 32, -1, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(32, 32, -1, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, 32, 32, ChunkVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, 31, -1, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(31, 31, -1, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, 31, 31, ChunkVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, -1, -1, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(32, -1, -1, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, -1, 32, ChunkVecSpace.REGION));
//
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, -1, -1, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(31, -1, -1, ChunkVecSpace.REGION));
//        assertThrows(IllegalArgumentException.class, () -> new ChunkVec(-1, -1, 31, ChunkVecSpace.REGION));
//    }
//
//    @Test
//    @DisplayName("Chunk to local block")
//    public void chunkToLocalBlockTest() {
//        ChunkVec vec = new ChunkVec(4, 4, ChunkVecSpace.REGION);
//        BlockVec block = vec.blockAt(8, 8, 8);
//        assertEquals(block.x, 72);
//        assertEquals(block.y, 8);
//        assertEquals(block.z, 72);
//
//        block = vec.blockAt(0, 0, 0);
//        assertEquals(block.x, 64);
//        assertEquals(block.y, 0);
//        assertEquals(block.z, 64);
//
//        vec = new ChunkVec(64, 64, ChunkVecSpace.WORLD);
//        block = vec.blockAt(8, 8, 8);
//        assertEquals(block.x, 1032);
//        assertEquals(block.y, 8);
//        assertEquals(block.z, 1032);
//
//        block = vec.blockAt(0, 0, 0);
//        assertEquals(block.x, 1024);
//        assertEquals(block.y, 0);
//        assertEquals(block.z, 1024);
//    }
//
//    @Test
//    @DisplayName("Chunk to world space block")
//    public void chunkToWorldSpaceBlockTest() {
//        ChunkVec vec = new ChunkVec(16, 16, ChunkVecSpace.WORLD);
//        BlockVec block = vec.blockInWorldSpace(8, 8, 8, new RegionVec(0, 0, 0));
//        assertEquals(16 * CHUNK_SIZE + 8, block.x);
//        assertEquals(8, block.y);
//        assertEquals(16 * CHUNK_SIZE + 8, block.z);
//
//        block = vec.blockInWorldSpace(0, 0, 0, new RegionVec(0, 0, 0));
//        assertEquals(16 * CHUNK_SIZE, block.x);
//        assertEquals(0, block.y);
//        assertEquals(16 * CHUNK_SIZE, block.z);
//
//        vec = new ChunkVec(16, 16, ChunkVecSpace.REGION);
//        block = vec.blockInWorldSpace(8, 8, 8, new RegionVec(0, 0, 0));
//        assertEquals(16 * CHUNK_SIZE + 8, block.x);
//        assertEquals(8, block.y);
//        assertEquals(16 * CHUNK_SIZE + 8, block.z);
//
//        block = vec.blockInWorldSpace(0, 0, 0, new RegionVec(0, 0, 0));
//        assertEquals(16 * CHUNK_SIZE, block.x);
//        assertEquals(0, block.y);
//        assertEquals(16 * CHUNK_SIZE, block.z);
//
//        // With explicit region vector values
//        vec = new ChunkVec(16, 16, ChunkVecSpace.REGION);
//        block = vec.blockInWorldSpace(8, 8, 8, new RegionVec(2, 2));
//        assertEquals((2 * REGION_SIZE * CHUNK_SIZE) + (16 * CHUNK_SIZE) + 8, block.x);
//        assertEquals(8, block.y);
//        assertEquals((2 * REGION_SIZE * CHUNK_SIZE) + (16 * CHUNK_SIZE) + 8, block.z);
//
//        block = vec.blockInWorldSpace(0, 0, 0, new RegionVec(2, 2));
//        assertEquals((2 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE, block.x);
//        assertEquals(0, block.y);
//        assertEquals((2 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE, block.z);
//
//        block = vec.blockInWorldSpace(0, 0, 0, new RegionVec(8, 8));
//        assertEquals((8 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE, block.x);
//        assertEquals(0, block.y);
//        assertEquals((8 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE, block.z);
//
//        block = vec.blockInWorldSpace(0, 0, 0, new RegionVec(8, 8));
//        assertEquals((8 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE, block.x);
//        assertEquals(0, block.y);
//        assertEquals((8 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE, block.z);
//
//        // With explicit negative region vector values
//        vec = new ChunkVec(16, 16, ChunkVecSpace.REGION);
//        block = vec.blockInWorldSpace(8, 8, 8, new RegionVec(-2, -2));
//        assertEquals((-2 * REGION_SIZE * CHUNK_SIZE) + (16 * CHUNK_SIZE) + 8, block.x);
//        assertEquals(8, block.y);
//        assertEquals((-2 * REGION_SIZE * CHUNK_SIZE) + (16 * CHUNK_SIZE) + 8, block.z);
//
//        block = vec.blockInWorldSpace(0, 0, 0, new RegionVec(-2, -2));
//        assertEquals((-2 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE, block.x);
//        assertEquals(0, block.y);
//        assertEquals((-2 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE, block.z);
//
//        block = vec.blockInWorldSpace(8, 8, 8, new RegionVec(-8, -8));
//        assertEquals((-8 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE + 8, block.x);
//        assertEquals(8, block.y);
//        assertEquals((-8 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE + 8, block.z);
//
//        block = vec.blockInWorldSpace(0, 0, 0, new RegionVec(-8, -8));
//        assertEquals((-8 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE, block.x);
//        assertEquals(0, block.y);
//        assertEquals((-8 * REGION_SIZE * CHUNK_SIZE) + 16 * CHUNK_SIZE, block.z);
//    }
//}
