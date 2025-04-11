package dev.ultreon.quantum.data;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApiStatus.Experimental
public interface DataWriter<T> {
    <D> void write(D data);

    void writeByte(byte value);
    void writeShort(short value);
    void writeInt(int value);
    void writeLong(long value);
    void writeFloat(float value);
    void writeDouble(double value);
    void writeChar(char value);
    void writeBoolean(boolean value);

    void writeString(String value);
    void writeUuid(UUID value);

    void writeList(List<T> value);
    void writeMap(Map<?, ?> value);

    T writeEnd();

    T writePop();
}
