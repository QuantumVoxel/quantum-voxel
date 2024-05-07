package dev.ultreon.quantum.network;

import com.badlogic.gdx.math.MathUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.network.partial.PartialPacket;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.ChunkPos;
import com.ultreon.data.TypeRegistry;
import com.ultreon.data.types.IType;
import com.ultreon.libs.commons.v0.tuple.Pair;
import com.ultreon.libs.commons.v0.util.EnumUtils;
import com.ultreon.libs.commons.v0.vector.*;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.ApiStatus;

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

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class PacketIO {
    private static final int MAX_UBO_SIZE = 1024 * 1024 * 2;
    private final DataInputStream input;
    private final DataOutputStream output;
    private final InputStream inputOrig;
    private final OutputStream outputOrig;

    @SuppressWarnings("resource")
    public PacketIO(InputStream in, OutputStream out) {
        if (in == null) in = new NullInputStream();
        if (out == null) out = new NullOutputStream();
        this.input = new DataInputStream(in);
        this.output = new DataOutputStream(out);
        this.inputOrig = in;
        this.outputOrig = out;
    }

    /**
     * Creates a new packet buffer from a list of partial packets.
     *
     * @param parts the partial packets
     * @throws PacketIntegrityException if the packet integrity check fails
     */
    public PacketIO(InputStream in, OutputStream out, List<PartialPacket> parts) throws PacketIntegrityException {
        this(in, out);
    }

    public PacketIO(Socket socket) throws IOException {
        this(socket.getInputStream(), socket.getOutputStream());
    }

    /**
     * Checks the integrity of the partial packets.
     *
     * @param parts the partial packets
     * @return the list of netty byte buffers
     * @throws PacketIntegrityException if the packet integrity check fails
     */
    @ApiStatus.Internal
    public final List<ByteBuf> validate(List<PartialPacket> parts) throws PacketIntegrityException {
        List<ByteBuf> bufs = new ArrayList<>();
        int dataOffsetCheck = 0;
        for (PartialPacket partialPacket : parts.stream().sorted(Comparator.comparing(PartialPacket::dataOffset)).toList()) {
            if (dataOffsetCheck != partialPacket.dataOffset()) throw new PacketIntegrityException("Packet data offset mismatch. Expected " + dataOffsetCheck + " but got " + partialPacket.dataOffset());
            bufs.add(partialPacket.data());
            dataOffsetCheck += partialPacket.data().readableBytes();
        }
        return bufs;
    }

    public String readString(int max) {
        if (max < 0) throw new IllegalArgumentException(CommonConstants.EX_INVALID_DATA);
        int len = this.readVarInt();
        if (len > max) throw new PacketOverflowException("string", len, max);
        byte[] bytes = new byte[len];
        this.readBytes0(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @CanIgnoreReturnValue
    public PacketIO writeUTF(String string, int max) {
        if (max < 0) throw new IllegalArgumentException(CommonConstants.EX_INVALID_DATA);
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > max) throw new PacketOverflowException("string", bytes.length, max);
        this.writeVarInt(bytes.length);
        this.writeBytes0(bytes);
        return this;
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

    public Identifier readId() {
        var location = this.readString(100);
        var path = this.readString(200);
        return new Identifier(location, path);
    }

    public void writeId(Identifier id) {
        this.writeUTF(id.namespace(), 100);
        this.writeUTF(id.path(), 200);
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

        return new UUID(mostSigBits, leastSigBits);
    }

    public void writeUuid(UUID value) {
        long mostSigBits = value.getMostSignificantBits();
        long leastSigBits = value.getLeastSignificantBits();
        try {
            this.output.writeLong(mostSigBits);
            this.output.writeLong(leastSigBits);
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

    public BlockPos readBlockPos() {
        int x = this.readInt();
        int y = this.readInt();
        int z = this.readInt();

        return new BlockPos(x, y, z);
    }

    @CanIgnoreReturnValue
    public PacketIO writeBlockPos(BlockPos pos) {
        try {
            this.output.writeInt(pos.x());
            this.output.writeInt(pos.y());
            this.output.writeInt(pos.z());
        } catch (IOException e) {
            throw new PacketException(e);
        }
        return this;
    }

    public ChunkPos readChunkPos() {
        int x = this.readInt();
        int z = this.readInt();

        return new ChunkPos(x, z);
    }

    @CanIgnoreReturnValue
    public PacketIO writeChunkPos(ChunkPos pos) {
        try {
            this.output.writeInt(pos.x());
            this.output.writeInt(pos.z());
        } catch (IOException e) {
            throw new PacketException(e);
        }
        return this;
    }

    public int readVarInt() {
        int value = 0;
        int shift = 0;
        int byteRead;

        do {
            byteRead = this.readByte();
            value |= (byteRead & 0x7F) << shift;
            shift += 7;
        } while ((byteRead & 0x80)!= 0);

        return value;
    }

    @CanIgnoreReturnValue
    public PacketIO writeVarInt(int value) {
        while ((value & ~0x7F)!= 0) {
            this.writeByte((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }

        this.writeByte((byte) value);
        return this;
    }

    public int getVarIntSize(int value) {
        int size = 0;
        while ((value & ~0x7F)!= 0) {
            size++;
            value >>>= 7;
        }
        return size + 1;
    }

    public void writeUbo(IType<?> ubo) {
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
    public final <T extends IType<?>> T readUbo(T... typeGetter) {
        T data;
        int id = this.readUnsignedByte();
        byte[] bytes = this.readByteArray(PacketIO.MAX_UBO_SIZE);

        try(DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes))) {
            Class<?> componentType = typeGetter.getClass().getComponentType();
            if (id != TypeRegistry.getId(componentType)) throw new PacketException("Id doesn't match requested type.");
            data = (T) TypeRegistry.read(TypeRegistry.getId(componentType), stream);
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
        try {
            return new PacketIO(new ByteArrayInputStream(this.input.readNBytes(length)), null);
        } catch (IOException e) {
            throw new PacketException(e);
        }
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
            byte[] bytes = this.input.readNBytes(length);
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
            return out.write(ByteBuffer.wrap(this.input.readNBytes(length)), position);
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
            this.output.write(in.readNBytes(length));
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
            return in.read(ByteBuffer.wrap(this.input.readNBytes(length)), position);
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
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        short[] array = new short[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readShort();
        }

        return array;
    }

    @CanIgnoreReturnValue
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
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readMedium();
        }

        return array;
    }

    @CanIgnoreReturnValue
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
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readInt();
        }

        return array;
    }

    @CanIgnoreReturnValue
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
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        long[] array = new long[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readLong();
        }

        return array;
    }

    @CanIgnoreReturnValue
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
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        float[] array = new float[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readFloat();
        }

        return array;
    }

    @CanIgnoreReturnValue
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
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        double[] array = new double[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readDouble();
        }

        return array;
    }

    @CanIgnoreReturnValue
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
            throw new PacketException("List too large, max = %d, actual = %d".formatted(max, size));
        }

        var list = new ArrayList<T>();

        for (int i = 0; i < size; i++) {
            list.add(decoder.apply(this));
        }

        return list;
    }

    @CanIgnoreReturnValue
    public <T> PacketIO writeList(List<T> list, BiConsumer<PacketIO, T> encoder) {
        this.writeInt(list.size());
        for (T item : list) {
            encoder.accept(this, item);
        }

        return this;
    }

    public <K, V> Map<K, V> readMap(Function<PacketIO, K> keyDecoder, Function<PacketIO, V> valueDecoder) {
        int size = this.readInt();
        var map = new HashMap<K, V>();

        for (int i = 0; i < size; i++) {
            map.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }

        return map;
    }

    public <K, V> Map<K, V> readMap(Function<PacketIO, K> keyDecoder, Function<PacketIO, V> valueDecoder, int max) {
        int size = this.readInt();
        if (size > max) {
            throw new PacketException("Map too large, max = %d, actual = %d".formatted(max, size));
        }

        var map = new HashMap<K, V>();

        for (int i = 0; i < size; i++) {
            map.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }

        return map;
    }

    @CanIgnoreReturnValue
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

    @CanIgnoreReturnValue
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
        return EnumUtils.byOrdinal(this.readVarInt(), fallback);
    }

    public BlockProperties readBlockMeta() {
        return BlockProperties.read(this);
    }

    public void writeBlockMeta(BlockProperties blockMeta) {
        blockMeta.write(this);
    }

    public PartialPacket[] split(int packetId, long sequenceId) {
        return null;
    }

    private void readBytes0(byte[] bytes) {
        if (!isReadable()) throw new WriteOnlyBufferException();

        try {
            int read = this.input.read(bytes);
            if (read < bytes.length) throw new EOFException();
            if (read != bytes.length) throw new IOException("Expected " + bytes.length + " bytes but got " + read);
        } catch (IOException e) {
            throw new PacketException(e);
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
