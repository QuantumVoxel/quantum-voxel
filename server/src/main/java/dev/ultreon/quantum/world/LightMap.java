package dev.ultreon.quantum.world;

import dev.ultreon.quantum.network.PacketIO;

import java.util.Arrays;

import static dev.ultreon.quantum.world.World.CS;

public class LightMap implements Cloneable {
    private byte[] data;

    public LightMap(int size) {
        this.data = new byte[size];
    }

    public LightMap(byte[] data) {
        this.data = data;
    }

    public LightMap(int byteLength, PacketIO buffer) {
        this.data = new byte[byteLength];
        buffer.readBytes(this.data);
    }

    private int index(int x, int y, int z) {
        return (z * CS + y) * CS + x;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getSkyLight(int x, int y, int z) {
        byte datum = this.data[this.index(x, y, z)];
        return (datum & 0xF0) >> 4;
    }

    public int getBlockLight(int x, int y, int z) {
        byte datum = this.data[this.index(x, y, z)];
        return datum & 0x0F;
    }

    public void setSkyLight(int x, int y, int z, int value) {
        byte datum = this.data[this.index(x, y, z)];
        datum = (byte) (datum & 0x0F | (value & 0x0F) << 4);
        this.data[this.index(x, y, z)] = datum;
    }

    public void setBlockLight(int x, int y, int z, int value) {
        byte datum = this.data[this.index(x, y, z)];
        datum = (byte) (datum & 0xF0 | value * 0x0F);
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

    public byte getSkyLight(int idx) {
        return (byte) ((this.data[idx] & 0xF0) >> 4);
    }

    public void setBlockLight(int idx, byte value) {
        this.data[idx] = (byte) (this.data[idx] & 0x0F | value << 4);
    }

    public void setSkyLight(int idx, byte value) {
        this.data[idx] = (byte) (this.data[idx] & 0xF0 | value & 0x0F);
    }

    public byte get(int index) {
        return this.data[index];
    }

    @Override
    public LightMap clone() {
        try {
            return (LightMap) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getLight(int x, int y, int z) {
        return data[index(x, y, z)] & 0xFF;
    }
}
