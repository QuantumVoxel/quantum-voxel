package dev.ultreon.quantum.network;

public interface TriConsumer<T, T1, T2> {
    void accept(T t, T1 t1, T2 t2);
}
