package dev.ultreon.quantum.data;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface DataOps<T> extends DataReader<T>, DataWriter<T> {

}
