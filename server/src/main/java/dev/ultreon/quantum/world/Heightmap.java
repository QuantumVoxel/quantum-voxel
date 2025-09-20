package dev.ultreon.quantum.world;

public class Heightmap {
    private int[] map;
    private final int width;
    private boolean initialized;

    public Heightmap(int width) {
        this.map = new int[width * width];
        this.width = width;
    }

    public int[] getMap() {
        return this.map;
    }

    public int get(int x, int z) {
        return this.map[z * this.width + x];
    }

    public void set(int x, int z, short value) {
        if (value < 0) value = 0;
        this.map[z * this.width + x] = value;
    }

    public int getWidth() {
        return this.width;
    }

    public void load(int[] data) {
        if (data == null) return;
        this.map = data;
    }

    public int[] save() {
        return this.map;
    }

    public void init() {
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
