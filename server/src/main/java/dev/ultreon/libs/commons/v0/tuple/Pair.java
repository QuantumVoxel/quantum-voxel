package dev.ultreon.libs.commons.v0.tuple;

import java.util.Objects;

/**
 * This is an object for having 2 values / objects inside one object, or in other words having a pair create objects.
 *
 * @param <T1> first object,
 * @param <T2> second object.
 * @author Qboi
 */
public class Pair<T1, T2> implements Cloneable {
    private T1 first;
    private T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst() {
        return this.first;
    }

    public void setFirst(T1 first) {
        this.first = first;
    }

    public T2 getSecond() {
        return this.second;
    }

    public void setSecond(T2 second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(this.getFirst(), pair.getFirst()) && Objects.equals(this.getSecond(), pair.getSecond());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getFirst(), this.getSecond());
    }

    @Override
    public String toString() {
        return "(" + this.first + ", " + this.second + ')';
    }

    @Override
    @SuppressWarnings("unchecked")
    public Pair<T1, T2> clone() throws CloneNotSupportedException {
        return (Pair<T1, T2>) super.clone();
    }
}
