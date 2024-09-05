package dev.ultreon.quantum.collection;

import com.google.common.base.Preconditions;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.ubo.DataKeys;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import it.unimi.dsi.fastutil.objects.Reference2ShortFunction;
import it.unimi.dsi.fastutil.shorts.Short2ReferenceFunction;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class FlatStorage<D> implements Storage<D> {
    private final D defaultValue;
    private D[] data;

    public FlatStorage(D defaultValue, D[] data) {
        this.defaultValue = defaultValue;
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public FlatStorage(D defaultValue, int size) {
        this.defaultValue = defaultValue;
        this.data = (D[]) Array.newInstance(defaultValue.getClass(), size);
    }

    @SuppressWarnings("unchecked")
    public FlatStorage(D defaultValue, short[] shorts, Short2ReferenceFunction<D> decoder) {
        this.defaultValue = defaultValue;

        this.data = (D[]) Array.newInstance(defaultValue.getClass(), shorts.length);
        for (int i = 0; i < shorts.length; i++) {
            this.data[i] = decoder.get(shorts[i]);
        }
    }

    @SuppressWarnings("unchecked")
    public FlatStorage(D defaultValue, List<D> data) {
        this.defaultValue = defaultValue;

        this.data = (D[]) Array.newInstance(defaultValue.getClass(), data.size());
        for (int i = 0, dataSize = data.size(); i < dataSize; i++) {
            this.data[i] = data.get(i);
        }
    }

    @SuppressWarnings("unchecked")
    public FlatStorage(D defaultValue, PacketIO buffer, Function<PacketIO, D> decoder) {
        this.defaultValue = defaultValue;

        int size = buffer.readInt();
        this.data = (D[]) Array.newInstance(defaultValue.getClass(), size);

        for (int i = 0; i < size; i++) {
            this.data[i] = decoder.apply(buffer);
        }
    }

    public FlatStorage(int size, D defaultValue) {
        this(defaultValue, size);
    }

    @Override
    public MapType save(MapType outputData, Function<D, MapType> encoder) {
        ListType<MapType> data = new ListType<>();
        for (@Nullable D entry : this.data) if (entry != null) data.add(encoder.apply(entry));

        outputData.put("Data", data);
        return outputData;
    }

    @Override
    public void load(MapType inputData, Function<MapType, D> decoder) {
        var data = inputData.<MapType>getList(DataKeys.PALETTE_DATA);
        List<MapType> value = data.getValue();
        for (int i = 0, valueSize = value.size(); i < valueSize; i++) {
            MapType entryData = value.get(i);
            if (entryData == null) {
                this.data[i] = defaultValue;
                continue;
            }
            D entry = decoder.apply(entryData);
            this.data[i] = entry;
        }
    }

    @Override
    public void write(PacketIO buffer, BiConsumer<PacketIO, D> encoder) {
        buffer.writeInt(this.data.length);
        D[] ds = this.data;
        for (D entry : ds) {
            if (entry == null) entry = defaultValue;
            encoder.accept(buffer, entry);
        }
    }

    @Override
    public void read(PacketIO buffer, Function<PacketIO, D> decoder) {
        var size = buffer.readInt();
        this.data = Arrays.copyOf(this.data, size);
        for (int i = 0; i < size; i++) {
            D entry = decoder.apply(buffer);
            if (entry == null) entry = defaultValue;
            this.data[i] = entry;
        }
    }

    @Override
    public boolean set(int idx, D value) {
        Preconditions.checkNotNull(value, "value");

        if (idx < 0 || idx >= this.data.length) {
            throw new ArrayIndexOutOfBoundsException(idx);
        }

        D datum = data[idx];
        if (datum == value) return false;
        this.data[idx] = value;
        return true;
    }

    @Override
    public D get(int idx) {
        if (idx < 0 || idx >= this.data.length) {
            throw new ArrayIndexOutOfBoundsException(idx);
        }

        D datum = this.data[idx];
        return datum == null ? defaultValue : datum;
    }

    public short[] serialize(Reference2ShortFunction<D> encoder) {
        short[] data = new short[this.data.length];
        D[] ds = this.data;
        for (int i = 0, dsLength = ds.length; i < dsLength; i++) {
            var datum = ds[i];
            if (datum != null) {
                data[i] =encoder.getShort(datum);
            }
        }
        return data;
    }

    @Override
    public <R> Storage<R> map(R defaultValue, IntFunction<R[]> type, Function<D, R> o) {
        var data = Arrays.stream(this.data).map(o).collect(Collectors.toList());
        return new FlatStorage<>(defaultValue, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FlatStorage<D> clone() throws CloneNotSupportedException {
        return (FlatStorage<D>) super.clone();
    }
}
