package dev.ultreon.quantum.ubo.util;

import dev.ultreon.quantum.ubo.DataIo;
import dev.ultreon.quantum.ubo.types.DataType;

import java.io.IOException;

public interface StringVisitor<T> {
    StringVisitor<DataType<?>> FROM_USO = DataIo::fromUso;

    T visit(String value) throws IOException;
}
