package dev.ultreon.quantum.block.state;

import dev.ultreon.libs.commons.v0.util.EnumUtils;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.ubo.types.*;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

public class BlockStateDefinition {
    private final Block block;
    private final Map<String, StatePropertyKey<?>> propertyByName = new HashMap<>();
    private final SequencedMap<StatePropertyKey<?>, Object> properties = new LinkedHashMap<>();
    private StatePropertyKey<?>[] keys;
    private Object[] defaults;

    public BlockStateDefinition(Block block) {
        this.block = block;
    }

    public <T> BlockStateDefinition set(StatePropertyKey<T> key, T value) {
        properties.put(key, value);
        propertyByName.put(key.name, key);
        return this;
    }

    public BlockState build() {
        if (keys != null || defaults != null) throw new IllegalStateException("Block state definition already built");
        int idx = 0;
        StatePropertyKey<?>[] keys = new StatePropertyKey[properties.size()];
        Object[] defaults = new Object[properties.size()];

        for (Map.Entry<StatePropertyKey<?>, Object> entry : properties.sequencedEntrySet()) {
            keys[idx] = entry.getKey();
            defaults[idx] = entry.getValue();
            idx++;
        }

        this.keys = keys;
        this.defaults = defaults;
        return new BlockState(block, keys, defaults);
    }

    public BlockState read(@NotNull PacketIO packetBuffer) {
        int size = packetBuffer.readMedium();
        Object[] values = new Object[size];
        for (int i = 0; i < size; i++) {
            StatePropertyKey<?> key = keys[i];
            Object property = properties.get(key);
            if (property == null)
                throw new IllegalStateException("Property " + key + " does not exist in block " + block);

            Object read = key.read(packetBuffer);
            values[i] = read;
        }

        return new BlockState(block, keys, values);
    }

    public void write(BlockState state, @NotNull PacketIO packetBuffer) {
        packetBuffer.writeMedium(properties.size());
        for (int i = 0; i < properties.size(); i++) {
            StatePropertyKey<?> key = keys[i];
            key.write(packetBuffer, state.get(i));
        }
    }

    public BlockState load(MapType entriesData) {
        Object[] properties = new Object[keys.length];

        for (String key : entriesData.keys()) {
            DataType<?> dataType = entriesData.get(key);

            StatePropertyKey<?> propertyKey = propertyByName.get(key);
            if (propertyKey != null) {
                int idx = ArrayUtils.indexOf(keys, propertyKey);
                switch (dataType) {
                    case IntType intType -> {
                        int value = intType.getValue();
                        properties[idx] = value;
                    }
                    case BooleanType booleanType -> {
                        boolean value = booleanType.getValue();
                        properties[idx] = value;
                    }
                    case StringType type -> {
                        String value = type.getValue();
                        properties[idx] = value;
                    }
                    case ByteType type -> {
                        Byte value = type.getValue();

                        for (Object e : propertyKey.type.getEnumConstants()) {
                            if (((Enum)e).ordinal() == value) {
                                properties[idx] = e;
                                break;
                            }
                        }
                        properties[idx] = defaults[idx];
                    }
                    case null, default ->
                            throw new IllegalArgumentException("Unsupported property data type: " + (dataType == null ? "null" : dataType.getClass().getSimpleName()));
                }
            }
        }

        return new BlockState(block, keys, properties);
    }

    public void save(BlockState state, MapType entriesData) {
        for (int i = 0; i < keys.length; i++) {
            StatePropertyKey<?> key = keys[i];
            Object value = state.get(i);

            if (value!= null) {
                DataType<?> dataType = switch (value) {
                    case Integer ignored -> new IntType((int) value);
                    case Boolean ignored -> new BooleanType((boolean) value);
                    case String string -> new StringType(string);
                    case Enum<?> enum_ -> new ByteType(enum_.ordinal());
                    default ->
                            throw new IllegalArgumentException("Unsupported property value type: " + value.getClass().getName());
                };

                entriesData.put(key.name, dataType);
            } else {
                throw new NullPointerException("Invalid state property value! Cannot be null");
            }
        }
    }

    public <T> StatePropertyKey<?> byName(@NotNull String name) {
        return propertyByName.get(name);
    }
}
