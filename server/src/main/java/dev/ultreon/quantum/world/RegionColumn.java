package dev.ultreon.quantum.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import dev.ultreon.quantum.util.IVec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class RegionColumn implements Column<ServerWorld.Region> {
    private final IntMap<ServerWorld.Region> regions = new IntMap<>();
    private final ServerWorld world;
    private final IVec2 pos;
    private int firstY;
    private int lastY;

    public RegionColumn(ServerWorld world, IVec2 pos) {
        this.world = world;
        this.pos = pos;
    }

    public @Nullable ServerWorld.Region get(int regionY) {
        synchronized (this) {
            return this.regions.get(regionY, null);
        }
    }

    public void set(int regionY, ServerWorld.Region region) {
        if (region == null) {
            remove(regionY);
            return;
        }
        synchronized (this) {
            if (regionY < this.firstY) {
                this.firstY = regionY;
            }
            if (regionY > this.lastY) {
                this.lastY = regionY;
            }
            this.regions.put(regionY, region);
        }
    }

    public ServerWorld.Region remove(int regionY) {
        synchronized (this) {
            ServerWorld.Region remove = this.regions.remove(regionY);
            if (regionY == this.firstY) {
                this.firstY = findHighestY();
            }
            if (regionY == this.lastY) {
                this.lastY = findLowestY();
            }
            return remove;
        }
    }

    private int findHighestY() {
        int highest = 0;
        for (int y : this.regions.keys().toArray().toArray()) {
            if (y > highest) {
                highest = y;
            }
        }
        return highest;
    }

    private int findLowestY() {
        int lowest = Integer.MAX_VALUE;
        for (int y : this.regions.keys().toArray().toArray()) {
            if (y < lowest) {
                lowest = y;
            }
        }
        return lowest;
    }

    @Override
    public int getFirstY() {
        return firstY;
    }

    @Override
    public int getLastY() {
        return lastY;
    }

    @Override
    public int size() {
        return regions.size;
    }

    public Array<ServerWorld.Region> values() {
        synchronized (this) {
            return this.regions.values().toArray();
        }
    }

    @Override
    public @NotNull Iterator<ServerWorld.Region> iterator() {
        return this.regions.values().iterator();
    }

    public IVec2 getPos() {
        return pos;
    }

    public ServerWorld getWorld() {
        return world;
    }
}
