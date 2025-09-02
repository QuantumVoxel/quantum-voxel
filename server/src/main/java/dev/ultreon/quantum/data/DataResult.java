package dev.ultreon.quantum.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class DataResult<T> {
    private final @Nullable T value;
    private final boolean success;

    public DataResult(@Nullable T value, boolean success) {
        this.value = value;
        this.success = success;
    }

    public boolean isSuccessful() {
        return success;
    }

    public @NotNull T getValue() {
        if (!success) throw new IllegalStateException("Cannot get value from failed result");
        if (value == null) throw new IllegalStateException("Cannot get value from null result");
        return value;
    }

    public @Nullable T getPartialValue() {
        return value;
    }

    public <R>  DataResult<R> map(Function<T, R> mapper) {
        if (success && value != null) {
            return new DataResult<>(mapper.apply(value), true);
        } else {
            return new DataResult<>(null, false);
        }
    }
}
