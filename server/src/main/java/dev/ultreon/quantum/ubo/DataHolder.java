package dev.ultreon.quantum.ubo;

import dev.ultreon.ubo.types.DataType;

/**
 * Interface for data holders.
 *
 * @param <T> The type of the UBO data.
 */
public interface DataHolder<T extends DataType<?>> extends DataWriter<T>, DataReader<T> {

}
