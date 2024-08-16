package dev.ultreon.quantum.world;

import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChunkRefresher {
    protected final List<ChunkVec> toLoad = new ArrayList<>();
    protected final List<ChunkVec> toUnload = new ArrayList<>();
    private boolean frozen = false;

    public ChunkRefresher() {

    }

    public void addLoading(Collection<ChunkVec> toLoad) {
        if (this.frozen) return;
        for (ChunkVec pos : toLoad) {
            if (this.toLoad.contains(pos)) continue;

            this.toLoad.add(pos);
            this.toUnload.remove(pos);
        }
    }

    public void addUnloading(Collection<ChunkVec> toLoad) {
        if (this.frozen) return;
        for (ChunkVec pos : toLoad) {
            if (this.toLoad.contains(pos)) continue;

            this.toUnload.add(pos);
        }
    }

    public void freeze() {
        this.frozen = true;
    }

    public boolean isFrozen() {
        return this.frozen;
    }
}
