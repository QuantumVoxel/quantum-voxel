package dev.ultreon.quantum.debug;

public class WorldGenDebugContext {
    public static boolean isActive() {
        return false;
    }

    public static void withinContext(Runnable runnable) {
        runnable.run();
    }
}
