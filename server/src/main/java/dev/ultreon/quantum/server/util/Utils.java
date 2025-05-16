package dev.ultreon.quantum.server.util;

import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
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
        return new BlockVec((int)pos.x, (int)pos.y, (int)pos.z, BlockVecSpace.CHUNK).chunk();
    }

    public static int normalizeToInt(byte b) {
        return b < 0 ? (int)b + 128 : b;
    }

    public static Duration parseDuration(String text) {
        try {
            String[] parts = text.split(":");
            long days = 0, hours = 0, minutes = 0, seconds = 0;

            seconds = Utils.getDurationNum(parts[parts.length - 1], true);
            minutes = Utils.getDurationNum(parts[parts.length - 2], parts.length >= 3);
            if (parts.length >= 3) hours = Utils.getDurationNum(parts[parts.length - 3], parts.length >= 4);
            if (parts.length >= 4) days = Utils.getDurationNum(parts[parts.length - 4], false);
            return Duration.ofSeconds(seconds + (minutes * 60) + (hours * 3600) + (days * 86400));
        } catch (NumberFormatException e) {
            throw new TimeFormatException("Invalid number format: " + e, e);
        }
    }

    private static long getDurationNum(String part, boolean trim) {
        if (!trim) {
            return Integer.parseInt(part);
        }
        return 0;
    }

    public static String reprChar(char c) {
        if (c == '\r') return "'\\r'";
        if (c == '\n') return "'\\n'";

        return c == '\t' ? "'\\t'" : "'" + c + "'";
    }
}
