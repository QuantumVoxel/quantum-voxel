package dev.ultreon.quantum.collection;

import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.ubo.types.MapType;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public interface Storage<D> extends Cloneable {

    MapType save(MapType outputData, Function<D, MapType> encoder);

    void load(MapType inputData, Function<MapType, D> decoder);


    void write(PacketIO buffer, BiConsumer<PacketIO, D> encoder);

    void read(PacketIO buffer, Function<PacketIO, D> decoder);

    boolean set(int idx, D value);

    D get(int idx);

    <R> Storage<R> map(R defaultValue, IntFunction<R[]> type, Function<D, R> o);

    Storage<D> clone();

    boolean isUniform();
}
