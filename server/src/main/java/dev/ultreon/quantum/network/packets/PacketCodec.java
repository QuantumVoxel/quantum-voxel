package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.network.PacketIO;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PacketCodec<T> {
    T read(PacketIO packetIO);

    void write(PacketIO packetIO, T data);

    static <T> PacketCodec<T> unit(Supplier<T> supplier) {
        return of(packetIO -> supplier.get(), (packetIO, data) -> {
        });
    }

    static <T> PacketCodec<T> unit(T constant) {
        return of(packetIO -> constant, (packetIO, data) -> {
        });
    }

    static <T> PacketCodec<T> nothing() {
        return unit(() -> null);
    }

    interface Writer<T> {
        void write(PacketIO packetIO, T data);
    }

    interface Reader<T> {
        T read(PacketIO packetIO);
    }

    static <T> PacketCodec<T> of(Reader<T> reader, Writer<T> writer) {
        return new PacketCodec<T>() {
            @Override
            public T read(PacketIO packetIO) {
                return reader.read(packetIO);
            }

            @Override
            public void write(PacketIO packetIO, T data) {
                writer.write(packetIO, data);
            }
        };
    }

    static <R, T1, T2> PacketCodec<R> pair(
            PacketCodec<T1> first,
            PacketCodec<T2> second,
            BiFunction<T1, T2, R> function,
            Function<R, T1> firstValue,
            Function<R, T2> secondValue) {
        return new PacketCodec<R>() {
            @Override
            public R read(PacketIO packetIO) {
                T1 firstValue = first.read(packetIO);
                T2 secondValue = second.read(packetIO);
                return function.apply(firstValue, secondValue);
            }

            @Override
            public void write(PacketIO packetIO, R data) {
                first.write(packetIO, firstValue.apply(data));
                second.write(packetIO, secondValue.apply(data));
            }
        };
    }
}
