package dev.ultreon.quantum.world;

public interface Column<T> extends Iterable<T> {
    T get(int y);
    void set(int y, T value);
    T remove(int y);

    int getFirstY();
    int getLastY();

    int size();
}
