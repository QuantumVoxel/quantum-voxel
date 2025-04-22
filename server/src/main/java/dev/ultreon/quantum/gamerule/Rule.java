package dev.ultreon.quantum.gamerule;

import dev.ultreon.quantum.api.neocommand.CommandExecuteException;

public interface Rule<T> {
    String getKey();
    void setValue(T value);
    T getValue();
    T getDefault();
    String getStringValue();
    void setStringValue(String value) throws CommandExecuteException;

    default void reset() {
        this.setValue(this.getDefault());
    }
}