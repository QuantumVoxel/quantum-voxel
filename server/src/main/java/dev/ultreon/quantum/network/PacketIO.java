package dev.ultreon.quantum.network;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.libs.commons.v0.util.EnumUtils;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.debug.timing.Timing;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.registry.IdRegistry;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryHandle;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.ubo.DataTypeRegistry;
import dev.ultreon.quantum.ubo.types.DataType;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.ChunkVecSpace;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The PacketIO class provides methods for reading and writing various data types to and from input and output streams.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class PacketIO implements RegistryHandle {
    private static final int MAX_UBO_SIZE = 1024 * 1024 * 2;
    private final DataInputStream input;
    private final DataOutputStream output;
    private final InputStream inputOrig;
    private final OutputStream outputOrig;
    private final RegistryHandle handle;

    /**
     * Initializes a new PacketIO instance with the specified input and output streams.
     * If the provided input or output stream is null, it defaults to a NullInputStream
     * or NullOutputStream respectively.
     *
     * @param in the input stream from which packets will be read. If null, defaults to a NullInputStream.
     * @param out the output stream to which packets will be written. If null, defaults to a NullOutputStream.
     */
    @SuppressWarnings("resource")
    public PacketIO(InputStream in, OutputStream out, RegistryHandle handle) {
        if (in == null) in = new NullInputStream();
        if (out == null) out = new NullOutputStream();
        this.input = new DataInputStream(in);
        this.output = new DataOutputStream(out);
        this.inputOrig = in;
        this.outputOrig = out;
        this.handle = handle;
    }

    /**
     * Initializes a new PacketIO instance using the specified socket.
     * The socket's input and output streams are used for reading and writing packets.
     *
     * @param socket the socket to be used for input and output streams.
     * @throws IOException if an I/O error occurs when creating the input or output streams.
     */
    public PacketIO(Socket socket, RegistryHandle handle) throws IOException {
        this(socket.getInputStream(), socket.getOutputStream(), handle);
    }

    public String readString(int max) {
        if (max < 0) throw new IllegalArgumentException(CommonConstants.EX_INVALID_DATA);
        int len = this.readShort();
        if (len > max) throw new PacketOverflowException("string", len, max);
        byte[] bytes = new byte[len];
        this.readBytes0(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String readString() {
        int len = this.readShort();
        byte[] bytes = new byte[len];
        this.readBytes0(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public PacketIO writeString(String string, int max) {
        if (max < 0) throw new IllegalArgumentException(CommonConstants.EX_INVALID_DATA);
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > max) throw new PacketOverflowException("string", bytes.length, max);
        this.writeShort(bytes.length);
        this.writeBytes0(bytes);
        return this;
    }

    public void writeString(String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        this.writeShort(bytes.length);
        this.writeBytes0(bytes);
    }

    public byte[] readByteArray(int max) {
        if (max < 0) throw new IllegalArgumentException(CommonConstants.EX_INVALID_DATA);
        int len;
        try {
            len = this.input.readInt();
        } catch (IOException e) {
            throw new PacketException(e);
        }
        if (len > max) throw new PacketOverflowException("byte array", len, max);
        byte[] bytes = new byte[len];
        this.readBytes0(bytes);
        return bytes;
    }

    public void writeByteArray(byte[] array, int max) {
        if (array.length > max) throw new DataOverflowException("byte array", array.length, max);
        try {
            this.output.writeInt(array.length);
        } catch (IOException e) {
            throw new PacketException(e);
        }
        this.writeBytes0(array);
    }

    public NamespaceID readId() {
        var location = this.readString(100);
        var path = this.readString(200);
        return new NamespaceID(location, path);
    }

    public void writeId(NamespaceID id) {
        Timing.start("write_id");
        this.writeString(id.getDomain(), 100);
        this.writeString(id.getPath(), 200);
        Timing.end("write_id");
    }

    public byte readByte() {
        try {
            return this.input.readByte();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public short readUnsignedByte() {
        try {
            return (short) this.input.readUnsignedByte();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeByte(int value) {
        try {
            this.output.writeByte(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeByte(byte value) {
        try {
            this.output.writeByte(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public short readShort() {
        try {
            return this.input.readShort();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public int readUnsignedShort() {
        try {
            return this.input.readUnsignedShort();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeShort(int value) {
        try {
            this.output.writeShort(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeShort(short value) {
        try {
            this.output.writeShort(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public int readInt() {
        try {
            return this.input.readInt();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeInt(int value) {
        try {
            this.output.writeInt(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public long readLong() {
        try {
            return this.input.readLong();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeLong(long value) {
        try {
            this.output.writeLong(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public float readFloat() {
        try {
            return this.input.readFloat();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeFloat(float value) {
        try {
            this.output.writeFloat(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public double readDouble() {
        try {
            return this.input.readDouble();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeDouble(double value) {
        try {
            this.output.writeDouble(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public char readChar() {
        try {
            return this.input.readChar();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeChar(char value) {
        try {
            this.output.writeChar(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public boolean readBoolean() {
        try {
            return this.input.readBoolean();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeBoolean(boolean value) {
        try {
            this.output.writeBoolean(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public UUID readUuid() {
        long mostSigBits = this.readLong();
        long leastSigBits = this.readLong();

        return GamePlatform.get().constructUuid(mostSigBits, leastSigBits);
    }

    public void writeUuid(UUID value) {
        long[] elements = GamePlatform.get().getUuidElements(value);
        try {
            this.output.writeLong(elements[0]);
            this.output.writeLong(elements[1]);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public BitSet readBitSet() {
        int size = this.readVarInt();
        byte[] bytes = new byte[size];
        this.readBytes0(bytes);
        return BitSet.valueOf(bytes);
    }

    public PacketIO writeBitSet(BitSet value) {
        byte[] bytes = value.toByteArray();
        this.writeVarInt(bytes.length);
        this.writeBytes0(bytes);
        return this;
    }

    public Vec2f readVec2f() {
        float x = this.readFloat();
        float y = this.readFloat();

        return new Vec2f(x, y);
    }

    public void writeVec2f(Vec2f vec) {
        try {
            this.output.writeFloat(vec.x);
            this.output.writeFloat(vec.y);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public Vec3f readVec3f() {
        float x = this.readFloat();
        float y = this.readFloat();
        float z = this.readFloat();

        return new Vec3f(x, y, z);
    }

    public void writeVec3f(Vec3f vec) {
        try {
            this.output.writeFloat(vec.x);
            this.output.writeFloat(vec.y);
            this.output.writeFloat(vec.z);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public Vec4f readVec4f() {
        float x = this.readFloat();
        float y = this.readFloat();
        float z = this.readFloat();
        float w = this.readFloat();

        return new Vec4f(x, y, z, w);
    }

    public void writeVec4f(Vec4f vec) {
        try {
            this.output.writeFloat(vec.x);
            this.output.writeFloat(vec.y);
            this.output.writeFloat(vec.z);
            this.output.writeFloat(vec.w);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public Vec2d readVec2d() {
        double x = this.readDouble();
        double y = this.readDouble();

        return new Vec2d(x, y);
    }

    public void writeVec2f(Vec2d vec) {
        try {
            this.output.writeDouble(vec.x);
            this.output.writeDouble(vec.y);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public Vec3d readVec3d() {
        double x = this.readDouble();
        double y = this.readDouble();
        double z = this.readDouble();

        return new Vec3d(x, y, z);
    }

    public void writeVec3d(Vec3d vec) {
        try {
            this.output.writeDouble(vec.x);
            this.output.writeDouble(vec.y);
            this.output.writeDouble(vec.z);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public Vec4d readVec4d() {
        double x = this.readDouble();
        double y = this.readDouble();
        double z = this.readDouble();
        double w = this.readDouble();

        return new Vec4d(x, y, z, w);
    }

    public void writeVec4d(Vec4d vec) {
        try {
            this.output.writeDouble(vec.x);
            this.output.writeDouble(vec.y);
            this.output.writeDouble(vec.z);
            this.output.writeDouble(vec.w);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public Vec2i readVec2i() {
        int x = this.readInt();
        int y = this.readInt();

        return new Vec2i(x, y);
    }

    public void writeVec2i(Vec2i vec) {
        try {
            this.output.writeInt(vec.x);
            this.output.writeInt(vec.y);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public Vec3i readVec3i() {
        int x = this.readInt();
        int y = this.readInt();
        int z = this.readInt();

        return new Vec3i(x, y, z);
    }

    public void writeVec3i(Vec3i vec) {
        try {
            this.output.writeInt(vec.x);
            this.output.writeInt(vec.y);
            this.output.writeInt(vec.z);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public Vec4i readVec4i() {
        int x = this.readInt();
        int y = this.readInt();
        int z = this.readInt();
        int w = this.readInt();

        return new Vec4i(x, y, z, w);
    }

    public void writeVec4i(Vec4i vec) {
        try {
            this.output.writeInt(vec.x);
            this.output.writeInt(vec.y);
            this.output.writeInt(vec.z);
            this.output.writeInt(vec.w);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public BlockVec readBlockVec() {
        int x = this.readInt();
        int y = this.readInt();
        int z = this.readInt();

        return new BlockVec(x, y, z, this.readEnum(BlockVecSpace.WORLD));
    }

    public PacketIO writeBlockVec(BlockVec pos) {
        this.writeInt(pos.getIntX());
        this.writeInt(pos.getIntY());
        this.writeInt(pos.getIntZ());
        this.writeEnum(pos.getSpace());
        return this;
    }

    public ChunkVec readChunkVec() {
        int x = this.readInt();
        int y = this.readInt();
        int z = this.readInt();
        ChunkVecSpace space = this.readEnum(ChunkVecSpace.WORLD);

        return new ChunkVec(x, y, z, space);
    }

    public PacketIO writeChunkVec(ChunkVec pos) {
        try {
            this.output.writeInt(pos.getIntX());
            this.output.writeInt(pos.getIntY());
            this.output.writeInt(pos.getIntZ());

            this.writeEnum(pos.getSpace());
        } catch (IOException e) {
            throw new PacketException(e);
        }
        return this;
    }

    public int readVarInt() {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = readByte();
            int value = (read & 0x7F);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((read & 0x80) != 0);

        return result;
    }

    public void writeVarInt(int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        writeByte(value & 0x7F);
    }

    public void writeUbo(DataType<?> ubo) {
        this.writeByte(ubo.id());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (var output = new DataOutputStream(bos)) {
            ubo.write(output);
            bos.flush();
        } catch (IOException ignored) {
            try {
                bos.close();
            } catch (IOException e) {
                throw new PacketException(e);
            }
        }

        this.writeByteArray(bos.toByteArray(), PacketIO.MAX_UBO_SIZE);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T extends DataType<?>> T readUbo(T... typeGetter) {
        T data;
        int id = this.readUnsignedByte();
        byte[] bytes = this.readByteArray(PacketIO.MAX_UBO_SIZE);

        try(DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes))) {
            Class<?> componentType = typeGetter.getClass().getComponentType();
            if (id != DataTypeRegistry.getId(componentType)) throw new PacketException("Id doesn't match requested type.");
            data = (T) DataTypeRegistry.read(DataTypeRegistry.getId(componentType), stream);
        } catch (IOException e) {
            throw new PacketException(e);
        }
        return data;
    }

    public int capacity() {
        return Integer.MAX_VALUE;
    }

    public PacketIO capacity(int newCapacity) {
        return this;
    }

    @Deprecated
    public int maxCapacity() {
        return Integer.MAX_VALUE;
    }

    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }

    @Deprecated
    public PacketIO order(ByteOrder order) {
        return this;
    }

    public PacketIO unwrap() {
        return this;
    }

    public boolean isDirect() {
        return true;
    }

    public boolean isReadOnly() {
        return isReadable() && !isWritable();
    }

    public boolean isReadable() {
        return !(this.inputOrig instanceof NullInputStream);
    }

    public boolean isReadable(int size) {
        if (!(this.inputOrig instanceof NullInputStream)) {
            try {
                return this.input.available() >= size;
            } catch (IOException e) {
                throw new PacketException(e);
            }
        }
        return false;
    }

    public boolean isWritable() {
        return !(this.outputOrig instanceof NullOutputStream);
    }

    public boolean isWritable(int size) {
        return !(this.outputOrig instanceof NullOutputStream);
    }

    public PacketIO clear() {
        throw new UnsupportedOperationException();
    }

    public int readMedium() {
        byte bits16;
        byte bits8;
        byte bits;
        try {
            bits16 = this.input.readByte();
            bits8 = this.input.readByte();
            bits = this.input.readByte();
        } catch (IOException e) {
            throw new PacketException(e);
        }

        return (bits16 & 0xFF) << 16 | (bits8 & 0xFF) << 8 | (bits & 0xFF);
    }

    public PacketIO readBytes(int length) {
        byte[] bytes = readNBytes(length);
        return new PacketIO(new ByteArrayInputStream(bytes), null, handle);
    }

    private byte @NotNull [] readNBytes(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = readByte();
        }
        return bytes;
    }

    public PacketIO readBytes(byte[] dst) {
        this.readBytes0(dst);
        return this;
    }

    public PacketIO readBytes(byte[] dst, int dstIndex, int length) {
        try {
            if (this.input.read(dst, dstIndex, length) != length) throw new EOFException();
        } catch (IOException e) {
            throw new PacketException(e);
        }
        return this;
    }

    public PacketIO readBytes(OutputStream out, int length) {
        try {
            byte[] bytes = readNBytes(length);
            out.write(bytes);
        } catch (IOException e) {
            throw new PacketException(e);
        }
        return this;
    }

    public CharSequence readCharSequence(int length, Charset charset) {
        byte[] bytes = this.readByteArray(length * MathUtils.ceil(charset.newEncoder().maxBytesPerChar()));

        return new String(bytes, charset);
    }

    public int readBytes(FileChannel out, long position, int length) {
        try {
            return out.write(ByteBuffer.wrap(readNBytes(length)), position);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeMedium(int value) {
        try {
            byte bits16 = (byte) ((value >> 16) & 0xFF);
            byte bits8 = (byte) ((value >> 8) & 0xFF);
            byte bits = (byte) (value & 0xFF);

            this.output.writeByte(bits16);
            this.output.writeByte(bits8);
            this.output.writeByte(bits);

            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeChar(int value) {
        try {
            this.output.writeChar(value);
            return this;
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public PacketIO writeBytes(byte[] src) {
        this.writeBytes0(src);
        return this;
    }

    public PacketIO writeBytes(byte[] src, int srcIndex, int length) {
        this.writeBytes0(src, srcIndex, length);
        return this;
    }

    private void writeBytes0(byte[] src, int srcIndex, int length) {
        try {
            this.output.write(src, srcIndex, length);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public void writeBytes(InputStream in, int length) {
        try {
            byte[] bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                int read = in.read();
                if (read < 0) throw new EncoderException("EOF reached");
                bytes[i] = (byte) read;
            }
            this.output.write(bytes);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public void writeBytes(ScatteringByteChannel in, int length) {
        try {
            ByteBuffer allocate = ByteBuffer.allocate(length);
            in.read(allocate);
            this.output.write(allocate.array(), 0, length);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public int writeBytes(FileChannel in, long position, int length) {
        try {
            return in.read(ByteBuffer.wrap(readNBytes(length)), position);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    @Deprecated
    public boolean hasArray() {
        return false;
    }

    @Deprecated
    public byte[] array() {
        throw new UnsupportedOperationException();
    }

    public int arrayOffset() {
        return 0;
    }

    public boolean hasMemoryAddress() {
        return false;
    }

    public long memoryAddress() {
        return 0L;
    }

    public String toString(Charset charset) {
        return "PacketBuffer{input=" + this.input + ", output=" + this.output + "}";
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        return this.equals(obj);
    }

    public String toString() {
        return this.toString(StandardCharsets.UTF_8);
    }

    public PacketIO asPacketBuffer() {
        return this;
    }

    public short[] readShortArray() {
        int len = this.readVarInt();
        short[] array = new short[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readShort();
        }

        return array;
    }

    public short[] readShortArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(String.format(CommonConstants.EX_ARRAY_TOO_LARGE, max, len));
        }

        short[] array = new short[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readShort();
        }

        return array;
    }

    public PacketIO writeShortArray(short[] array) {
        this.writeVarInt(array.length);
        for (short s : array) {
            this.writeShort(s);
        }

        return this;
    }

    public int[] readMediumArray() {
        int len = this.readVarInt();
        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readMedium();
        }

        return array;
    }

    public int[] readMediumArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(String.format(CommonConstants.EX_ARRAY_TOO_LARGE, max, len));
        }

        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readMedium();
        }

        return array;
    }

    public PacketIO writeMediumArray(int[] array) {
        this.writeVarInt(array.length);
        for (int i : array) {
            this.writeMedium(i);
        }

        return this;
    }

    public int[] readIntArray() {
        int len = this.readVarInt();
        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readInt();
        }

        return array;
    }

    public int[] readIntArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(String.format(CommonConstants.EX_ARRAY_TOO_LARGE, max, len));
        }

        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readInt();
        }

        return array;
    }

    public PacketIO writeIntArray(int[] array) {
        this.writeVarInt(array.length);
        for (int i : array) {
            this.writeInt(i);
        }

        return this;
    }

    public long[] readLongArray() {
        int len = this.readVarInt();
        long[] array = new long[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readLong();
        }

        return array;
    }

    public long[] readLongArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(String.format(CommonConstants.EX_ARRAY_TOO_LARGE, max, len));
        }

        long[] array = new long[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readLong();
        }

        return array;
    }

    public PacketIO writeLongArray(long[] array) {
        this.writeVarInt(array.length);
        for (long l : array) {
            this.writeLong(l);
        }

        return this;
    }
    public float[] readFloatArray() {
        int len = this.readVarInt();
        float[] array = new float[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readFloat();
        }

        return array;
    }

    public float[] readFloatArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(String.format(CommonConstants.EX_ARRAY_TOO_LARGE, max, len));
        }

        float[] array = new float[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readFloat();
        }

        return array;
    }

    public PacketIO writeFloatArray(float[] array) {
        this.writeVarInt(array.length);
        for (float f : array) {
            this.writeFloat(f);
        }

        return this;
    }
    public double[] readDoubleArray() {
        int len = this.readVarInt();
        double[] array = new double[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readDouble();
        }

        return array;
    }

    public double[] readDoubleArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(String.format(CommonConstants.EX_ARRAY_TOO_LARGE, max, len));
        }

        double[] array = new double[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readDouble();
        }

        return array;
    }

    public PacketIO writeDoubleArray(double[] array) {
        this.writeVarInt(array.length);
        for (double d : array) {
            this.writeDouble(d);
        }

        return this;
    }

    public <T> List<T> readList(Function<PacketIO, T> decoder) {
        int size = this.readInt();
        var list = new ArrayList<T>();

        for (int i = 0; i < size; i++) {
            list.add(decoder.apply(this));
        }

        return list;
    }

    public <T> List<T> readList(Function<PacketIO, T> decoder, int max) {
        int size = this.readInt();
        if (size > max) {
            throw new PacketException(String.format("List too large, max = %d, actual = %d", max, size));
        }

        var list = new ArrayList<T>();

        for (int i = 0; i < size; i++) {
            list.add(decoder.apply(this));
        }

        return list;
    }

    public <T> PacketIO writeList(List<T> list, BiConsumer<PacketIO, T> encoder) {
        this.writeInt(list.size());
        for (T item : list) {
            encoder.accept(this, item);
        }

        return this;
    }

    public <K, V> Map<K, V> readMap(Function<PacketIO, K> keyDecoder, Function<PacketIO, V> valueDecoder) {
        int size = this.readMedium();
        var map = new HashMap<K, V>();

        for (int i = 0; i < size; i++) {
            map.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }

        return map;
    }

    public <K, V> Map<K, V> readMap(Function<PacketIO, K> keyDecoder, Function<PacketIO, V> valueDecoder, int max) {
        int size = this.readMedium();
        if (size > max) {
            throw new PacketException(String.format("Map too large, max = %d, actual = %d", max, size));
        }

        var map = new HashMap<K, V>();

        for (int i = 0; i < size; i++) {
            map.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }

        return map;
    }

    public <K, V> PacketIO writeMap(Map<K, V> map, BiConsumer<PacketIO, K> keyEncoder, BiConsumer<PacketIO, V> valueEncoder) {
        this.writeMedium(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            keyEncoder.accept(this, entry.getKey());
            valueEncoder.accept(this, entry.getValue());
        }

        return this;
    }

    public <F, S> Pair<F, S> readPair(Function<PacketIO, F> firstDecoder, Function<PacketIO, S> secondDecoder) {
        return new Pair<>(firstDecoder.apply(this), secondDecoder.apply(this));
    }

    public <F, S> PacketIO writePair(Pair<F, S> pair, BiConsumer<PacketIO, F> firstEncoder, BiConsumer<PacketIO, S> secondEncoder) {
        firstEncoder.accept(this, pair.getFirst());
        secondEncoder.accept(this, pair.getSecond());
        return this;
    }

    public ItemStack readItemStack() {
        return ItemStack.load(this.readUbo());
    }

    public PacketIO writeItemStack(ItemStack stack) {
        this.writeUbo(stack.save());
        return this;
    }

    public TextObject readTextObject() {
        return TextObject.deserialize(this.readUbo());
    }

    public void writeTextObject(TextObject message) {
        this.writeUbo(message.serialize());
    }

    public <T extends Enum<T>> T readEnum(T fallback) {
        return EnumUtils.byOrdinal(this.readByte(), fallback);
    }

    public <T extends Enum<T>> T readEnum(Class<T> type) {
        T[] enumConstants = type.getEnumConstants();
        byte b = this.readByte();
        if (b >= enumConstants.length) {
            throw new PacketException("Invalid enum ordinal: " + b + " for " + type);
        }
        return enumConstants[b];
    }

    public void writeEnum(Enum<?> value) {
        this.writeByte(value.ordinal());
    }

    public BlockState readBlockState() {
        return BlockState.read(this);
    }

    public void writeBlockState(BlockState blockMeta) {
        blockMeta.write(this);
    }

    private void readBytes0(byte[] bytes) {
        if (!isReadable()) throw new WriteOnlyBufferException();

        try {
            int read = this.input.read(bytes);
            if (read < bytes.length) throw new PacketException("End of stream reached");
            if (read != bytes.length) throw new PacketException("Expected " + bytes.length + " bytes but got " + read);
        } catch (IOException e) {
            throw new PacketException(e.getMessage(), e);
        }
    }

    private void writeBytes0(byte[] bytes) {
        if (!isWritable()) throw new ReadOnlyBufferException();

        try {
            this.output.write(bytes);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public int readableBytes() {
        try {
            return this.inputOrig.available();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void flush() {
        if (!isWritable()) throw new ReadOnlyBufferException();

        try {
            this.output.flush();
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public <K, V> ObjectMap<K, V> readObjectMap(Function<PacketIO, K> keyDecoder, Function<PacketIO, V> valueDecoder) {
        int size = this.readMedium();
        var map = new ObjectMap<K, V>(size);

        for (int i = 0; i < size; i++) {
            map.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }

        return map;
    }

    public <K, V> void writeObjectMap(ObjectMap<K, V> map, BiConsumer<PacketIO, K> keyEncoder, BiConsumer<PacketIO, V> valueEncoder) {
        this.writeMedium(map.size);
        for (ObjectMap.Entry<K, V> entry : map.entries()) {
            keyEncoder.accept(this, entry.key);
            valueEncoder.accept(this, entry.value);
        }
    }

    @Override
    public <T> IdRegistry<T> get(RegistryKey<? extends Registry<T>> key) {
        return handle.get(key);
    }

    private static class NullInputStream extends InputStream {
        @Override
        public int read() {
            return 0;
        }
    }
    
    private static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) {
            
        }
    }
}
