package dev.ultreon.quantum.collection;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.google.common.base.Preconditions;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.ubo.DataKeys;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * <p>Palette storage is used for storing data in palettes.
 * It's used for optimizing memory and storage usage.
 * Generally used for advanced voxel games.</p>
 *
 * <p>It makes use of short arrays to store {@link #getPalette() index pointers} to the {@linkplain #getData() data}.
 * While the data itself is stored without any duplicates.</p>
 *
 * @param <D> the data type.
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
@ApiStatus.Experimental
public class PaletteStorage<D> implements Disposable, Storage<D> {
    private final D defaultValue;
    private short[] palette;
    private Array<D> data = new Array<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

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
        this.rwLock.readLock().lock();
        try {
            ListType<MapType> data = new ListType<>();
            for (@NotNull D entry : this.data.toArray()) {
                if (entry == null) {
                    throw new IllegalArgumentException("Cannot save null data!");
                }
                data.add(encoder.apply(entry));
            }
            outputData.put("Data", data);

            outputData.putShortArray("Palette", this.palette);

            return outputData;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    @Override
    public void load(MapType inputData, Function<MapType, D> decoder) {
        this.rwLock.writeLock().lock();
        try {
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
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    @Override
    public void write(PacketIO buffer, BiConsumer<PacketIO, D> encoder) {
        this.rwLock.readLock().lock();
        try {
            buffer.writeVarInt(this.data.size);
            for (D entry : this.data.toArray()) if (entry != null) encoder.accept(buffer, entry);
            buffer.writeVarInt(this.palette.length);
            for (short v : this.palette) {
                if (v != -1 && v >= this.data.size) throw new IllegalArgumentException("Invalid palette index " + v);
                buffer.writeShort(v);
            }
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    @Override
    public void read(PacketIO buffer, Function<PacketIO, D> decoder) {
        this.rwLock.writeLock().lock();
        try {
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
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean set(int idx, D value) {
        this.rwLock.writeLock().lock();
        try {
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
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    public short toDataIdx(int idx) {
        this.rwLock.readLock().lock();
        try {
            return idx >= 0 && idx < this.palette.length ? this.palette[idx] : -1;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public D direct(int dataIdx) {
        this.rwLock.readLock().lock();
        try {
            if (dataIdx >= 0 && dataIdx < this.data.size) {
                D d = this.data.get(dataIdx);
                return d != null ? d : this.defaultValue;
            }

            return this.defaultValue;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public short add(int idx, D value) {
        this.rwLock.writeLock().lock();
        try {
            Preconditions.checkNotNull(value, "value");

            short dataIdx = (short) (this.data.size);
            this.data.add(value);
            this.palette[idx] = dataIdx;
            return dataIdx;
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    public void remove(int idx) {
        this.rwLock.writeLock().lock();
        try {
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
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    @Override
    public void dispose() {
        this.data.clear();
        this.palette = null;
    }

    @Nullable
    @Override
    public D get(int idx) {
        this.rwLock.readLock().lock();
        try {
            short paletteIdx = this.toDataIdx(idx);
            return paletteIdx < 0 ? this.defaultValue : this.direct(paletteIdx);
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    @Override
    public <R> Storage<R> map(@NotNull R defaultValue, Class<R> type, @NotNull Function<@NotNull D, @Nullable R> mapper) {
        Preconditions.checkNotNull(defaultValue, "defaultValue");
        Preconditions.checkNotNull(mapper, "mapper");

        var ref = new Object() {
            final transient Function<D, R> mapperRef = mapper;
        };

        this.rwLock.readLock().lock();
        try {
            @SuppressWarnings("unchecked") var data = Arrays.stream(this.data.toArray()).map(d -> {
                if (ref.mapperRef == null) {
                    QuantumServer.LOGGER.warn("Mapper in PaletteStorage.mapper(...) just nullified out of thin air! What the f*** is going on?");
                    return defaultValue;
                }

                R applied;
                try {
                    applied = ref.mapperRef.apply(d);
                } catch (NullPointerException e) {
                    QuantumServer.LOGGER.warn("Something sus going on, why is there a nullptr? Double check passed, third check failed :huh:", e);
                    return defaultValue;
                }
                if (applied == null) return defaultValue;
                return applied;
            }).toArray(value -> (R[])java.lang.reflect.Array.newInstance(type, value));
            return new PaletteStorage<>(defaultValue, this.palette, new Array<>(data));
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public short[] getPalette() {
        this.rwLock.readLock().lock();
        try {
            short[] palette = this.palette.clone();
            this.rwLock.readLock().unlock();
            return palette;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public List<D> getData() {
        this.rwLock.readLock().lock();
        try {
            return List.of(this.data.toArray());
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public void set(short[] palette, D[] data) {
        Preconditions.checkNotNull(palette, "palette");
        Preconditions.checkNotNull(data, "data");

        set(palette, new Array<>(data));
    }

    public void set(short[] palette, Array<D> data) {
        Preconditions.checkNotNull(palette, "palette");
        Preconditions.checkNotNull(data, "data");

        this.rwLock.writeLock().lock();
        try {
            if (this.palette.length != palette.length)
                throw new IllegalArgumentException("Palette length must be equal.");

            if (this.data.contains(null, true))
                throw new IllegalArgumentException("Data cannot contain null values.");

            this.palette = palette;
            this.data = data;
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        this.rwLock.readLock().lock();
        try {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            PaletteStorage<?> that = (PaletteStorage<?>) o;
            return Arrays.equals(this.palette, that.palette) && this.data.equals(that.data);
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    @Override
    public int hashCode() {
        this.rwLock.readLock().lock();
        try {
            int result = this.data.hashCode();
            result = 31 * result + Arrays.hashCode(this.palette);
            return result;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }
}
