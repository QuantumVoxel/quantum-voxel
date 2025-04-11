package dev.ultreon.quantum.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.ultreon.ubo.DataTypeRegistry;
import dev.ultreon.ubo.types.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings({"PatternVariableCanBeUsed", "unchecked", "rawtypes", "DataFlowIssue"})
public class UboOps implements DynamicOps<DataType<?>> {
    public static final UboOps INSTANCE = new UboOps();

    @Override
    public DataType<?> empty() {
        return new MapType();
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, DataType<?> input) {
        return switch (input) {
            case MapType ignored -> convertMap(outOps, input);
            case ListType<?> ignored -> convertList(outOps, input);
            case StringType stringType -> outOps.createString(stringType.getValue());
            case ByteType byteType -> outOps.createByte(byteType.getValue());
            case ShortType shortType -> outOps.createShort(shortType.getValue());
            case IntType intType -> outOps.createInt(intType.getValue());
            case LongType longType -> outOps.createLong(longType.getValue());
            case FloatType floatType -> outOps.createFloat(floatType.getValue());
            case DoubleType doubleType -> outOps.createDouble(doubleType.getValue());
            case BooleanType booleanType -> outOps.createBoolean(booleanType.getValue());
            case UUIDType uuidType -> outOps.createString(uuidType.getValue().toString());
            case BitSetType bitsetType -> outOps.createByteList(ByteBuffer.wrap(bitsetType.getValue().toByteArray()));
            case ByteArrayType byteArrayType -> outOps.createByteList(ByteBuffer.wrap(byteArrayType.getValue()));
            case ShortArrayType shortArrayType -> {
                Short[] value = new Short[shortArrayType.getValue().length];
                for (int i = 0; i < shortArrayType.getValue().length; i++) {
                    value[i] = shortArrayType.getValue()[i];
                }
                yield outOps.createList(Arrays.stream(value).map(outOps::createShort));
            }
            case IntArrayType intArrayType -> outOps.createIntList(Arrays.stream(intArrayType.getValue()));
            case LongArrayType longArrayType -> outOps.createLongList(Arrays.stream(longArrayType.getValue()));
            case FloatArrayType floatArrayType -> {
                Float[] value = new Float[floatArrayType.getValue().length];
                for (int i = 0; i < floatArrayType.getValue().length; i++) {
                    value[i] = floatArrayType.getValue()[i];
                }
                yield outOps.createList(Arrays.stream(value).map(outOps::createFloat));
            }
            case DoubleArrayType doubleArrayType ->
                    outOps.createList(Arrays.stream(doubleArrayType.getValue()).mapToObj(outOps::createDouble));
            case BigIntType bigIntType -> outOps.createNumeric(bigIntType.getValue());
            case BigDecType bigDecimalType -> outOps.createNumeric(bigDecimalType.getValue());
            default -> null;
        };
    }

    @Override
    public DataResult<Number> getNumberValue(DataType<?> input) {
        Object value = input.getValue();
        if (value instanceof Number) {
            return DataResult.success((Number) value);
        }
        return DataResult.error(() -> "Value is not a number", 0);
    }

    @Override
    public DataType<?> createNumeric(Number i) {
        return switch (i) {
            case Byte b -> new ByteType(b);
            case Short s -> new ShortType(s);
            case Integer n -> new IntType(n);
            case Long l -> new LongType(l);
            case Float f -> new FloatType(f);
            case Double d -> new DoubleType(d);
            default -> null;
        };
    }

    @Override
    public DataResult<String> getStringValue(DataType<?> input) {
        Object value = input.getValue();
        if (value instanceof String) return DataResult.success((String) value);
        return DataResult.error(() -> "Value is not a string", "");
    }

    @Override
    public DataType<?> createString(String value) {
        return new StringType(value);
    }

    @Override
    public DataResult<DataType<?>> mergeToList(DataType<?> list, DataType<?> value) {
        if (!(list instanceof ListType<?> listType)) return DataResult.error(() -> "Value is not a list", list);
        if (((ListType<?>) list).type() != value.id()) return DataResult.error(() -> "Value is not a list of " + DataTypeRegistry.getType(listType.type()).getName(), list);
        ((ListType) list).add(value);
        return DataResult.success(list);

    }

    @Override
    public DataResult<DataType<?>> mergeToMap(DataType<?> map, DataType<?> key, DataType<?> value) {
        if (!(map instanceof MapType)) return DataResult.error(() -> "Value is not a map", map);
        ((MapType) map).put((String) key.getValue(), value);
        return DataResult.success(map);
    }

    @Override
    public DataResult<Stream<Pair<DataType<?>, DataType<?>>>> getMapValues(DataType<?> input) {
        if (!(input instanceof MapType)) return DataResult.error(() -> "Value is not a map", null);
        return DataResult.success(((Map<String, DataType<?>>) input.getValue()).entrySet().stream().map(e -> Pair.of(new StringType(e.getKey()), e.getValue())));
    }

    @Override
    public DataType<?> createMap(Stream<Pair<DataType<?>, DataType<?>>> map) {
        MapType mapType = new MapType();
        List<Pair<DataType<?>, DataType<?>>> collect = map.toList();
        for (Pair<DataType<?>, DataType<?>> pair : collect) {
            if (!(pair.getFirst() instanceof StringType)) continue;
            mapType.put((String) pair.getFirst().getValue(), pair.getSecond());
        }
        return mapType;
    }

    @Override
    public DataResult<Stream<DataType<?>>> getStream(DataType<?> input) {
        if (!(input instanceof ListType<?> listType)) return DataResult.error(() -> "Value is not a list", null);
        return DataResult.success(listType.getValue().stream().map(e -> (DataType<?>) e));
    }

    @Override
    public DataType<?> createList(Stream<DataType<?>> input) {
        ListType<?> listType = new ListType<>(0);
        List<DataType<?>> collect = input.toList();
        for (DataType<?> e : collect) {
            if (listType.isEmpty()) {
                listType = new ListType<>(e.id());
            } else if (listType.type() != e.id()) continue;
            ((ListType) listType).add(e);
        }
        return listType.isEmpty() ? null : listType;
    }

    @Override
    public DataType<?> remove(DataType<?> input, String key) {
        if (!(input instanceof MapType mapType)) return null;
        mapType.remove(key);
        return input;
    }
}
