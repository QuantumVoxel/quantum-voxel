package dev.ultreon.quantum.api.commands.selector;

import dev.ultreon.quantum.api.commands.error.CommandError;
import dev.ultreon.quantum.api.commands.error.InvalidSelectorError;
import dev.ultreon.quantum.api.commands.error.SelectorTooSmallError;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BaseSelector<T> {
    protected @Nullable SelectorKey key;
    protected @Nullable String stringValue;
    protected @Nullable CommandError error;
    protected @Nullable Result<T> result;

    public BaseSelector(Parsed parsed) {
        this.error = parsed.error();
        this.key = parsed.key();
        this.stringValue = parsed.value();
    }

    public BaseSelector(String text) {
        this(BaseSelector.parseSelector(text));
    }

    public @Nullable SelectorKey getKey() {
        return this.key;
    }

    protected abstract Result<T> calculateData();

    public @Nullable String getStringValue() {
        return this.stringValue;
    }

    public @Nullable T getValue() {
        if (this.result != null) {
            return this.result.value();
        }
        return null;
    }

    public boolean hasError() {
        if (this.result != null) {
            return this.result.hasError();
        }
        return false;
    }

    public @Nullable CommandError getError() {
        Result<T> res = this.result;
        if (res == null) {
            return null;
        }
        return res == null ? null : res.error();
    }

    public static final class Result<T> {
        private final @Nullable T value;
        private final @Nullable CommandError error;

        public Result(@Nullable T value, @Nullable CommandError error) {
            this.value = value;
            this.error = error;
        }

        public boolean hasError() {
            return this.error != null;
        }

        public @Nullable T value() {
            return value;
        }

        public @Nullable CommandError error() {
            return error;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Result<?>) obj;
            return Objects.equals(this.value, that.value) &&
                    Objects.equals(this.error, that.error);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, error);
        }

        @Override
        public String toString() {
            return "Result[" +
                    "value=" + value + ", " +
                    "error=" + error + ']';
        }

    }

    public record Parsed(@Nullable SelectorKey key, @Nullable String value, @Nullable CommandError error) {
        public boolean hasError() {
            return this.error != null;
        }

        @Override
        public String toString() {
            if (this.key != null) {
                return this.key.toString() + this.value;
            }
            if (this.value != null) {
                return this.value;
            }
            return "<null>";
        }

        @Override
        public @Nullable SelectorKey key() {
            return key;
        }

        @Override
        public @Nullable String value() {
            return value;
        }

        @Override
        public @Nullable CommandError error() {
            return error;
        }

    }

    @Override
    public String toString() {
        return this.key.symbol() + this.stringValue;
    }

    public static Parsed parseSelector(String text) throws IllegalArgumentException {
        if (text.length() <= 1) {
            return new Parsed(null, null, new SelectorTooSmallError(text));
        }
        SelectorKey key = SelectorKey.fromKey(text.charAt(0));
        if (key == null) return new Parsed(null, null, new InvalidSelectorError(text));
        return new Parsed(key, text.substring(1), null);
    }

    public static Parsed parseSelector(String text, Parsed def) {
        if (text.length() <= 1) {
            return null;
        }
        SelectorKey key = SelectorKey.fromKey(text.charAt(0));
        if (key == null) return def;
        return new Parsed(key, text.substring(1), null);
    }

}