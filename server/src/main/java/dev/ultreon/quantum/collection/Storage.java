package dev.ultreon.quantum.collection;

import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.rng.RNG;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

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

    @Nullable D getRandom(RNG rng, AtomicInteger integer, Predicate<D> predicate);

    void setUniform(D value);
}
