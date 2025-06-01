package dev.ultreon.quantum.block.property;

import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.ubo.types.DataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class StatePropertyKey<T> {
    private final List<T> values;
    private String name;
    private Class<T> type;

    public StatePropertyKey(String name, T[] values, Class<T> type) {
        this.values = List.of(values);
        this.name = name;
    }

    public List<T> getValues() {
        return values;
    }

    public int getValueIndex(T value) {
        int idx = values.indexOf(value);
        if (idx < 0) throw new IllegalArgumentException("Invalid value: " + value);
        return idx;
    }

    public T getValueByIndex(int index) {
        return values.get(index);
    }

    public int getValueCount() {
        return values.size();
    }

    public T read(@NotNull PacketIO packetBuffer) {
        int index = packetBuffer.readInt();
        if (index < 0 || index >= values.size()) {
            throw new IllegalArgumentException("Invalid index for property " + name + ": " + index);
        }

        return type.cast(values.get(index));
    }

    public void write(@NotNull PacketIO packetBuffer, Object value) {
        int index = values.indexOf(value);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid value for property " + name + ": " + value);
        }

        packetBuffer.writeInt(index);
    }

    public abstract int indexOf(T value);

    public abstract void load(BlockState blockState, DataType<?> value);

    public abstract DataType<?> save(BlockState blockState);

    public String getName() {
        return name;
    }
}
