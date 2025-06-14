package dev.ultreon.quantum.ubo;

import dev.ultreon.quantum.ubo.types.DataType;
import dev.ultreon.quantum.ubo.util.DataTypeVisitor;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataIo {
    private static final short VERSION = 3;
    private static final int HEADER = 0xff804269;
    private static final int BUFFER_SIZE = 4096;

    @SafeVarargs
    public static <T extends DataType<?>> T read(File file, T... type) throws IOException {
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(file.toPath()), BUFFER_SIZE)) {
            return read(stream, type);
        }
    }

    @SafeVarargs
    public static <T extends DataType<?>> T read(URL url, T... type) throws IOException {
        try (InputStream stream = new BufferedInputStream(url.openStream(), BUFFER_SIZE)) {
            return read(stream, type);
        }
    }

    @SafeVarargs
    public static <T extends DataType<?>> T read(byte[] data, T... type) throws IOException {
        try (InputStream stream = new BufferedInputStream(new ByteArrayInputStream(data), BUFFER_SIZE)) {
            return read(stream, type);
        }
    }

    /**
     * @throws IOException       when an I/O error occurs.
     * @throws DataTypeException when the read data type is invalid.
     */
    @SafeVarargs
    public static <T extends DataType<?>> T read(InputStream stream, T... type) throws IOException {
        if (stream instanceof DataInput) {
            return read((DataInput) stream, type);
        }
        return read((DataInput) new DataInputStream(stream), type);
    }

    /**
     * @throws IOException       when an I/O error occurs.
     * @throws DataTypeException when the read data type is invalid.
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T extends DataType<?>> T read(DataInput input, T... type) throws IOException {
        int magic = input.readInt();
        if (magic != HEADER) {
            throw new IOException(String.format("Invalid header got 0x%08X (expected 0xFF804269)", magic));
        }

        short readVersion = input.readShort();
        if (readVersion > VERSION) {
            throw new FutureVersionException(readVersion, VERSION);
        }

        Class<T> componentType = (Class<T>) type.getClass().getComponentType();
        int componentId = DataTypeRegistry.getId(componentType);
        int id = input.readUnsignedByte();

        if (componentId != id) {
            throw new DataTypeException("The read data id " + id + " is different from the expected id: " + componentId);
        }

        return (T) DataTypeRegistry.read(id, input);
    }

    @SafeVarargs
    public static <T extends DataType<?>> T readCompressed(File file, T... type) throws IOException {
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(file.toPath()), BUFFER_SIZE)) {
            return readCompressed(stream, type);
        }
    }

    @SafeVarargs
    public static <T extends DataType<?>> T readCompressed(URL url, T... type) throws IOException {
        try (InputStream stream = new BufferedInputStream(url.openStream())) {
            return readCompressed(stream, type);
        }
    }

    @SafeVarargs
    public static <T extends DataType<?>> T readCompressed(byte[] data, T... type) throws IOException {
        try (InputStream stream = new BufferedInputStream(new ByteArrayInputStream(data))) {
            return readCompressed(stream, type);
        }
    }

    @SafeVarargs
    public static <T extends DataType<?>> T readCompressed(InputStream stream, T... type) throws IOException {
        GZIPInputStream gzipStream = new GZIPInputStream(stream);
        return read(gzipStream, type);
    }

    public static void write(DataType<?> dataType, File file) throws IOException {
        try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(file.toPath()), BUFFER_SIZE)) {
            write(dataType, stream);
        }
    }

    public static void write(DataType<?> dataType, URL file) throws IOException {
        try (OutputStream stream = new BufferedOutputStream(file.openConnection().getOutputStream(), BUFFER_SIZE)) {
            write(dataType, stream);
        }
    }

    public static byte[] write(DataType<?> dataType) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream stream = new BufferedOutputStream(bos, BUFFER_SIZE)) {
            write(dataType, stream);
        }
        return bos.toByteArray();
    }

    public static void write(DataType<?> dataType, OutputStream stream) throws IOException {
        if (stream instanceof DataOutput) {
            write(dataType, (DataOutput) stream);
        }
        write(dataType, (DataOutput) new DataOutputStream(stream));
    }

    public static void write(DataType<?> dataType, DataOutput output) throws IOException {
        output.writeInt(HEADER);
        output.writeShort(VERSION); // Version
        output.writeByte(dataType.id()); // Type
        dataType.write(output);
    }

    public static void writeCompressed(DataType<?> dataType, URL file) throws IOException {
        try (OutputStream stream = new BufferedOutputStream(file.openConnection().getOutputStream(), BUFFER_SIZE)) {
            writeCompressed(dataType, stream);
        }
    }

    public static byte[] writeCompressed(DataType<?> dataType) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream stream = new BufferedOutputStream(bos, BUFFER_SIZE)) {
            writeCompressed(dataType, stream);
        }
        return bos.toByteArray();
    }

    public static void writeCompressed(DataType<?> dataType, File file) throws IOException {
        try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(file.toPath()), BUFFER_SIZE)) {
            writeCompressed(dataType, stream);
        }
    }

    public static void writeCompressed(DataType<?> dataType, OutputStream stream) throws IOException {
        GZIPOutputStream gzipStream = new GZIPOutputStream(stream);
        write(dataType, gzipStream);
        gzipStream.finish();
        gzipStream.flush();
    }

    public static String toUso(DataType<?> dataType) {
        return dataType.writeUso();
    }

    public static <T> T visit(DataTypeVisitor<T> visitor, DataType<?> dataType) {
        return dataType.accept(visitor);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T extends DataType<?>> T fromUso(String value, T... type) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(value))) {
            DataType<?> iDataType = readUso(reader.readLine());
            return (T) iDataType;
        }
    }

    private static DataType<?> readUso(String value) throws IOException {
        return new UsoParser(value).parse();
    }

}
