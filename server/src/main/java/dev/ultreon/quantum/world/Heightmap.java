package dev.ultreon.quantum.world;

import java.util.BitSet;

public class Heightmap {
    private short[] map;
    private final int width;
    private boolean initialized;

    public Heightmap(int width) {
        this.map = new short[width * width];

        this.width = width;
    }

    public short[] getMap() {
        return this.map;
    }

    public short get(int x, int z) {
        return this.map[z * this.width + x];
    }

    public void set(int x, int z, short value) {
        if (value < 0) value = 0;
        this.map[z * this.width + x] = value;
    }

    public int getWidth() {
        return this.width;
    }

    public void load(short[] data) {
        if (data == null) return;
        this.map = data;
    }

    public short[] save() {
        return this.map;
    }

    public void init() {
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
