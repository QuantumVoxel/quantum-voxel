package dev.ultreon.quantum.util;

import org.jetbrains.annotations.NotNull;

public class Range implements IntIterable {
    private final int start;
    private final int end;
    private final int step;

    public Range(int start, int end) {
        this(start, end, 1);
    }

    public Range(int start, int end, int step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    @Override
    public IntIterator intIterator() {
        return this.iterator();
    }

    @Override
    public @NotNull IntIterator iterator() {
        return new RangeIterator(this);
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public int getStep() {
        return this.step;
    }

    public static class RangeIterator implements IntIterator {
        private final Range range;
        private int current;

        public RangeIterator(Range range) {
            this.range = range;
            this.current = range.start;
        }

        @Override
        public boolean hasNext() {
            return this.current <= this.range.end;
        }

        @Override
        public int nextInt() {
            int next = this.current;
            this.current += this.range.step;
            return next;
        }
    }
}
