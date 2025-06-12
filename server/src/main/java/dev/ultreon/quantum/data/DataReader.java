package dev.ultreon.quantum.data;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@ApiStatus.Experimental
public interface DataReader<T> {

    byte readByte(T data);
    short readShort(T data);
    int readInt(T data);
    long readLong(T data);
    float readFloat(T data);
    double readDouble(T data);
    String readString(T data);
    boolean readBoolean(T data);
    char readChar(T data);
    UUID readUuid(T data);

    void readEnd(T data);

    @Nullable Object read(T data);

    DataResult<T> readMapEntry(String key, Map<String, T> map);

    void iterate(T data, Consumer<T> consumer);

    boolean isMap(T data);
    boolean isList(T data);

    boolean isNumber(T data);
    boolean isBoolean(T data);
    boolean isString(T data);
    boolean isChar(T data);
    boolean isUuid(T data);
    boolean isNull(T data);

    Class<?> getType(T data);
}
