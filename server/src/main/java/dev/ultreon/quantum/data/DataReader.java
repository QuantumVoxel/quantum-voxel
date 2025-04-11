package dev.ultreon.quantum.data;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApiStatus.Experimental
public interface DataReader<T> {

    byte readByte();
    short readShort();
    int readInt();
    long readLong();
    float readFloat();
    double readDouble();
    String readString();
    boolean readBoolean();
    char readChar();
    UUID readUuid();

    void startReadList();
    List<T> endReadList();

    void startReadMap();
    Map<?, ?> endReadMap();

    void readEnd();

    void readPop();
}
