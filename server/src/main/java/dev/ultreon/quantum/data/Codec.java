package dev.ultreon.quantum.data;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiConsumer;
import java.util.function.Function;

@ApiStatus.Experimental
public interface Codec<T> {
    default <R> Codec<R> xmap(Function<T, R> readMap, Function<R, T> writeMap) {
        Codec<T> self = this;
        return new Codec<>() {
            @Override
            public R read(DataReader<?> reader) {
                return readMap.apply(self.read(reader));
            }

            @Override
            public void write(DataWriter<?> writer, R data) {
                self.write(writer, writeMap.apply(data));
            }
        };
    }

    void write(DataWriter<?> writer, T data);

    T read(DataReader<?> reader);

    static <T> Codec<T> unit(T constant) {
        return new Codec<>() {
            @Override
            public T read(DataReader<?> reader) {
                return constant;
            }

            @Override
            public void write(DataWriter<?> writer, T data) {
                // do nothing
            }
        };
    }

    static <T> Codec<T> nothing() {
        return new Codec<>() {
            @Override
            public T read(DataReader<?> reader) {
                return null;
            }

            @Override
            public void write(DataWriter<?> writer, T data) {
                // do nothing
            }
        };
    }

    static <T> Codec<T> of(Function<DataReader<?>, T> readerFn, BiConsumer<DataWriter<?>, T> writerFn) {
        return new Codec<>() {
            @Override
            public T read(DataReader<?> reader) {
                return readerFn.apply(reader);
            }

            @Override
            public void write(DataWriter<?> writer, T data) {
                writerFn.accept(writer, data);
            }
        };
    }

    Codec<Byte> BYTE = of(DataReader::readByte, DataWriter::writeByte);
    Codec<Short> SHORT = of(DataReader::readShort, DataWriter::writeShort);
    Codec<Integer> INT = of(DataReader::readInt, DataWriter::writeInt);
    Codec<Long> LONG = of(DataReader::readLong, DataWriter::writeLong);
    Codec<Float> FLOAT = of(DataReader::readFloat, DataWriter::writeFloat);
    Codec<Double> DOUBLE = of(DataReader::readDouble, DataWriter::writeDouble);
    Codec<String> STRING = of(DataReader::readString, DataWriter::writeString);
    Codec<Boolean> BOOLEAN = of(DataReader::readBoolean, DataWriter::writeBoolean);
}
