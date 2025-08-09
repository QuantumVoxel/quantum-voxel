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
        boolean success = true;
        
        for (Map.Entry<String, FieldCodec<T, ?>> entry : this.codecByField.entrySet()) {
            String key = entry.getKey();
            Function<T, ?> getter = this.getterByField.get(key);
            
            if (getter != null && writer instanceof DataOps) {
                try {
                    Object value = getter.apply(data);
                    if (value != null) {
                        @SuppressWarnings("unchecked")
                        FieldCodec<T, Object> fieldCodec = (FieldCodec<T, Object>) entry.getValue();
                        D encodedValue = fieldCodec.codec.encode((DataOps<D>) writer, value);
                        writer.writeMapEntry(map, key, encodedValue);
                    }
                } catch (Exception e) {
                    success = false;
                }
            }
        }
        
        return new DataResult<>(map, success);
    }

    @Override
    public <D> DataResult<T> read(DataReader<D> reader, D data) {
        if (!reader.isMap(data)) {
            return new DataResult<>(null, false);
        }
        
        Map<String, Object> resultMap = new HashMap<>();
        final boolean[] success = {true};
        
        reader.iterate(data, entry -> {
            if (reader.isString(entry)) {
                String key = reader.readString(entry);
                FieldCodec<T, ?> fieldCodec = this.codecByField.get(key);
                if (fieldCodec != null && reader instanceof DataOps) {
                    try {
                        Object value = fieldCodec.codec.decode((DataOps<D>) reader, entry);
                        resultMap.put(key, value);
                    } catch (Exception e) {
                        success[0] = false;
                    }
                }
            }
        });
        
        if (!success[0]) {
            return new DataResult<>(null, false);
        }
        
        return new DataResult<>(this.map(resultMap), true);
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
