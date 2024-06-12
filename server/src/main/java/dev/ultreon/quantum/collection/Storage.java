package dev.ultreon.quantum.collection;

import dev.ultreon.ubo.types.MapType;
import dev.ultreon.quantum.network.PacketIO;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Storage<D> {

    MapType save(MapType outputData, Function<D, MapType> encoder);

    void load(MapType inputData, Function<MapType, D> decoder);


    void write(PacketIO buffer, BiConsumer<PacketIO, D> encoder);

    void read(PacketIO buffer, Function<PacketIO, D> decoder);

    boolean set(int idx, D value);

    D get(int idx);


    <R> Storage<R> map(R defaultValue, Class<R> type, Function<D, R> o);
}
