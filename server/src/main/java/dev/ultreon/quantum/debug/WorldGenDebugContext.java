package dev.ultreon.quantum.debug;

public class WorldGenDebugContext {
    private static final ThreadLocal<Boolean> active = ThreadLocal.withInitial(() -> false);

    public static boolean isActive() {
        return active.get() || DebugFlags.WORLD_GEN.isEnabled();
    }

    public static void withinContext(Runnable runnable) {
        active.set(true);
        runnable.run();
        active.set(false);
    }
}
