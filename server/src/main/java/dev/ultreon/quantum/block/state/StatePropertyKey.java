package dev.ultreon.quantum.block.state;

import dev.ultreon.quantum.network.PacketIO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public abstract class StatePropertyKey<T> {
    private static final Map<UUID, Class<?>> uuidMap = new IdentityHashMap<>();

    final String name;
    private final T[] possibleValues;
    final Class<T> type;

    public StatePropertyKey(String name, T[] possibleValues, Class<T> type) {
        this.name = name;
        this.possibleValues = possibleValues;
        this.type = type;
        if (possibleValues.length < 2) throw new IllegalArgumentException("Property must have at least two possible values");
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> byUuid(UUID stateId) {
        return (Class<T>) uuidMap.computeIfAbsent(stateId, uuid -> {
            try {
                return Class.forName(uuid.toString()).asSubclass(Enum.class);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Enum class not found for UUID " + uuid, e);
            }
        });
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
}
