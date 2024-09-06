package dev.ultreon.quantum.world;

import java.util.Arrays;
import java.util.Stack;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public class LightMap {
    private byte[] data;

    private final int width = CHUNK_SIZE;
    private final int height = CHUNK_SIZE;
    private final int depth = CHUNK_SIZE;
    private final Stack<Integer> stack = new Stack<>();

    public LightMap(int size) {
        this.data = new byte[size];
    }

    public LightMap(byte[] data) {
        this.data = data;
    }

    private int index(int x, int y, int z) {
        return (z * height + y) * width + x;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getSunlight(int x, int y, int z) {
        byte datum = this.data[this.index(x, y, z)];
        return (datum & 0xF0) >> 4;
    }

    public int getBlockLight(int x, int y, int z) {
        byte datum = this.data[this.index(x, y, z)];
        return datum & 0x0F;
    }

    public void setSunlight(int x, int y, int z, int value) {
        byte datum = this.data[this.index(x, y, z)];
        datum = (byte) ((datum & 0x0F) | ((value << 4) & 0xF0));
        this.data[this.index(x, y, z)] = datum;
    }

    public void setBlockLight(int x, int y, int z, int value) {
        byte datum = this.data[this.index(x, y, z)];
        datum = (byte) ((datum & 0xF0) | value);
        this.data[this.index(x, y, z)] = datum;
    }

    public byte[] save() {
        return data;
    }

    public void load(byte[] data) {
        if (data == null) return;
        this.data = data;
    }

    public void clear() {
        Arrays.fill(data, (byte) 0);
    }

    public byte getBlockLight(int idx) {
        return (byte) (this.data[idx] & 0x0F);
    }

    public byte getSunlight(int idx) {
        return (byte) ((this.data[idx] & 0xF0) >> 4);
    }

    public void setBlockLight(int idx, byte value) {
        this.data[idx] = (byte) ((this.data[idx] & 0x0F) | (value << 4));
    }

    public void setSunlight(int idx, byte value) {
        this.data[idx] = (byte) ((this.data[idx] & 0xF0) | (value & 0x0F));
    }
}
