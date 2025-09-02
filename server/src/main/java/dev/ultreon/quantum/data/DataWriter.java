package dev.ultreon.quantum.data;

import com.badlogic.gdx.utils.JsonValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApiStatus.Experimental
public interface DataWriter<T> {
    <D> T write(D data);

    T writeByte(byte value);
    T writeShort(short value);
    T writeInt(int value);
    T writeLong(long value);
    T writeFloat(float value);
    T writeDouble(double value);
    T writeChar(char value);
    T writeBoolean(boolean value);

    T writeString(String value);
    T writeUuid(UUID value);

    T unit();

    void writeMapEntry(T map, String key, T value);

    void writeListItem(T list, T value);

    T createMap();
    T createList();
}
