package dev.ultreon.quantum.api.commands.selector;

import dev.ultreon.quantum.api.commands.error.CommandError;
import dev.ultreon.quantum.api.commands.error.InvalidSelectorError;
import dev.ultreon.quantum.api.commands.error.SelectorTooSmallError;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BaseSelector<T> {
    protected SelectorKey key;
    protected String stringValue;
    protected CommandError error;
    protected Result<T> result;

    public BaseSelector(Parsed parsed) {
        this.error = parsed.error();
        this.key = parsed.key();
        this.stringValue = parsed.value();
    }

    public BaseSelector(String text) {
        this(BaseSelector.parseSelector(text));
    }

    public SelectorKey getKey() {
        return this.key;
    }

    protected abstract Result<T> calculateData();

    public String getStringValue() {
        return this.stringValue;
    }

    public @Nullable T getValue() {
        return this.result.value();
    }

    public boolean hasError() {
        return this.result.hasError();
    }

    public CommandError getError(){
        return this.result == null ? null : this.result.error();
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
            var that = (Result) obj;
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

    public static final class Parsed {
        private final SelectorKey key;
        private final String value;
        private final CommandError error;

        public Parsed(SelectorKey key, String value, CommandError error) {
            this.key = key;
            this.value = value;
            this.error = error;
        }

            public boolean hasError() {
                return this.error != null;
            }

        @Override
        public String toString() {
            return this.key.toString() + this.value;
        }

        public SelectorKey key() {
            return key;
        }

        public String value() {
            return value;
        }

        public CommandError error() {
            return error;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Parsed) obj;
            return Objects.equals(this.key, that.key) &&
                   Objects.equals(this.value, that.value) &&
                   Objects.equals(this.error, that.error);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value, error);
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