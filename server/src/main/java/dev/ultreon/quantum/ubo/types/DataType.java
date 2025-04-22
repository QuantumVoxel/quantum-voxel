package dev.ultreon.quantum.ubo.types;

import dev.ultreon.quantum.ubo.util.DataTypeVisitor;

import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Function;

public interface DataType<T> {
    T getValue();

    void setValue(T obj);

    int id();

    void write(DataOutput output) throws IOException;

    boolean equals(Object other);

    int hashCode();

    DataType<T> copy();

    String writeUso();

    default <R> R accept(DataTypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    default <R> R map(Function<DataType<T>, R> mapper) {
        return mapper.apply(this);
    }

    default <R extends DataType<?>> R cast(Class<R> type, R def) {
        if (this.equals(def)) {
            return def;
        }

        if (type.isInstance(this)) {
            return type.cast(this);
        }

        return def;
    }
}
