package dev.ultreon.quantum.collection;

import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class IndexedArray<T> implements Storage<T> {
    private final List<T> data;
    private final int[] indexArray;
    private final T defaultValue;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final int size;

    public IndexedArray(int size, T defaultValue) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0.");
        }
        if (defaultValue == null) {
            throw new IllegalArgumentException("Default value cannot be null.");
        }

        this.size = size;
        this.data = Collections.synchronizedList(new ArrayList<>());
        this.defaultValue = defaultValue;

        // Set the default value in the first position and initialize reference count
        data.add(defaultValue);

        this.indexArray = new int[size];
        for (int i = 0; i < size; i++) {
            indexArray[i] = 0;  // Initialize all indices to point to the default value
        }
    }

    private int indexOf(T value) {
        return data.indexOf(value);
    }

    public T get(int index) {
        lock.readLock().lock();
        try {
            return data.get(indexArray[index]);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public <R> Storage<R> map(R defaultValue, Class<R> type, Function<T, R> o) {
        return null;
    }

    @Override
    public MapType save(MapType outputData, Function<T, MapType> encoder) {
        this.lock.readLock().lock();

        try {
            ListType<MapType> data = new ListType<>();
            for (int i = 0; i < size; i++) {
                T value = get(i);
                if (value != null) {
                    data.add(encoder.apply(value));
                }
            }

            outputData.put("data", data);
            outputData.putIntArray("index", indexArray);
            return outputData;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public void load(MapType inputData, Function<MapType, T> decoder) {

    }

    @Override
    public void write(PacketIO buffer, BiConsumer<PacketIO, T> encoder) {

    }

    @Override
    public void read(PacketIO buffer, Function<PacketIO, T> decoder) {

    }

    public boolean set(int index, T value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot set a null value.");
        }

        lock.writeLock().lock();
        try {
            int oldIndex = indexArray[index];
            T oldValue = data.get(oldIndex);

            if (value.equals(oldValue)) {
                // No change needed if the value is the same
                return false;
            }

            // Check if the value is already in the data list
            int newIndex = indexOf(value);
            if (newIndex == -1) {
                // Add the new value to the data list
                data.add(value);
                newIndex = data.size() - 1;
            }

            // Update the index array with the new index
            indexArray[index] = newIndex;

            // Optionally, remove old value if it's no longer referenced and not the default value
            if (oldIndex != 0 && indexOf(oldValue) == oldIndex && oldValue != defaultValue) {
                data.set(oldIndex, null);
            }

            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(int index) {
        lock.writeLock().lock();
        try {
            int oldIndex = indexArray[index];
            T oldValue = data.get(oldIndex);

            // Reset to default value
            indexArray[index] = 0;

            // Optionally, remove old value if it's no longer referenced and not the default value
            if (oldIndex != 0 && indexOf(oldValue) == oldIndex && oldValue != defaultValue) {
                data.set(oldIndex, null);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        return size;
    }
}
