package dev.ultreon.quantum.world.vec;

import org.junit.jupiter.api.Test;

import static dev.ultreon.quantum.world.World.CS;
import static org.junit.jupiter.api.Assertions.*;

class ChunkVecTest {
    @Test
    public void blockInWorldSpaceTest() {
        ChunkVec vec = new ChunkVec(0, 0, 0);
        assertEquals(new BlockVec(0, 0, 0, BlockVecSpace.WORLD), vec.blockInWorldSpace(0, 0, 0));

        vec = new ChunkVec(1, 1, 1);
        assertEquals(new BlockVec(CS, CS, CS, BlockVecSpace.WORLD), vec.blockInWorldSpace(0, 0, 0));
        assertEquals(new BlockVec(CS+1, CS+1, CS+1, BlockVecSpace.WORLD), vec.blockInWorldSpace(1, 1, 1));

        vec = new ChunkVec(-1, -1, -1);
        assertEquals(new BlockVec(-CS, -CS, -CS, BlockVecSpace.WORLD), vec.blockInWorldSpace(0, 0, 0));
        assertEquals(new BlockVec(-CS-1, -CS-1, -CS-1, BlockVecSpace.WORLD), vec.blockInWorldSpace(-1, -1, -1));
    }

    @Test
    public void startTest() {
        ChunkVec vec = new ChunkVec(0, 0, 0);
        assertEquals(new BlockVec(0, 0, 0, BlockVecSpace.WORLD), vec.start());
        vec = new ChunkVec(1, 1, 1);
        assertEquals(new BlockVec(CS, CS, CS, BlockVecSpace.WORLD), vec.start());
        vec = new ChunkVec(-1, -1, -1);
        assertEquals(new BlockVec(-CS, -CS, -CS, BlockVecSpace.WORLD), vec.start());
    }

    @Test
    public void endTest() {
        ChunkVec vec = new ChunkVec(0, 0, 0);
        assertEquals(new BlockVec(CS-1, CS-1, CS-1, BlockVecSpace.WORLD), vec.end());
        vec = new ChunkVec(1, 1, 1);
        assertEquals(new BlockVec(CS+CS-1, CS+CS-1, CS+CS-1, BlockVecSpace.WORLD), vec.end());
        vec = new ChunkVec(-1, -1, -1);
        assertEquals(new BlockVec(-1, -1, -1, BlockVecSpace.WORLD), vec.end());
    }
}