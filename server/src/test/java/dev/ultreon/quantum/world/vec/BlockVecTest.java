package dev.ultreon.quantum.world.vec;

import dev.ultreon.quantum.world.CubicDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;
import static dev.ultreon.quantum.world.World.REGION_SIZE;
import static org.junit.jupiter.api.Assertions.*;

class BlockVecTest {
    @DisplayName("Method offset(x, y, z)")
    @Test
    void offset() {
        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        BlockVec offset = vec.offset(0, 0, 0);
        assertEquals(vec, offset);

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(1, 2, 3);
        assertEquals(1, offset.getX());
        assertEquals(2, offset.getY());
        assertEquals(3, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(-1, -2, -3);
        assertEquals(-1, offset.getX());
        assertEquals(-2, offset.getY());
        assertEquals(-3, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.DOWN);
        assertEquals(0, offset.getX());
        assertEquals(-1, offset.getY());
        assertEquals(0, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.UP);
        assertEquals(0, offset.getX());
        assertEquals(1, offset.getY());
        assertEquals(0, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.NORTH);
        assertEquals(0, offset.getX());
        assertEquals(0, offset.getY());
        assertEquals(-1, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.SOUTH);
        assertEquals(0, offset.getX());
        assertEquals(0, offset.getY());
        assertEquals(1, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.WEST);
        assertEquals(-1, offset.getX());
        assertEquals(0, offset.getY());
        assertEquals(0, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.EAST);
        assertEquals(1, offset.getX());
        assertEquals(0, offset.getY());
        assertEquals(0, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.DOWN, 2);
        assertEquals(0, offset.getX());
        assertEquals(-2, offset.getY());
        assertEquals(0, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.UP, 2);
        assertEquals(0, offset.getX());
        assertEquals(2, offset.getY());
        assertEquals(0, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.NORTH, 2);
        assertEquals(0, offset.getX());
        assertEquals(0, offset.getY());
        assertEquals(-2, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.SOUTH, 2);
        assertEquals(0, offset.getX());
        assertEquals(0, offset.getY());
        assertEquals(2, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.WEST, 2);
        assertEquals(-2, offset.getX());
        assertEquals(0, offset.getY());
        assertEquals(0, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offset(CubicDirection.EAST, 2);
        assertEquals(2, offset.getX());
        assertEquals(0, offset.getY());
        assertEquals(0, offset.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        offset = vec.offsetRegion(new RegionVec(2, 0, 2));
        assertEquals(2 * (REGION_SIZE * CHUNK_SIZE), offset.getX());
        assertEquals(0, offset.getY());
        assertEquals(2 * (REGION_SIZE * CHUNK_SIZE), offset.getZ());
    }

    @DisplayName("Method below()")
    @Test
    void below() {
        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        BlockVec below = vec.below();
        assertEquals(0, below.getX());
        assertEquals(-1, below.getY());
        assertEquals(0, below.getZ());

        vec = new BlockVec(0, -16, 0, BlockVecSpace.WORLD);
        below = vec.below();
        assertEquals(0, below.getX());
        assertEquals(-17, below.getY());
        assertEquals(0, below.getZ());

        vec = new BlockVec(0, 16, 0, BlockVecSpace.WORLD);
        below = vec.below();
        assertEquals(0, below.getX());
        assertEquals(15, below.getY());
        assertEquals(0, below.getZ());
    }

    @DisplayName("Method above()")
    @Test
    void above() {
        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        BlockVec above = vec.above();
        assertEquals(0, above.getX());
        assertEquals(1, above.getY());
        assertEquals(0, above.getZ());

        vec = new BlockVec(0, 16, 0, BlockVecSpace.WORLD);
        above = vec.above();
        assertEquals(0, above.getX());
        assertEquals(17, above.getY());
        assertEquals(0, above.getZ());

        vec = new BlockVec(0, -16, 0, BlockVecSpace.WORLD);
        above = vec.above();
        assertEquals(0, above.getX());
        assertEquals(-15, above.getY());
        assertEquals(0, above.getZ());
    }

    @DisplayName("Method dst(vec)")
    @Test
    void dst() {
        BlockVec vecStart = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        BlockVec vecEnd = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        assertEquals(0, vecStart.dst(vecEnd));

        vecEnd = new BlockVec(0, 10, 0, BlockVecSpace.WORLD);
        assertEquals(10, vecStart.dst(vecEnd));

        vecEnd = new BlockVec(0, -10, 0, BlockVecSpace.WORLD);
        assertEquals(10, vecStart.dst(vecEnd));

        vecEnd = new BlockVec(10, 0, 0, BlockVecSpace.WORLD);
        assertEquals(10, vecStart.dst(vecEnd));

        vecEnd = new BlockVec(-10, 0, 0, BlockVecSpace.WORLD);
        assertEquals(10, vecStart.dst(vecEnd));

        vecEnd = new BlockVec(0, 0, 10, BlockVecSpace.WORLD);
        assertEquals(10, vecStart.dst(vecEnd));

        vecEnd = new BlockVec(0, 0, -10, BlockVecSpace.WORLD);
        assertEquals(10, vecStart.dst(vecEnd));
    }

    @DisplayName("Method chunk()")
    @Test
    void chunk() {
        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        ChunkVec chunk = vec.chunk();
        assertEquals(0, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(0, chunk.getZ());

        vec = new BlockVec(18, 37, 18, BlockVecSpace.WORLD);
        chunk = vec.chunk();
        assertEquals(1, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(1, chunk.getZ());

        vec = new BlockVec(18, 0, 18, BlockVecSpace.WORLD);
        chunk = vec.chunk();
        assertEquals(1, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(1, chunk.getZ());

        vec = new BlockVec(18, -37, 18, BlockVecSpace.WORLD);
        chunk = vec.chunk();
        assertEquals(1, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(1, chunk.getZ());

        // Negative X,Z
        vec = new BlockVec(-18, 0, -18, BlockVecSpace.WORLD);
        chunk = vec.chunk();
        assertEquals(-2, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(-2, chunk.getZ());

        // Negative Y
        vec = new BlockVec(0, -37, 0, BlockVecSpace.WORLD);
        chunk = vec.chunk();
        assertEquals(0, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(0, chunk.getZ());

        // Negative X,Y,Z
        vec = new BlockVec(-18, -37, -18, BlockVecSpace.WORLD);
        chunk = vec.chunk();
        assertEquals(-2, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(-2, chunk.getZ());
    }

    @DisplayName("Method toSpace(space)")
    @Test
    void toSpace() {
        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        BlockVec vecWorld = vec.toSpace(BlockVecSpace.WORLD);
        assertEquals(vec, vecWorld);

        BlockVec vecChunk = vec.toSpace(BlockVecSpace.CHUNK);
        assertEquals(0, vecChunk.getX());
        assertEquals(0, vecChunk.getY());
        assertEquals(0, vecChunk.getZ());

        BlockVec vecSection = vec.toSpace(BlockVecSpace.SECTION);
        assertEquals(0, vecSection.getX());
        assertEquals(0, vecSection.getY());
        assertEquals(0, vecSection.getZ());

        BlockVec vecRegion = vec.toSpace(BlockVecSpace.REGION);
        assertEquals(0, vecRegion.getX());
        assertEquals(0, vecRegion.getY());
        assertEquals(0, vecRegion.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.CHUNK);
        vecChunk = vec.toSpace(BlockVecSpace.WORLD);
        assertEquals(0, vecChunk.getX());
        assertEquals(0, vecChunk.getY());
        assertEquals(0, vecChunk.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.SECTION);
        vecSection = vec.toSpace(BlockVecSpace.WORLD);
        assertEquals(0, vecSection.getX());
        assertEquals(0, vecSection.getY());
        assertEquals(0, vecSection.getZ());

        vec = new BlockVec(0, 0, 0, BlockVecSpace.REGION);
        vecRegion = vec.toSpace(BlockVecSpace.WORLD);
        assertEquals(0, vecRegion.getX());
        assertEquals(0, vecRegion.getY());
        assertEquals(0, vecRegion.getZ());

        // Convert large world space coordinates to different spaces
        vec = new BlockVec(1023, 1023, 1023, BlockVecSpace.WORLD);
        vecWorld = vec.toSpace(BlockVecSpace.CHUNK);
        assertEquals(15, vecWorld.getX());
        assertEquals(1023, vecWorld.getY());
        assertEquals(15, vecWorld.getZ());

        vec = new BlockVec(1023, 1023, 1023, BlockVecSpace.WORLD);
        vecWorld = vec.toSpace(BlockVecSpace.SECTION);
        assertEquals(15, vecWorld.getX());
        assertEquals(15, vecWorld.getY());
        assertEquals(15, vecWorld.getZ());

        vec = new BlockVec(1023, 1023, 1023, BlockVecSpace.WORLD);
        vecWorld = vec.toSpace(BlockVecSpace.REGION);
        assertEquals(511, vecWorld.getX());
        assertEquals(1023, vecWorld.getY());
        assertEquals(511, vecWorld.getZ());

        vec = new BlockVec(15, 1023, 15, BlockVecSpace.CHUNK);
        vecChunk = vec.toSpace(BlockVecSpace.WORLD);
        assertEquals(15, vecChunk.getX());
        assertEquals(1023, vecChunk.getY());
        assertEquals(15, vecChunk.getZ());

        vec = new BlockVec(15, 15, 15, BlockVecSpace.SECTION);
        vecSection = vec.toSpace(BlockVecSpace.WORLD);
        assertEquals(15, vecSection.getX());
        assertEquals(15, vecSection.getY());
        assertEquals(15, vecSection.getZ());

        vec = new BlockVec(511, 511, 511, BlockVecSpace.REGION);
        vecRegion = vec.toSpace(BlockVecSpace.WORLD);
        assertEquals(511, vecRegion.getX());
        assertEquals(511, vecRegion.getY());
        assertEquals(511, vecRegion.getZ());
    }

    @DisplayName("Method chunkLocal()")
    @Test
    void chunkLocal() {
        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        BlockVec chunk = vec.chunkLocal();

        assertEquals(0, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(0, chunk.getZ());

        vec = new BlockVec(-1, 0, 0, BlockVecSpace.WORLD);
        chunk = vec.chunkLocal();

        assertEquals(15, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(0, chunk.getZ());

        vec = new BlockVec(17, 0, 0, BlockVecSpace.WORLD);
        chunk = vec.chunkLocal();

        assertEquals(1, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(0, chunk.getZ());

        vec = new BlockVec(0, -1, 0, BlockVecSpace.WORLD);
        chunk = vec.chunkLocal();

        assertEquals(0, chunk.getX());
        assertEquals(-1, chunk.getY());
        assertEquals(0, chunk.getZ());

        vec = new BlockVec(0, 17, 0, BlockVecSpace.WORLD);
        chunk = vec.chunkLocal();

        assertEquals(0, chunk.getX());
        assertEquals(17, chunk.getY());
        assertEquals(0, chunk.getZ());

        vec = new BlockVec(0, 0, -1, BlockVecSpace.WORLD);
        chunk = vec.chunkLocal();

        assertEquals(0, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(15, chunk.getZ());

        vec = new BlockVec(0, 0, 17, BlockVecSpace.WORLD);
        chunk = vec.chunkLocal();

        assertEquals(0, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(1, chunk.getZ());

        vec = new BlockVec(0, 0, -17, BlockVecSpace.WORLD);
        chunk = vec.chunkLocal();

        assertEquals(0, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(15, chunk.getZ());

        vec = new BlockVec(0, -17, 0, BlockVecSpace.WORLD);
        chunk = vec.chunkLocal();

        assertEquals(0, chunk.getX());
        assertEquals(-17, chunk.getY());
        assertEquals(0, chunk.getZ());

        vec = new BlockVec(-17, 0, 0, BlockVecSpace.WORLD);
        chunk = vec.chunkLocal();

        assertEquals(15, chunk.getX());
        assertEquals(0, chunk.getY());
        assertEquals(0, chunk.getZ());
    }

    @DisplayName("Method sectionLocal()")
    @Test
    void sectionLocal() {
        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        BlockVec section = vec.sectionLocal();

        assertEquals(0, section.getX());
        assertEquals(0, section.getY());
        assertEquals(0, section.getZ());

        vec = new BlockVec(0, 0, 17, BlockVecSpace.WORLD);
        section = vec.sectionLocal();

        assertEquals(0, section.getX());
        assertEquals(0, section.getY());
        assertEquals(1, section.getZ());

        vec = new BlockVec(0, 17, 0, BlockVecSpace.WORLD);
        section = vec.sectionLocal();

        assertEquals(0, section.getX());
        assertEquals(1, section.getY());
        assertEquals(0, section.getZ());

        vec = new BlockVec(17, 0, 0, BlockVecSpace.WORLD);
        section = vec.sectionLocal();

        assertEquals(1, section.getX());
        assertEquals(0, section.getY());
        assertEquals(0, section.getZ());

        vec = new BlockVec(0, 0, -1, BlockVecSpace.WORLD);
        section = vec.sectionLocal();

        assertEquals(0, section.getX());
        assertEquals(0, section.getY());
        assertEquals(15, section.getZ());

        vec = new BlockVec(0, -1, 0, BlockVecSpace.WORLD);
        section = vec.sectionLocal();

        assertEquals(0, section.getX());
        assertEquals(15, section.getY());
        assertEquals(0, section.getZ());

        vec = new BlockVec(-1, 0, 0, BlockVecSpace.WORLD);
        section = vec.sectionLocal();

        assertEquals(15, section.getX());
        assertEquals(0, section.getY());
        assertEquals(0, section.getZ());

        vec = new BlockVec(0, 0, -17, BlockVecSpace.WORLD);
        section = vec.sectionLocal();

        assertEquals(0, section.getX());
        assertEquals(0, section.getY());
        assertEquals(15, section.getZ());

        vec = new BlockVec(0, -17, 0, BlockVecSpace.WORLD);
        section = vec.sectionLocal();

        assertEquals(0, section.getX());
        assertEquals(15, section.getY());
        assertEquals(0, section.getZ());

        vec = new BlockVec(-17, 0, 0, BlockVecSpace.WORLD);
        section = vec.sectionLocal();

        assertEquals(15, section.getX());
        assertEquals(0, section.getY());
        assertEquals(0, section.getZ());
    }

    @DisplayName("Method regionLocal()")
    @Test
    void regionLocal() {
        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        BlockVec region = vec.regionLocal();

        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new BlockVec(513, 0, 0, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(1, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new BlockVec(0, 513, 0, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(0, region.getX());
        assertEquals(513, region.getY());
        assertEquals(0, region.getZ());

        vec = new BlockVec(0, 0, 513, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(1, region.getZ());

        vec = new BlockVec(0, 0, -1, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(511, region.getZ());

        vec = new BlockVec(0, -1, 0, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(0, region.getX());
        assertEquals(-1, region.getY());
        assertEquals(0, region.getZ());

        vec = new BlockVec(-1, 0, 0, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(511, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new BlockVec(0, 0, 17, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(17, region.getZ());

        vec = new BlockVec(0, 17, 0, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(0, region.getX());
        assertEquals(17, region.getY());
        assertEquals(0, region.getZ());

        vec = new BlockVec(17, 0, 0, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(17, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new BlockVec(0, 0, -513, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(511, region.getZ());

        vec = new BlockVec(0, -513, 0, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(0, region.getX());
        assertEquals(-513, region.getY());
        assertEquals(0, region.getZ());

        vec = new BlockVec(-513, 0, 0, BlockVecSpace.WORLD);
        region = vec.regionLocal();

        assertEquals(511, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());
    }

    @DisplayName("Method local(space)")
    @Test
    void local() {
        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        BlockVec local = vec.local(BlockVecSpace.WORLD);

        assertEquals(0, local.getX());
        assertEquals(0, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(1024, 0, 0, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.WORLD);

        assertEquals(1024, local.getX());
        assertEquals(0, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(0, 1024, 0, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.WORLD);

        assertEquals(0, local.getX());
        assertEquals(1024, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(0, 0, 1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.WORLD);

        assertEquals(0, local.getX());
        assertEquals(0, local.getY());
        assertEquals(1024, local.getZ());

        vec = new BlockVec(1024, 1024, 1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.CHUNK);

        assertEquals(0, local.getX());
        assertEquals(1024, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(1023, 1024, 1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.CHUNK);

        assertEquals(15, local.getX());
        assertEquals(1024, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(1024, 1023, 1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.CHUNK);

        assertEquals(0, local.getX());
        assertEquals(1023, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(1024, 1024, 1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.SECTION);

        assertEquals(0, local.getX());
        assertEquals(0, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(1023, 1023, 1023, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.SECTION);

        assertEquals(15, local.getX());
        assertEquals(15, local.getY());
        assertEquals(15, local.getZ());

        vec = new BlockVec(1024, 1023, 1023, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.SECTION);

        assertEquals(0, local.getX());
        assertEquals(15, local.getY());
        assertEquals(15, local.getZ());

        vec = new BlockVec(1023, 1024, 1023, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.SECTION);

        assertEquals(15, local.getX());
        assertEquals(0, local.getY());
        assertEquals(15, local.getZ());

        vec = new BlockVec(1023, 1023, 1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.SECTION);

        assertEquals(15, local.getX());
        assertEquals(15, local.getY());
        assertEquals(0, local.getZ());

        // Negative values
        vec = new BlockVec(-1024, 0, 0, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.WORLD);

        assertEquals(-1024, local.getX());
        assertEquals(0, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(0, -1024, 0, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.WORLD);

        assertEquals(0, local.getX());
        assertEquals(-1024, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(0, 0, -1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.WORLD);

        assertEquals(0, local.getX());
        assertEquals(0, local.getY());
        assertEquals(-1024, local.getZ());

        vec = new BlockVec(-1024, -1024, -1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.CHUNK);

        assertEquals(0, local.getX());
        assertEquals(-1024, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(-1023, -1024, -1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.CHUNK);

        assertEquals(1, local.getX());
        assertEquals(-1024, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(-1024, -1023, -1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.CHUNK);

        assertEquals(0, local.getX());
        assertEquals(-1023, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(-1024, -1024, -1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.SECTION);

        assertEquals(0, local.getX());
        assertEquals(0, local.getY());
        assertEquals(0, local.getZ());

        vec = new BlockVec(-1023, -1023, -1023, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.SECTION);

        assertEquals(1, local.getX());
        assertEquals(1, local.getY());
        assertEquals(1, local.getZ());

        vec = new BlockVec(-1024, -1023, -1023, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.SECTION);

        assertEquals(0, local.getX());
        assertEquals(1, local.getY());
        assertEquals(1, local.getZ());

        vec = new BlockVec(-1023, -1024, -1023, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.SECTION);

        assertEquals(1, local.getX());
        assertEquals(0, local.getY());
        assertEquals(1, local.getZ());

        vec = new BlockVec(-1023, -1023, -1024, BlockVecSpace.WORLD);
        local = vec.local(BlockVecSpace.SECTION);

        assertEquals(1, local.getX());
        assertEquals(1, local.getY());
        assertEquals(0, local.getZ());
    }

    @DisplayName("Method region()")
    @Test
    void region() {
        BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
        RegionVec region = vec.region();

        assertEquals(0, region.getX());
        assertEquals(0, region.getZ());

        vec = new BlockVec(1023, 1023, 1023, BlockVecSpace.WORLD);
        region = vec.region();

        assertEquals(1, region.getX());
        assertEquals(1, region.getZ());

        vec = new BlockVec(1024, 1024, 1024, BlockVecSpace.WORLD);
        region = vec.region();

        assertEquals(2, region.getX());
        assertEquals(2, region.getZ());

        vec = new BlockVec(-512, -512, -512, BlockVecSpace.WORLD);
        region = vec.region();

        assertEquals(-1, region.getX());
        assertEquals(-1, region.getZ());

        vec = new BlockVec(-511, -511, -511, BlockVecSpace.WORLD);
        region = vec.region();

        assertEquals(-1, region.getX());
        assertEquals(-1, region.getZ());

        vec = new BlockVec(-1024, -1024, -1024, BlockVecSpace.WORLD);
        region = vec.region();

        assertEquals(-2, region.getX());
        assertEquals(-2, region.getZ());

        vec = new BlockVec(-1023, -1023, -1023, BlockVecSpace.WORLD);
        region = vec.region();

        assertEquals(-2, region.getX());
        assertEquals(-2, region.getZ());
    }
}