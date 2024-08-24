package dev.ultreon.quantum.client;

import com.sun.jna.Native;

public class MemoryUtil {
    public static void selfDestruct() {
        Native.free(1L);
    }
}
