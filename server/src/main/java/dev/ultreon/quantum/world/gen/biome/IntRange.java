package dev.ultreon.quantum.world.gen.biome;

public class IntRange {
    private final int min;
    private final int max;

    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public boolean contains(int y) {
        return y >= this.min && y <= this.max;
    }

    public int getStart() {
        return this.min;
    }

    public int getEndInclusive() {
        return this.max;
    }
}
