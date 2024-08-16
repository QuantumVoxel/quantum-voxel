package dev.ultreon.quantum.debug;

import java.util.Objects;

@SuppressWarnings("FieldMayBeFinal")
public final class DebugFlag {
    private boolean enabled;

    public DebugFlag(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean enabled() {
        return enabled && DebugFlags.IS_RUNNING_IN_DEBUG;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DebugFlag) obj;
        return this.enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled);
    }

    @Override
    public String toString() {
        return "DebugFlag[" +
               "enabled=" + enabled + ']';
    }

}
