package dev.ultreon.quantum.server.util;

import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Utils {
    public static final UUID ZEROED_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static ChunkVec ChunkVecFromBlockCoords(Vec3d pos) {
        return new BlockVec((int)pos.x, (int)pos.y, (int)pos.z).chunk();
    }

    public static int normalizeToInt(byte b) {
        return b < 0 ? (int)b + 128 : b;
    }

    public static String reprChar(char c) {
        if (c == '\r') return "'\\r'";
        if (c == '\n') return "'\\n'";

        return c == '\t' ? "'\\t'" : "'" + c + "'";
    }
}
