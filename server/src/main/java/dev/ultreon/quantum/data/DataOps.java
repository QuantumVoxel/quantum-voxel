package dev.ultreon.quantum.data;

import org.jetbrains.annotations.ApiStatus;

import java.util.BitSet;
import java.util.UUID;

@ApiStatus.Experimental
public interface DataOps<T extends DataIO> extends DataReader<T>, DataWriter<T> {

}
