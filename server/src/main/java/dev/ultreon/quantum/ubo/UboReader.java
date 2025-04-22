package dev.ultreon.quantum.ubo;

import dev.ultreon.quantum.ubo.types.DataType;

/**
 * Interface for data readers.
 *
 * @param <T>
 */
public interface UboReader<T extends DataType<?>> {
    /**
     * Loads this object from a UBO object.
     *
     * @param data the UBO object
     */
    void load(T data);
}
