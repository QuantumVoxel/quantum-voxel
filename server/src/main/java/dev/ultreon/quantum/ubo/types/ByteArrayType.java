package dev.ultreon.quantum.ubo.types;

import dev.ultreon.quantum.ubo.DataTypes;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

public class ByteArrayType implements ArrayType<byte[], Byte> {
    private byte[] obj;

    public ByteArrayType(byte[] obj) {
        this.obj = obj;
    }

    public ByteArrayType(ByteBuffer buffer) {
        this.obj = new byte[buffer.remaining()];
        buffer.get(obj);
    }

    public ByteArrayType(String str) {
        this.obj = str.getBytes(StandardCharsets.UTF_8);
    }

    public ByteArrayType(String str, Charset charset) {
        this.obj = str.getBytes(charset);
    }

    public ByteArrayType(int len) {
        this.obj = new byte[len];
    }

    public ByteArrayType(byte[] obj, int len) {
        this.obj = Arrays.copyOf(obj, len);
    }

    public ByteArrayType(Byte[] array) {
        this.obj = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            this.obj[i] = array[i];
        }
    }

    @Override
    public byte[] getValue() {
        return obj;
    }

    @Override
    public void setValue(byte[] obj) {
        if (obj == null) throw new IllegalArgumentException("Value can't be set to null");
        this.obj = obj;
    }

    @Override
    public int id() {
        return DataTypes.BYTE_ARRAY;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeInt(obj.length);
        for (byte b : obj) {
            output.writeByte(b);
        }
    }

    public static ByteArrayType read(DataInput input) throws IOException {
        int len = input.readInt();
        byte[] arr = new byte[len];
        for (int i = 0; i < len; i++) {
            arr[i] = input.readByte();
        }
        return new ByteArrayType(arr);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ByteArrayType)) return false;
        ByteArrayType that = (ByteArrayType) other;
        return Arrays.equals(obj, that.obj);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(obj);
    }

    @Override
    public ByteArrayType copy() {
        return new ByteArrayType(obj.clone());
    }

    @Override
    public String writeUso() {
        StringBuilder builder = new StringBuilder("(b;");
        for (byte v : obj) {
            builder.append(v).append(",");
        }

        return builder.substring(0, builder.length() - 1) + ")";
    }

    @Override
    public int size() {
        return obj.length;
    }

    @Override
    public @NotNull Byte get(int index) {
        return obj[index];
    }

    @Override
    public void set(int index, Byte value) {
        obj[index] = value;
    }

    public byte getByte(int index) {
        return obj[index];
    }

    public void set(int index, byte value) {
        obj[index] = value;
    }

    @Override
    public String toString() {
        return writeUso();
    }

    @Override
    public @NotNull ByteIterator iterator() {
        return new ByteIterator(obj);
    }

    public static class ByteIterator implements Iterator<@NotNull Byte> {
        private final byte[] obj;
        private int index;

        public ByteIterator(byte[] obj) {
            this.obj = obj;
        }

        @Override
        public boolean hasNext() {
            return index < obj.length;
        }

        @Override
        @Deprecated
        public Byte next() {
            return obj[index++];
        }

        public byte nextByte() {
            return obj[index++];
        }
    }
}
