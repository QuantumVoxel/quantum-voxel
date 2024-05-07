package dev.ultreon.quantum.debug;

public record DebugFlag(boolean enabled) {
    @Override
    public boolean enabled() {
        return enabled && DebugFlags.IS_RUNNING_IN_DEBUG;
    }
}
