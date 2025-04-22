package dev.ultreon.quantum.ubo;

import dev.ultreon.quantum.ubo.types.DataType;

/**
 * Interface for data writers.
 *
 * @param <T>
 */
@FunctionalInterface
public interface UboWriter<T extends DataType<?>> {
    /**
     * Saves this object to a UBO object.
     *
     * @return the UBO object
     */
    T save();
}
