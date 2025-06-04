package dev.ultreon.quantum.world.data;

import com.badlogic.gdx.utils.DataBuffer;
import com.badlogic.gdx.utils.DataInput;
import dev.ultreon.quantum.ubo.types.MapType;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public final class UboObject {
    static MapType fromBytes(byte[] decompress) throws IOException {
        return MapType.read(new DataInput(new ByteArrayInputStream(decompress)));
    }

    static byte[] write(MapType chunk) throws IOException {
        DataBuffer buffer = new DataBuffer();
        chunk.write(buffer);
        return buffer.getBuffer();
    }
}
