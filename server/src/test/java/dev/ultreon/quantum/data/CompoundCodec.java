package dev.ultreon.quantum.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CompoundCodec<T> implements Codec<T> {
    private final Map<String, FieldCodec<T, ?>> codecByField = new HashMap<>();
    private final Map<String, Function<T, ?>> getterByField = new HashMap<>();
    private final Function<Map<String, Object>, T> map;

    private CompoundCodec(Function<Map<String, Object>, T> map) {
        this.map = map;
    }

    public T map(Map<String, Object> map) {
        return this.map.apply(map);
    }

    public static <T> Builder<T> builder(Function<Map<String, Object>, T> map) {
        return new Builder<>(new CompoundCodec<>(map));
    }

    @Override
    public <D> DataResult<D> write(DataWriter<D> writer, T data) {
        D map = writer.createMap();
        for (Map.Entry<String, FieldCodec<T, ?>> entry : this.codecByField.entrySet()) {
//            writer.writeMapEntry(entry.getKey(), this.getterByField.get(entry.getKey()).apply(data));
        }
        return new DataResult<>(map, true);
    }

    @Override
    public <D> DataResult<T> read(DataReader<D> reader, D data) {
//        if (data instanceof Map) {
//            Map<String, D> map = (Map<String, D>) data;
//            reader.iterate(data, d -> {
//                String key = reader.getString(d);
//                FieldCodec<T, ?> codec = this.codecByField.get(key);
//                if (codec == null) {
//                    return new DataResult<>(null, false);
//                }
//                return codec.codec.read(reader, d);
//            });
//        }

        throw new UnsupportedOperationException("TODO");
    }

    public static class Builder<T> {
        private final CompoundCodec<T> instance;

        private Builder(CompoundCodec<T> instance) {
            this.instance = instance;
        }

        public <R> FieldCodec<T, R> add(MapCodec<R> codec) {
            return new FieldCodec<>(this, codec);
        }

        public CompoundCodec<T> build() {
            return instance;
        }
    }

    public static class FieldCodec<T, R> {
        private final Builder<T> builder;
        private final MapCodec<R> codec;
        private Function<T, R> getter;

        private FieldCodec(Builder<T> builder, MapCodec<R> codec) {
            this.builder = builder;
            this.codec = codec;
        }

        public FieldCodec<T, R> getBy(Function<T, R> getter) {
            this.getter = getter;
            return this;
        }

        public Builder<T> build() {
            builder.instance.codecByField.put(codec.key(), this);
            builder.instance.getterByField.put(codec.key(), getter);
            return builder;
        }
    }
}
