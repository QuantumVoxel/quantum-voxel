package dev.ultreon.quantum.data;

public interface MapCodec<T> {
    String key();

    <D> T decode(DataOps<D> ops, D data);
    <D> D encode(DataOps<D> ops, T value);

}
