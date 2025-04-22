package dev.ultreon.quantum.block.state;

import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.ubo.types.DataType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@EqualsAndHashCode
public abstract class StatePropertyKey<T> {

    final String name;
    private final T[] possibleValues;
    final Class<T> type;

    public StatePropertyKey(String name, T[] possibleValues, Class<T> type) {
        this.name = name;
        this.possibleValues = possibleValues;
        this.type = type;
        if (possibleValues.length < 2) throw new IllegalArgumentException("Property must have at least two possible values");
    }

    public T read(@NotNull PacketIO packetBuffer) {
        int index = packetBuffer.readInt();
        if (index < 0 || index >= possibleValues.length) {
            throw new IllegalArgumentException("Invalid index for property " + name + ": " + index);
        }

        return type.cast(possibleValues[index]);
    }

    public void write(@NotNull PacketIO packetBuffer, Object value) {
        int index = ArrayUtils.indexOf(possibleValues, value);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid value for property " + name + ": " + value);
        }

        packetBuffer.writeInt(index);
    }

    public String getName() {
        return name;
    }

    public List<T> allPossibleValues() {
        return List.of(possibleValues);
    }

    public int size() {
        return possibleValues.length;
    }

    public abstract int indexOf(T value);

    public T valueByIndex(int valueIndex) {
        return possibleValues[valueIndex];
    }

    public abstract void load(BlockState blockState, DataType<?> value);

    public abstract DataType<?> save(BlockState blockState);
}
