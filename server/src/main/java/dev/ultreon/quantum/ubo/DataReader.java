package dev.ultreon.quantum.ubo;

import java.io.DataInput;
import java.io.IOException;

@FunctionalInterface
public interface DataReader<T extends dev.ultreon.quantum.ubo.types.DataType<?>> {
    T read(DataInput input) throws IOException;
}
