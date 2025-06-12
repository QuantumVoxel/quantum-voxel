package dev.ultreon.quantum.data;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

@ApiStatus.Experimental
public interface Codec<T> {
    default <R> Codec<R> map(Function<T, R> readMap, Function<R, T> writeMap) {
        Codec<T> self = this;
        return new Codec<>() {
            @Override
            public <D> DataResult<R> read(DataReader<D> reader, D data) {
                return self.read(reader, data).map(readMap);
            }

            @Override
            public <D> DataResult<D> write(DataWriter<D> writer, R obj) {
                return self.write(writer, writeMap.apply(obj));
            }
        };
    }

    <D> DataResult<D> write(DataWriter<D> writer, T data);

    <D> DataResult<T> read(DataReader<D> reader, D data);

    static <T> Codec<T> unit(T constant) {
        return new Codec<>() {
            @Override
            public <D> DataResult<D> write(DataWriter<D> writer, T data) {
                return new DataResult<>(writer.unit(), true) ;
            }

            @Override
            public <D> DataResult<T> read(DataReader<D> reader, D data) {
                return new DataResult<>(constant, true) ;
            }
        };
    }

    static <T> Codec<T> nothing() {
        return new Codec<>() {
            @Override
            public <D> DataResult<D> write(DataWriter<D> writer, T data) {
                return new DataResult<>(null, true) ;
            }

            @Override
            public <D> DataResult<T> read(DataReader<D> reader, D data) {
                return new DataResult<>(null, true) ;
            }
        };
    }

    @ApiStatus.Internal
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <D, T> Codec<T> of(BiFunction<DataReader<D>, D, T> readerFn, BiFunction<DataWriter<D>, T, D> writerFn) {
        return new Codec() {
            @Override
            public DataResult write(DataWriter writer, Object data) {
                return new DataResult<>(writerFn.apply((DataWriter<D>) writer, (T) data), true);
            }

            @Override
            public DataResult read(DataReader reader, Object data) {
                return new DataResult<>(readerFn.apply((DataReader<D>) reader, (D) data), true);
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

    default MapCodec<T> fieldOf(String key) {
        return new MapCodec<T>() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public <D> T decode(DataOps<D> ops, D data) {
                return Codec.this.read(ops, data).getValue();
            }

            @Override
            public <D> D encode(DataOps<D> ops, T value) {
                return Codec.this.write(ops, value).getValue();
            }
        };
    }

    default Codec<List<T>> listOf() {
        return new Codec<>() {
            @Override
            public <D> DataResult<D> write(DataWriter<D> writer, List<T> data) {
                D listWriter = writer.createList();
                for (T item : data) {
                    DataResult<D> result = Codec.this.write(writer, item);
                    if (!result.isSuccessful()) {
                        D partialValue = result.getPartialValue();
                        if (partialValue == null) {
                            return new DataResult<>(listWriter, false);
                        }
                        writer.writeListItem(listWriter, partialValue);
                    }
                }

                return new DataResult<>(listWriter, true);
            }

            @Override
            public <D> DataResult<List<T>> read(DataReader<D> reader, D data) {
                AtomicBoolean successful = new AtomicBoolean(reader.isList(data));
                List<T> list = new ArrayList<>();
                reader.iterate(data, t -> {
                    if (!successful.get()) return;
                    DataResult<T> result = Codec.this.read(reader, t);
                    if (!result.isSuccessful()) {
                        successful.set(false);
                        return;
                    }
                    list.add(result.getValue());
                });

                boolean s = successful.get();
                return new DataResult<>(s ? list : null, s);
            }
        };
    }

    default Codec<Optional<T>> optionalOf() {
        return new Codec<>() {
            @Override
            public <D> DataResult<D> write(DataWriter<D> writer, Optional<T> data) {
                return data.<DataResult<D>>map(t -> Codec.this.write(writer, t))
                        .orElseGet(() -> new DataResult<>(writer.unit(), true));
            }

            @Override
            public <D> DataResult<Optional<T>> read(DataReader<D> reader, D data) {
                return reader.isNull(data)
                        ? new DataResult<>(Optional.empty(), true)
                        : Codec.this.read(reader, data).map(Optional::of) ;
            }
        };
    }
}
