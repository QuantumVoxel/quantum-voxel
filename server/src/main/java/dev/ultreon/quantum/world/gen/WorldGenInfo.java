package dev.ultreon.quantum.world.gen;

import dev.ultreon.quantum.world.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public final class WorldGenInfo {
    public List<ChunkPos> toLoad = new ArrayList<>();
    public List<ChunkPos> toRemove = new ArrayList<>();
    public List<ChunkPos> toUpdate = new ArrayList<>();

    public WorldGenInfo() {

    }
}
