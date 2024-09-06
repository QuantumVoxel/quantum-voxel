package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.OS;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Utils {
    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static <R, T> Consumer<R> with(T object, BiConsumer<R, T> consumer) {
        return v -> consumer.accept(v, object);
    }

    public static <T> Runnable with(T object, Consumer<T> consumer) {
        return () -> consumer.accept(object);
    }

    public static int normalizeToInt(byte b) {
        return b < 0 ? (int)b + 128 : b;
    }

    public static Vec3d toCoreLibs(Vector3 vector) {
        return new Vec3d(vector.x, vector.y, vector.z);
    }

    public static boolean openURL(@Nullable String url) {
        if (url == null || !url.startsWith("https://")) return false;

        System.out.println("Opening " + url);

        try {
            if (OS.isWindows()) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (OS.isMac()) {
                Runtime.getRuntime().exec("open " + url);
            } else if (OS.isLinux()) {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
            return true;
        } catch (Exception e) {
            QuantumClient.LOGGER.error("Failed to open URL", e);
            return false;
        }
    }
}
