package dev.ultreon.quantum.events.api;

import org.jetbrains.annotations.Nullable;

public final class ValueEventResult<T> {
    private final boolean interrupted;
    private final boolean canceled;
    private final T value;

    public ValueEventResult(boolean interrupt, boolean canceled, @Nullable T value) {
        this.interrupted = interrupt;
        this.canceled = canceled;
        this.value = value;
    }

    public boolean isInterrupted() {
        return this.interrupted;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    @Nullable
    public T getValue() {
        return this.value;
    }

    public static <T> ValueEventResult<T> pass() {
        return new ValueEventResult<>(false, false, null);
    }

    public static <T> ValueEventResult<T> interruptCancel(T value) {        return new ValueEventResult<>(true, true, value);
    }

    public static <T> ValueEventResult<T> interrupt(T value) {
        return new ValueEventResult<>(true, false, value);
    }

    public static <T> ValueEventResult<T> interrupt(boolean cancel, T value) {
        return new ValueEventResult<>(true, cancel, value);
    }
}
