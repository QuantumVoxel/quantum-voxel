package dev.ultreon.quantum.config.api.props;

import dev.ultreon.quantum.config.api.ConfigValue;

public interface ConfigProperty<T> extends ConfigValue {
    String getName();

    T getValue();

    void setValue(T value);

    boolean isValid(T value);

    T getFallback();
}
