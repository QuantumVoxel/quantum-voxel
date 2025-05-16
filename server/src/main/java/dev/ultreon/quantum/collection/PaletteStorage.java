package dev.ultreon.quantum.collection;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.ubo.DataKeys;
import dev.ultreon.quantum.ubo.types.ListType;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.rng.RNG;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * <p>Palette storage is used for storing data in palettes.
 * It's used for optimizing memory and storage usage.
 * Generally used for advanced voxel games.</p>
 *
 * <p>It makes use of short arrays to store {@link #getPalette() index pointers} to the {@linkplain #getData() data}.
 * While the data itself is stored without any duplicates.</p>
 *
 * @param <D> the data type.
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
@ApiStatus.Experimental
public class PaletteStorage<D> implements Disposable, Storage<D> {
    private final D defaultValue;
    private short[] palette;
    private Array<D> data = new Array<>();

    @Deprecated
    public PaletteStorage(D defaultValue, int size) {
        this(size, defaultValue);
    }

    public PaletteStorage(D defaultValue, short[] palette, Array<D> data) {
        this.defaultValue = defaultValue;
        this.palette = palette;
        this.data = data;
    }

    public PaletteStorage(D defaultValue, PacketIO buffer, Function<PacketIO, D> decoder) {
        this(0, defaultValue);
        this.read(buffer, decoder);
    }

    public PaletteStorage(int size, D defaultValue) {
        this.defaultValue = defaultValue;

        this.palette = new short[size];
        Arrays.fill(this.palette, (short) -1);
    }

    @Override
    public MapType save(MapType outputData, Function<D, MapType> encoder) {
        ListType<MapType> data = new ListType<>();
        for (@NotNull D entry : this.data.toArray()) {
            data.add(encoder.apply(entry));
        }
        outputData.put("Data", data);

        outputData.putShortArray("Palette", this.palette);

        return outputData;
    }

    @Override
    public void load(MapType inputData, Function<MapType, D> decoder) {
        this.data.clear();
        var data = inputData.<MapType>getList(DataKeys.PALETTE_DATA);
        for (MapType entryData : data.getValue()) {
            D entry = decoder.apply(entryData);
            if (entry == null) {
                this.data.add(this.defaultValue);
                continue;
            }
            this.data.add(entry);
        }

        this.palette = inputData.getShortArray(DataKeys.PALETTE, new short[this.palette.length]);
    }

    @Override
    public void write(PacketIO buffer, BiConsumer<PacketIO, D> encoder) {
        buffer.writeVarInt(this.data.size);
        for (D entry : this.data.toArray()) if (entry != null) encoder.accept(buffer, entry);
        buffer.writeVarInt(this.palette.length);
        for (short v : this.palette) {
            if (v != -1 && v >= this.data.size) throw new IllegalArgumentException("Invalid palette index " + v);
            buffer.writeShort(v);
        }
    }

    @Override
    public void read(PacketIO buffer, Function<PacketIO, D> decoder) {
        var data = new Array<D>(defaultValue.getClass());
        var dataSize = buffer.readVarInt();
        for (int i = 0; i < dataSize; i++)
            data.add(decoder.apply(buffer));
        this.data = data;

        short[] palette = new short[buffer.readVarInt()];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = buffer.readShort();
            if (palette[i] != -1 && palette[i] >= this.data.size)
                throw new IllegalArgumentException("Invalid palette index " + palette[i]);
        }

        this.palette = palette;
    }

    @Override
    public boolean set(int idx, D value) {
        if (value == null) {
            this.remove(idx);
            return false;
        }

        short old = this.palette[idx];

        short setIdx = (short) this.data.indexOf(value, false);
        if (setIdx == -1) {
            setIdx = this.add(idx, value);
        }
        this.palette[idx] = setIdx;

        if (old < 0 || ArrayUtils.contains(this.palette, old))
            return false;

        int i1 = data.indexOf(value, false);
        if (i1 >= 0) {
            this.data.set(old, value);
            return false;
        }

        this.data.removeIndex(old);

        // Update paletteMap entries for indices after the removed one
        for (int i = 0; i < this.palette.length; i++) {
            int oldValue = this.palette[i];
            this.palette[i] = (short) (oldValue - 1);
        }
        return false;
    }

    public short toDataIdx(int idx) {
        return idx >= 0 && idx < this.palette.length ? this.palette[idx] : -1;
    }

    public D direct(int dataIdx) {
        if (dataIdx >= 0 && dataIdx < this.data.size) {
            D d = this.data.get(dataIdx);
            return d != null ? d : this.defaultValue;
        }

        return this.defaultValue;
    }

    public short add(int idx, D value) {
        short dataIdx = (short) (this.data.size);
        this.data.add(value);
        this.palette[idx] = dataIdx;
        return dataIdx;
    }

    public void remove(int idx) {
        if (idx >= 0 && idx < this.data.size) {
            int dataIdx = this.toDataIdx(idx);
            if (dataIdx < 0) return;
            this.data.removeIndex(dataIdx);
            this.palette[idx] = -1;

            // Update paletteMap entries for indices after the removed one
            for (int i = idx; i < this.palette.length; i++) {
                int oldValue = this.palette[i];
                this.palette[i] = (short) (oldValue - 1);
            }
        }
    }

    @Override
    public void dispose() {
        this.data.clear();
        this.palette = null;
    }

    @NotNull
    @Override
    public D get(int idx) {
        short paletteIdx = this.toDataIdx(idx);
        return paletteIdx < 0 ? this.defaultValue : this.direct(paletteIdx);
    }

    @Override
    public <R> Storage<R> map(@NotNull R defaultValue, IntFunction<R[]> generator, @NotNull Function<@NotNull D, @Nullable R> mapper) {
        var ref = new Object() {
            final transient Function<D, R> mapperRef = mapper;
        };

        R[] data = generator.apply(this.data.size);
        for (int i = 0; i < this.data.size; i++) {
            D d = this.data.get(i);
            if (ref.mapperRef == null) {
                QuantumServer.LOGGER.warn("Mapper in PaletteStorage.mapper(...) just nullified out of thin air! What the f*** is going on?");
                data[i] = defaultValue;
                continue;
            }

            R applied;
            try {
                applied = ref.mapperRef.apply(d);
            } catch (NullPointerException e) {
                QuantumServer.LOGGER.warn("Something sus going on, why is there a nullptr? Double check passed, third check failed :huh:", e);
                data[i] = defaultValue;
                continue;
            }
            data[i] = applied == null ? defaultValue : applied;
        }
        return new PaletteStorage<>(defaultValue, this.palette, new Array<>(data));
    }

    public short[] getPalette() {
        short[] palette = this.palette.clone();
        return palette;
    }

    public List<D> getData() {
        return List.of(this.data.toArray());
    }

    public void set(short[] palette, D[] data) {
        set(palette, new Array<>(data));
    }

    public void set(short[] palette, Array<D> data) {
        if (this.palette.length != palette.length)
            throw new IllegalArgumentException("Palette length must be equal.");

        if (this.data.contains(null, true))
            throw new IllegalArgumentException("Data cannot contain null values.");

        this.palette = palette;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        PaletteStorage<?> that = (PaletteStorage<?>) o;
        return Arrays.equals(this.palette, that.palette) && this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = this.data.hashCode();
        result = 31 * result + Arrays.hashCode(this.palette);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PaletteStorage<D> clone() {
        try {
            return (PaletteStorage<D>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isUniform() {
        return data.size <= 1;
    }

    @Override
    public @NotNull D getRandom(RNG rng, AtomicInteger integer, Predicate<D> predicate) {
        if (this.data.size == 0) return null;

        IntList list = new IntArrayList();
        D[] array = this.data.toArray();
        for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
            D d = array[i];
            if (!predicate.test(d)) continue;
            list.add(i);
        }
        if (list.isEmpty()) return null;

        int[] realIndexes = findIndexes(this.palette, list.toIntArray());
        int rand = realIndexes[rng.nextInt(realIndexes.length)];
        integer.set(rand);
        return get(rand);
    }

    @Override
    public void setUniform(D value) {
        this.data.clear();
        this.data.add(value);
        Arrays.fill(this.palette, (short) 0);
    }

    private int[] findIndexes(short[] arr, int[] val) {
        int[] indexes = new int[arr.length];
        int count = 0;
        int i = 0;

        // Find all indexes from 0 to val.size() in the palette
        for (short v : arr) {
            if (ArrayUtils.contains(val, v)) {
                indexes[i++] = v;
                count++;
            }
        }

        return Arrays.copyOf(indexes, count);
    }
}
