package dev.ultreon.quantum.data;

import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.server.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("DataFlowIssue")
public final class JsonOps implements DataOps<JsonValue> {
    public static final JsonOps INSTANCE = new JsonOps();

    private JsonOps() {

    }

    @Override
    public byte readByte(JsonValue data) {
        return data.isNumber() ? data.asByte() : 0;
    }

    @Override
    public short readShort(JsonValue data) {
        return data.isNumber() ? data.asShort() : 0;
    }

    @Override
    public int readInt(JsonValue data) {
        return data.isNumber() ? data.asInt() : 0;
    }

    @Override
    public long readLong(JsonValue data) {
        return data.isNumber() ? data.asLong() : 0;
    }

    @Override
    public float readFloat(JsonValue data) {
        return data.isNumber() ? data.asFloat() : 0;
    }

    @Override
    public double readDouble(JsonValue data) {
        return data.isNumber() ? data.asDouble() : 0;
    }

    @Override
    public String readString(JsonValue data) {
        return data.isString() ? data.asString() : "";
    }

    @Override
    public boolean readBoolean(JsonValue data) {
        return data.isBoolean() && data.asBoolean();
    }

    @Override
    public char readChar(JsonValue data) {
        return data.isString() ? data.asString().charAt(0) : '\0';
    }

    @Override
    public UUID readUuid(JsonValue data) {
        if (!data.isString()) {
            return Utils.ZEROED_UUID;
        }
        
        try {
            return UUID.fromString(data.asString());
        } catch (IllegalArgumentException e) {
            throw new CodecException("Invalid UUID: " + data.trace(), e);
        }
    }

    @Override
    public void readEnd(JsonValue data) {

    }

    @Override
    public @Nullable Object read(JsonValue data) {
        if (data == null) {
            return null;
        }
        
        if (data.isNull()) {
            return null;
        }
        
        if (data.isBoolean()) {
            return readBoolean(data);
        } else if (data.isNumber()) {
            return (readNumber(data));
        } else if (data.isString()) {
            return readString(data);
        } else {
            throw new CodecException("Unknown type: " + data.trace());
        }
    }

    private @Nullable Number readNumber(JsonValue data) {
        JsonValue next = data.next;
        long aLong = next.asLong();
        double aDouble = next.asDouble();

        return aLong == aDouble ? (int) aLong : aDouble;
    }

    @Override
    public DataResult<JsonValue> readMapEntry(String key, Map<String, JsonValue> map) {
        JsonValue v = map.get(key);
        if (v == null) {
            return new DataResult<>(null, false);
        }

        return new DataResult<>(v, true);
    }

    @Override
    public boolean isMap(JsonValue data) {
        return data.isObject();
    }

    @Override
    public boolean isList(JsonValue data) {
        return data.isArray();
    }

    @Override
    public boolean isNumber(JsonValue data) {
        return data.isNumber();
    }

    @Override
    public boolean isBoolean(JsonValue data) {
        return data.isBoolean();
    }

    @Override
    public boolean isString(JsonValue data) {
        return data.isString();
    }

    @Override
    public boolean isChar(JsonValue data) {
        return data.isString() && data.asString().length() == 1;
    }

    @Override
    public boolean isUuid(JsonValue data) {
        return data.isString() && data.asString().matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    }

    @Override
    public boolean isNull(@Nullable JsonValue data) {
        return data == null || data.isNull();
    }

    @Override
    public Class<?> getType(JsonValue data) {
        if (data.isNull()) {
            return null;
        }

        if (data.isBoolean()) {
            return boolean.class;
        } else if (data.isNumber()) {
            return Number.class;
        } else if (data.isString()) {
            return String.class;
        } else if (data.isObject()) {
            return Map.class;
        } else if (data.isArray()) {
            return List.class;
        } else {
            throw new CodecException("Unknown type: " + data.trace());
        }
    }

    @Override
    public void iterate(JsonValue data, Consumer<JsonValue> consumer) {
        if (data.isArray()) {
            for (JsonValue v : data) {
                consumer.accept(v);
            }
        } else if (data.isObject()) {
            for (JsonValue v : data) {
                consumer.accept(v);
            }
        } else {
            throw new CodecException("Unknown type: " + data.trace());
        }
    }

    @Override
    public <D> JsonValue write(D data) {
        if (data instanceof JsonValue) {
            return (JsonValue) data;
        }

        if (data instanceof Boolean) {
            return new JsonValue((Boolean) data);
        } else if (data instanceof Number) {
            return new JsonValue(data instanceof Long ? ((Number) data).longValue() : ((Number) data).doubleValue());
        } else if (data instanceof String) {
            return new JsonValue((String) data);
        } else if (data instanceof UUID) {
            return new JsonValue(data.toString());
        } else if (data instanceof Map) {
            JsonValue map = createMap();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) data).entrySet()) {
                writeMapEntry(map, entry.getKey().toString(), write(entry.getValue()));
            }
            return map;
        } else if (data instanceof List) {
            JsonValue list = createList();
            for (Object o : (List<?>) data) {
                writeListItem(list, write(o));
            }
            return list;
        } else {
            throw new CodecException("Unknown type: " + data.getClass().getName());
        }
    }

    @Override
    public JsonValue writeByte(byte value) {
        return new JsonValue(value);
    }

    @Override
    public JsonValue writeShort(short value) {
        return new JsonValue(value);
    }

    @Override
    public JsonValue writeInt(int value) {
        return new JsonValue(value);
    }

    @Override
    public JsonValue writeLong(long value) {
        return new JsonValue(value);
    }

    @Override
    public JsonValue writeFloat(float value) {
        return new JsonValue(value);
    }

    @Override
    public JsonValue writeDouble(double value) {
        return new JsonValue(value);
    }

    @Override
    public JsonValue writeChar(char value) {
        return new JsonValue(String.valueOf(value));
    }

    @Override
    public JsonValue writeBoolean(boolean value) {
        return new JsonValue(value);
    }

    @Override
    public JsonValue writeString(String value) {
        return new JsonValue(value);
    }

    @Override
    public JsonValue writeUuid(UUID value) {
        return new JsonValue(value.toString());
    }

    @Override
    public @Nullable JsonValue unit() {
        return new JsonValue(JsonValue.ValueType.nullValue);
    }

    @Override
    public void writeMapEntry(JsonValue map, String key, JsonValue value) {
        JsonValue v = this.write(value);
        if (!map.isObject()) throw new CodecException("Parent is not an object");
        map.addChild(key, v);
    }

    @Override
    public void writeListItem(JsonValue list, JsonValue value) {
        JsonValue v = this.write(value);
        if (!list.isArray()) throw new CodecException("Parent is not an array");
        list.addChild(v);
    }

    @Override
    public JsonValue createMap() {
        return new JsonValue(JsonValue.ValueType.object);
    }

    @Override
    public JsonValue createList() {
        return new JsonValue(JsonValue.ValueType.array);
    }
}
