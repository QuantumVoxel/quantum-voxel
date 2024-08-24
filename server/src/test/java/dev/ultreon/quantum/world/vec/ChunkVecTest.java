package dev.ultreon.quantum.world.vec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChunkVecTest {

    @Test
    void offset() {
        ChunkVec vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        ChunkVec offset = vec.offset(0, 0, 0);
        assertEquals(vec, offset);

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        offset = vec.offset(1, 2, 3);
        assertEquals(1, offset.getX());
        assertEquals(2, offset.getZ());

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        offset = vec.offset(-1, -2, -3);
        assertEquals(-1, offset.getX());
        assertEquals(-2, offset.getZ());

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        offset = vec.offset(0, -1, 0);
        assertEquals(0, offset.getX());
        assertEquals(-1, offset.getY());
        assertEquals(0, offset.getZ());

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        offset = vec.offset(0, 0, -1);
        assertEquals(0, offset.getX());
        assertEquals(1, offset.getZ());
    }

    @Test
    void regionLocal() {
        ChunkVec vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        ChunkVec region = vec.regionLocal();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(33, 0, 0, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(1, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(0, 33, 0, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(0, region.getX());
        assertEquals(1, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(0, 0, 33, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(33, 33, 33, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(1, region.getX());
        assertEquals(1, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(-33, -33, -33, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(31, region.getX());
        assertEquals(31, region.getY());
        assertEquals(31, region.getZ());

        vec = new ChunkVec(-33, 33, 33, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(31, region.getX());
        assertEquals(1, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(33, -33, 33, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(1, region.getX());
        assertEquals(31, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(33, 33, -33, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(1, region.getX());
        assertEquals(1, region.getY());
        assertEquals(31, region.getZ());

        vec = new ChunkVec(33, -33, -33, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(1, region.getX());
        assertEquals(31, region.getY());
        assertEquals(31, region.getZ());

        vec = new ChunkVec(-33, 33, -33, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(31, region.getX());
        assertEquals(1, region.getY());
        assertEquals(31, region.getZ());

        vec = new ChunkVec(-33, -33, 33, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(31, region.getX());
        assertEquals(31, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(-33, 33, -33, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(31, region.getX());
        assertEquals(1, region.getY());
        assertEquals(31, region.getZ());

        vec = new ChunkVec(-1, 33, -1, ChunkVecSpace.WORLD);
        region = vec.regionLocal();
        assertEquals(31, region.getX());
        assertEquals(1, region.getY());
        assertEquals(31, region.getZ());
    }

    @Test
    void regionSpace() {
        ChunkVec vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        ChunkVec region = vec.regionSpace();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(33, 0, 0, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(1, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(0, 33, 0, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(0, region.getX());
        assertEquals(1, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(0, 0, 33, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(33, 33, 33, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(1, region.getX());
        assertEquals(1, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(-33, -33, -33, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(31, region.getX());
        assertEquals(31, region.getY());
        assertEquals(31, region.getZ());

        vec = new ChunkVec(-33, 33, 33, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(31, region.getX());
        assertEquals(1, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(33, -33, 33, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(1, region.getX());
        assertEquals(31, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(33, 33, -33, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(1, region.getX());
        assertEquals(1, region.getY());
        assertEquals(31, region.getZ());

        vec = new ChunkVec(33, -33, -33, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(1, region.getX());
        assertEquals(31, region.getY());
        assertEquals(31, region.getZ());

        vec = new ChunkVec(-33, 33, -33, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(31, region.getX());
        assertEquals(1, region.getY());
        assertEquals(31, region.getZ());

        vec = new ChunkVec(-33, -33, 33, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(31, region.getX());
        assertEquals(31, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(-33, 33, -33, ChunkVecSpace.WORLD);
        region = vec.regionSpace();
        assertEquals(31, region.getX());
        assertEquals(1, region.getY());
        assertEquals(31, region.getZ());
    }

    @Test
    void blockInWorldSpace() {
        ChunkVec vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        BlockVec block = vec.blockInWorldSpace(0, 0, 0);
        assertEquals(0, block.getX());
        assertEquals(0, block.getY());
        assertEquals(0, block.getZ());
        
        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(0, 1, 0);
        assertEquals(0, block.getX());
        assertEquals(1, block.getY());
        assertEquals(0, block.getZ());
        
        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(0, 0, 1);
        assertEquals(0, block.getX());
        assertEquals(0, block.getY());
        assertEquals(1, block.getZ());
        
        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(1, 0, 0);
        assertEquals(1, block.getX());
        assertEquals(0, block.getY());
        assertEquals(0, block.getZ());
        
        vec = new ChunkVec(1, 0, 0, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(0, 0, 0);
        assertEquals(16, block.getX());
        assertEquals(0, block.getY());
        assertEquals(0, block.getZ());

        vec = new ChunkVec(0, 0, 1, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(0, 0, 0);
        assertEquals(0, block.getX());
        assertEquals(0, block.getY());
        assertEquals(16, block.getZ());
        
        vec = new ChunkVec(1, 0, 1, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(0, 16, 0);
        assertEquals(16, block.getX());
        assertEquals(16, block.getY());
        assertEquals(16, block.getZ());
        
        vec = new ChunkVec(1, 0, 1, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(1, 17, 1);
        assertEquals(17, block.getX());
        assertEquals(17, block.getY());
        assertEquals(17, block.getZ());
        
        // Negative
        
        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(-1, 0, 0);
        assertEquals(-1, block.getX());
        assertEquals(0, block.getY());
        assertEquals(0, block.getZ());
        
        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(0, -1, 0);
        assertEquals(0, block.getX());
        assertEquals(-1, block.getY());
        assertEquals(0, block.getZ());
        
        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(0, 0, -1);
        assertEquals(0, block.getX());
        assertEquals(0, block.getY());
        assertEquals(-1, block.getZ());
        
        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(-1, -1, -1);
        assertEquals(-1, block.getX());
        assertEquals(-1, block.getY());
        assertEquals(-1, block.getZ());
        
        vec = new ChunkVec(-1, 0, -1, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(0, -16, 0);
        assertEquals(-16, block.getX());
        assertEquals(-16, block.getY());
        assertEquals(-16, block.getZ());
        
        vec = new ChunkVec(-1, 0, -1, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(-1, -17, -1);
        assertEquals(-17, block.getX());
        assertEquals(-17, block.getY());
        assertEquals(-17, block.getZ());
        
        vec = new ChunkVec(-1, 0, -1, ChunkVecSpace.WORLD);
        block = vec.blockInWorldSpace(1, -15, 1);
        assertEquals(-15, block.getX());
        assertEquals(-15, block.getY());
        assertEquals(-15, block.getZ());
    }

    @Test
    void worldSpace() {
        ChunkVec vec = new ChunkVec(0, 0, 0, ChunkVecSpace.REGION);
        ChunkVec block = vec.worldSpace(new RegionVec(0, 0, 0));
        assertEquals(0, block.getX());
        assertEquals(0, block.getY());
        assertEquals(0, block.getZ());

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.REGION);
        block = vec.worldSpace(new RegionVec(1, 0, 0));
        assertEquals(32, block.getX());
        assertEquals(0, block.getY());
        assertEquals(0, block.getZ());

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.REGION);
        block = vec.worldSpace(new RegionVec(0, 1, 0));
        assertEquals(0, block.getX());
        assertEquals(0, block.getY());
        assertEquals(0, block.getZ());

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.REGION);
        block = vec.worldSpace(new RegionVec(0, 0, 1));
        assertEquals(0, block.getX());
        assertEquals(0, block.getY());
        assertEquals(32, block.getZ());

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.REGION);
        block = vec.worldSpace(new RegionVec(1, 1, 1));
        assertEquals(32, block.getX());
        assertEquals(0, block.getY());
        assertEquals(32, block.getZ());

        // Negative

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.REGION);
        block = vec.worldSpace(new RegionVec(-1, 0, 0));
        assertEquals(-32, block.getX());
        assertEquals(0, block.getY());
        assertEquals(0, block.getZ());

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.REGION);
        block = vec.worldSpace(new RegionVec(0, -1, 0));
        assertEquals(0, block.getX());
        assertEquals(0, block.getY());
        assertEquals(0, block.getZ());

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.REGION);
        block = vec.worldSpace(new RegionVec(0, 0, -1));
        assertEquals(0, block.getX());
        assertEquals(0, block.getY());
        assertEquals(-32, block.getZ());

        vec = new ChunkVec(0, 0, 0, ChunkVecSpace.REGION);
        block = vec.worldSpace(new RegionVec(-1, -1, -1));
        assertEquals(-32, block.getX());
        assertEquals(0, block.getY());
        assertEquals(-32, block.getZ());
    }

    @Test
    void section() {
    }

    @Test
    void region() {
        ChunkVec vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        RegionVec region = vec.region();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(33, 0, 0, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(1, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(0, 33, 0, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(0, 0, 33, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(1, region.getZ());

        vec = new ChunkVec(33, 33, 33, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(1, region.getX());
        assertEquals(0, region.getY());
        assertEquals(1, region.getZ());



        vec = new ChunkVec(31, 0, 0, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(0, 31, 0, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(0, 0, 31, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(31, 31, 31, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        // Negative

        vec = new ChunkVec(-1, 0, 0, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(-1, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(0, -1, 0, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(0, region.getZ());

        vec = new ChunkVec(0, 0, -1, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(0, region.getX());
        assertEquals(0, region.getY());
        assertEquals(-1, region.getZ());

        vec = new ChunkVec(-33, -1, -33, ChunkVecSpace.WORLD);
        region = vec.region();
        assertEquals(-2, region.getX());
        assertEquals(0, region.getY());
        assertEquals(-2, region.getZ());
    }

    @Test
    void blockAt() {
        var ref = new Object() {
            ChunkVec vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        };
        BlockVec block = ref.vec.blockAt(0, 0, 0);
        assertEquals(0, block.getX());
        assertEquals(0, block.getY());
        assertEquals(0, block.getZ());

        ref.vec = new ChunkVec(0, 0, 0, ChunkVecSpace.WORLD);
        block = ref.vec.blockAt(1, 2, 3);
        assertEquals(1, block.getX());
        assertEquals(2, block.getY());
        assertEquals(3, block.getZ());
    }
}