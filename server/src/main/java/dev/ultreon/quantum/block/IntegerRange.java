package dev.ultreon.quantum.block;

public class IntegerRange {

    private final int min;
    private final int max;

    public IntegerRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static IntegerRange of(int min, int max) {
        return new IntegerRange(min, max);
    }

    public int getMinimum() {
        return min;
    }

    public int getMaximum() {
        return max;
    }
}
