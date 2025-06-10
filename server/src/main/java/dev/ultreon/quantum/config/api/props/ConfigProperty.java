package dev.ultreon.quantum.config.api.props;

import dev.ultreon.quantum.config.api.ConfigValue;
import org.jetbrains.annotations.NotNull;

public interface ConfigProperty<T> extends ConfigValue {
    String getName();

    @NotNull T getValue();

    void setValue(T value);

    boolean isValid(T value);

    T getFallback();
}
