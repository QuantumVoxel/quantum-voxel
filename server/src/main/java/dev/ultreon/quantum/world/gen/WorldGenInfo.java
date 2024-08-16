package dev.ultreon.quantum.world.gen;

import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.ArrayList;
import java.util.List;

public final class WorldGenInfo {
    public List<ChunkVec> toLoad = new ArrayList<>();
    public List<ChunkVec> toRemove = new ArrayList<>();
    public List<ChunkVec> toUpdate = new ArrayList<>();

    public WorldGenInfo() {

    }
}
