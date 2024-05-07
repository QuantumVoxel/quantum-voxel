package dev.ultreon.quantum.ubo;

import dev.ultreon.ubo.types.DataType;

/**
 * Interface for data readers.
 *
 * @param <T>
 */
public interface DataReader<T extends DataType<?>> {
    /**
     * Loads this object from a UBO object.
     *
     * @param data the UBO object
     */
    void load(T data);
}
