package dev.ultreon.quantum.ubo;

import dev.ultreon.ubo.types.DataType;

/**
 * Interface for data writers.
 *
 * @param <T>
 */
@FunctionalInterface
public interface DataWriter<T extends DataType<?>> {
    /**
     * Saves this object to a UBO object.
     *
     * @return the UBO object
     */
    T save();
}
