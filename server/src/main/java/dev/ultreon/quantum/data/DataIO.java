package dev.ultreon.quantum.data;

import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

@ApiStatus.Experimental
public interface DataIO<T> {
    default T createByte(byte value) {
        return createNumeric(value);
    }

    default T createShort(short value) {
        return createNumeric(value);
    }

    default T createInt(int value) {
        return createNumeric(value);
    }

    default T createLong(long value) {
        return createNumeric(value);
    }

    default T createFloat(float value) {
        return createNumeric(value);
    }

    default T createDouble(double value) {
        return createNumeric(value);
    }

    T createChar(char value);

    T createString(String value);

    T createUuid(UUID value);

    T addToList(T list, T value);

    T addToMap(T map, String key, T value);

    void set(T map, String key, T value);

    void add(T list, T value);

    T createNumeric(Number value);

    T createBoolean(boolean value);

    T createList();

    T createMap();
}
