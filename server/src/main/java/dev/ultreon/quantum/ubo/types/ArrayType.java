package dev.ultreon.quantum.ubo.types;

public interface ArrayType<T, B> extends DataType<T>, Iterable<B> {
    int size();

    B get(int index);

    void set(int index, B value);
}
