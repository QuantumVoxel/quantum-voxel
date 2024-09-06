package dev.ultreon.quantum.world;

import java.util.Objects;

public final class LightSource {
    private final int x;
    private final int y;
    private final int z;
    private final int level;

    public LightSource(
            int x, int y, int z,
            int level
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = level;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public int level() {
        return level;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LightSource) obj;
        return this.x == that.x &&
                this.y == that.y &&
                this.z == that.z &&
                this.level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, level);
    }

    @Override
    public String toString() {
        return "LightSource[" +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "z=" + z + ", " +
                "level=" + level + ']';
    }

}
