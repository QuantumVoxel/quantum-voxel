package dev.ultreon.quantum.network;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import dev.ultreon.libs.collections.v0.list.SizedList;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.ChunkVecSpace;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PacketCodec<T> {
    T read(PacketIO packetIO);

    void write(PacketIO packetIO, T data);

    default <R> PacketCodec<R> map(Function<T, R> readMap, Function<R, T> writeMap) {
        return new PacketCodec<>() {
            @Override
            public R read(PacketIO packetIO) {
                return readMap.apply(PacketCodec.this.read(packetIO));
            }

            @Override
            public void write(PacketIO packetIO, R data) {
                PacketCodec.this.write(packetIO, writeMap.apply(data));
            }
        };
    }

    static <T> PacketCodec<T> unit(Supplier<T> supplier) {
        return of(packetIO -> supplier.get(), (packetIO, data) -> {
        });
    }

    static <T> PacketCodec<T> unit(T constant) {
        return of(packetIO -> constant, (packetIO, data) -> {
        });
    }

    static <T> PacketCodec<T> of(Reader<T> reader, Writer<T> writer) {
        return new PacketCodec<>() {
            @Override
            public T read(PacketIO packetIO) {
                return reader.read(packetIO);
            }

            @Override
            public void write(PacketIO packetIO, T data) {
                writer.write(packetIO, data);
            }
        };
    }

    static <F, S> PacketCodec<Pair<F, S>> pair(Reader<F> firstReader, Reader<S> secondReader, Writer<F> firstWriter, Writer<S> secondWriter) {
        return of(packetIO -> new Pair<>(firstReader.read(packetIO), secondReader.read(packetIO)), (packetIO, pair) -> {
            firstWriter.write(packetIO, pair.getFirst());
            secondWriter.write(packetIO, pair.getSecond());
        });
    }

    @FunctionalInterface
    interface Reader<T> {
        T read(PacketIO packetIO);
    }

    @FunctionalInterface
    interface Writer<T> {
        void write(PacketIO packetIO, T data);
    }

    static <T> PacketCodec<T> nothing() {
        return of(packetIO -> null, (packetIO, data) -> {
        });
    }

    static <K, V> PacketCodec<Map<K, V>> mapOf(PacketCodec<K> keyCodec, PacketCodec<V> valueCodec) {
        return of(packetIO -> packetIO.readMap(keyCodec::read, valueCodec::read), (packetIO, map) -> packetIO.writeMap(map, keyCodec::write, valueCodec::write));
    }

    static <K, V> PacketCodec<ObjectMap<K, V>> objectMapOf(PacketCodec<K> keyCodec, PacketCodec<V> valueCodec) {
        return of(packetIO -> packetIO.readObjectMap(keyCodec::read, valueCodec::read), (packetIO, map) -> packetIO.writeObjectMap(map, keyCodec::write, valueCodec::write));
    }

    static <T> PacketCodec<List<T>> listOf(PacketCodec<T> codec) {
        return of(packetIO -> packetIO.readList(codec::read), (packetIO, list) -> packetIO.writeList(list, codec::write));
    }

    static <T> PacketCodec<Array<T>> gdxArrayOf(PacketCodec<T> codec) {
        return PacketCodec.of(packetIO -> {
            int size = packetIO.readMedium();
            Array<T> array = new Array<>(size);

            for (int i = 0; i < size; i++) {
                array.add(codec.read(packetIO));
            }

            return array;
        }, (packetIO, array) -> {
            packetIO.writeMedium(array.size);
            for (int i = 0; i < array.size; i++) {
                codec.write(packetIO, array.get(i));
            }
        });
    }

    PacketCodec<IntArray> GDX_INT_ARRAY = of(packetIO -> {
        int size = packetIO.readMedium();
        IntArray array = new IntArray(size);

        for (int i = 0; i < size; i++) {
            array.add(packetIO.readInt());
        }

        return array;
    }, (packetIO, array) -> {
        packetIO.writeMedium(array.size);
        for (int i = 0; i < array.size; i++) {
            packetIO.writeInt(array.get(i));
        }
    });

    PacketCodec<ShortArray> GDX_SHORT_ARRAY = of(packetIO -> {
        int size = packetIO.readMedium();
        ShortArray array = new ShortArray(size);

        for (int i = 0; i < size; i++) {
            array.add(packetIO.readShort());
        }

        return array;
    }, (packetIO, array) -> {
        packetIO.writeMedium(array.size);
        for (int i = 0; i < array.size; i++) {
            packetIO.writeShort(array.get(i));
        }
    });

    PacketCodec<ByteArray> GDX_BYTE_ARRAY = of(packetIO -> {
        int size = packetIO.readMedium();
        ByteArray array = new ByteArray(size);

        for (int i = 0; i < size; i++) {
            array.add(packetIO.readByte());
        }

        return array;
    }, (packetIO, array) -> {
        packetIO.writeMedium(array.size);
        for (int i = 0; i < array.size; i++) {
            packetIO.writeByte(array.get(i));
        }
    });

    PacketCodec<CharArray> GDX_CHAR_ARRAY = of(packetIO -> {
        int size = packetIO.readMedium();
        CharArray array = new CharArray(size);

        for (int i = 0; i < size; i++) {
            array.add(packetIO.readChar());
        }

        return array;
    }, (packetIO, array) -> {
        packetIO.writeMedium(array.size);
        for (int i = 0; i < array.size; i++) {
            packetIO.writeChar(array.get(i));
        }
    });

    PacketCodec<FloatArray> GDX_FLOAT_ARRAY = of(packetIO -> {
        int size = packetIO.readMedium();
        FloatArray array = new FloatArray(size);

        for (int i = 0; i < size; i++) {
            array.add(packetIO.readFloat());
        }

        return array;
    }, (packetIO, array) -> {
        packetIO.writeMedium(array.size);
        for (int i = 0; i < array.size; i++) {
            packetIO.writeFloat(array.get(i));
        }
    });

    PacketCodec<LongArray> GDX_LONG_ARRAY = of(packetIO -> {
        int size = packetIO.readMedium();
        LongArray array = new LongArray(size);

        for (int i = 0; i < size; i++) {
            array.add(packetIO.readLong());
        }

        return array;
    }, (packetIO, array) -> {
        packetIO.writeMedium(array.size);
        for (int i = 0; i < array.size; i++) {
            packetIO.writeLong(array.get(i));
        }
    });

    static <T> PacketCodec<IntMap<T>> intMapOf(PacketCodec<T> codec) {
        return of(packetIO -> {
            int size = packetIO.readMedium();
            IntMap<T> map = new IntMap<>();

            for (int i = 0; i < size; i++) {
                map.put(packetIO.readInt(), codec.read(packetIO));
            }

            return map;
        }, (packetIO, map) -> {
            packetIO.writeMedium(map.size);
            for (IntMap.Entry<T> entry : map.entries()) {
                packetIO.writeInt(entry.key);
                codec.write(packetIO, entry.value);
            }
        });
    }

    static <T> PacketCodec<LongMap<T>> longMapOf(PacketCodec<T> codec) {
        return of(packetIO -> {
            int size = packetIO.readMedium();
            LongMap<T> map = new LongMap<>();

            for (int i = 0; i < size; i++) {
                map.put(packetIO.readLong(), codec.read(packetIO));
            }

            return map;
        }, (packetIO, map) -> {
            packetIO.writeMedium(map.size);
            for (LongMap.Entry<T> entry : map.entries()) {
                packetIO.writeLong(entry.key);
                codec.write(packetIO, entry.value);
            }
        });
    }

    static <T> PacketCodec<SizedList<T>> sizedListOf(PacketCodec<T> codec) {
        return of(packetIO -> {
            int size = packetIO.readMedium();
            SizedList<T> list = new SizedList<>();

            for (int i = 0; i < size; i++) {
                list.add(packetIO.readDouble(), codec.read(packetIO));
            }

            return list;
        }, (packetIO, list) -> {
            for (int i = 0; i < list.getRanges().length; i++) {
                packetIO.writeDouble(list.getSize(i));
                codec.write(packetIO, list.getValue(i));
            }
        });
    }

    static <T extends Enum<T>> PacketCodec<EnumSet<T>> enumSetOf(Class<T> enumClass) {
        return of(packetIO -> {
            int size = packetIO.readMedium();
            List<T> list = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                list.add(packetIO.readEnum(enumClass));
            }

            return EnumSet.copyOf(list);
        }, (packetIO, set) -> {
            packetIO.writeMedium(set.size());
            for (T value : set) {
                packetIO.writeEnum(value);
            }
        });
    }

    static <T> PacketCodec<Set<T>> setOf(PacketCodec<T> codec) {
        return of(packetIO -> {
            int size = packetIO.readMedium();
            Set<T> set = new HashSet<>();

            for (int i = 0; i < size; i++) {
                set.add(codec.read(packetIO));
            }

            return set;
        }, (packetIO, set) -> {
            packetIO.writeMedium(set.size());
            for (T value : set) {
                codec.write(packetIO, value);
            }
        });
    }

    static <T> PacketCodec<LinkedHashSet<T>> linkedHashSetOf(PacketCodec<T> codec) {
        return of(packetIO -> {
            int size = packetIO.readMedium();
            LinkedHashSet<T> set = new LinkedHashSet<>();

            for (int i = 0; i < size; i++) {
                set.add(codec.read(packetIO));
            }

            return set;
        }, (packetIO, set) -> {
            packetIO.writeMedium(set.size());
            for (T value : set) {
                codec.write(packetIO, value);
            }
        });
    }

    static <T> PacketCodec<Queue<T>> queueOf(PacketCodec<T> codec) {
        return of(packetIO -> {
            int size = packetIO.readMedium();
            Queue<T> queue = new ArrayDeque<>();

            for (int i = 0; i < size; i++) {
                queue.add(codec.read(packetIO));
            }

            return queue;
        }, (packetIO, queue) -> {
            packetIO.writeMedium(queue.size());
            for (T value : queue) {
                codec.write(packetIO, value);
            }
        });
    }

    static <T> PacketCodec<PriorityQueue<T>> priorityQueueOf(PacketCodec<T> codec) {
        return of(packetIO -> {
            int size = packetIO.readMedium();
            PriorityQueue<T> queue = new PriorityQueue<>();

            for (int i = 0; i < size; i++) {
                queue.add(codec.read(packetIO));
            }

            return queue;
        }, (packetIO, queue) -> {
            packetIO.writeMedium(queue.size());
            for (T value : queue) {
                codec.write(packetIO, value);
            }
        });
    }

    static PacketCodec<ListType<?>> uboListOf() {
        return of(packetIO -> {
            int size = packetIO.readMedium();
            return packetIO.readUbo();
        }, (packetIO, list) -> {
            packetIO.writeMedium(list.size());
            packetIO.writeUbo(list);
        });
    }

    PacketCodec<Void> VOID = nothing();
    PacketCodec<Unit> UNIT = unit(Unit.INSTANCE);
    PacketCodec<String> STRING = of(PacketIO::readString, PacketIO::writeString);
    PacketCodec<Byte> BYTE = of(PacketIO::readByte, PacketIO::writeByte);
    PacketCodec<Short> SHORT = of(PacketIO::readShort, PacketIO::writeShort);
    PacketCodec<Integer> INT = of(PacketIO::readInt, PacketIO::writeInt);
    PacketCodec<Long> LONG = of(PacketIO::readLong, PacketIO::writeLong);
    PacketCodec<Float> FLOAT = of(PacketIO::readFloat, PacketIO::writeFloat);
    PacketCodec<Double> DOUBLE = of(PacketIO::readDouble, PacketIO::writeDouble);
    PacketCodec<Boolean> BOOLEAN = of(PacketIO::readBoolean, PacketIO::writeBoolean);
    PacketCodec<Vec2d> VEC2D = unionOf(Vec2d.class)
            .add(Vec2d::getX, DOUBLE)
            .add(Vec2d::getY, DOUBLE)
            .build();
    PacketCodec<BlockVecSpace> BLOCK_VEC_SPACE = of(packetIO -> packetIO.readEnum(BlockVecSpace.WORLD), PacketIO::writeEnum);
    PacketCodec<BlockVec> BLOCK_VEC = unionOf(BlockVec.class)
            .add(BlockVec::getIntX, INT)
            .add(BlockVec::getIntY, INT)
            .add(BlockVec::getIntZ, INT)
            .add(BlockVec::getSpace, BLOCK_VEC_SPACE)
            .build();
    PacketCodec<ChunkVecSpace> CHUNK_VEC_SPACE = of(packetIO -> packetIO.readEnum(ChunkVecSpace.WORLD), PacketIO::writeEnum);
    PacketCodec<ChunkVec> CHUNK_VEC = unionOf(ChunkVec.class)
            .add(ChunkVec::getIntX, INT)
            .add(ChunkVec::getIntY, INT)
            .add(ChunkVec::getIntZ, INT)
            .add(ChunkVec::getSpace, CHUNK_VEC_SPACE)
            .build();

    PacketCodec<Vec2i> VEC2I = unionOf(Vec2i.class)
            .add(Vec2i::getX, INT)
            .add(Vec2i::getY, INT)
            .build();

    PacketCodec<Vec3i> VEC3I = unionOf(Vec3i.class)
            .add(Vec3i::getIntX, INT)
            .add(Vec3i::getIntY, INT)
            .add(Vec3i::getIntZ, INT)
            .build();

    PacketCodec<Vec4i> VEC4I = unionOf(Vec4i.class)
            .add(Vec4i::getX, INT)
            .add(Vec4i::getY, INT)
            .add(Vec4i::getZ, INT)
            .add(Vec4i::getW, INT)
            .build();

    PacketCodec<Vec2f> VEC2F = unionOf(Vec2f.class)
            .add(Vec2f::getX, FLOAT)
            .add(Vec2f::getY, FLOAT)
            .build();

    PacketCodec<Vec3f> VEC3F = unionOf(Vec3f.class)
            .add(Vec3f::getX, FLOAT)
            .add(Vec3f::getY, FLOAT)
            .add(Vec3f::getZ, FLOAT)
            .build();

    PacketCodec<Vec4f> VEC4F = unionOf(Vec4f.class)
            .add(Vec4f::getX, FLOAT)
            .add(Vec4f::getY, FLOAT)
            .add(Vec4f::getZ, FLOAT)
            .add(Vec4f::getW, FLOAT)
            .build();

    PacketCodec<Vec3d> VEC3D = unionOf(Vec3d.class)
            .add(Vec3d::getX, DOUBLE)
            .add(Vec3d::getY, DOUBLE)
            .add(Vec3d::getZ, DOUBLE)
            .build();

    PacketCodec<Vec4d> VEC4D = unionOf(Vec4d.class)
            .add(Vec4d::getX, DOUBLE)
            .add(Vec4d::getY, DOUBLE)
            .add(Vec4d::getZ, DOUBLE)
            .add(Vec4d::getW, DOUBLE)
            .build();

    PacketCodec<Quaternion> QUATERNION = unionOf(Quaternion.class)
            .add(q -> q.x, FLOAT)
            .add(q -> q.y, FLOAT)
            .add(q -> q.z, FLOAT)
            .add(q -> q.w, FLOAT)
            .build();

    PacketCodec<Vector3> VECTOR3 = unionOf(Vector3.class)
            .add(v -> v.x, FLOAT)
            .add(v -> v.y, FLOAT)
            .add(v -> v.z, FLOAT)
            .build();

    PacketCodec<Vector2> VECTOR2 = unionOf(Vector2.class)
            .add(v -> v.x, FLOAT)
            .add(v -> v.y, FLOAT)
            .build();

    PacketCodec<GridPoint2> GRID_POINT2 = unionOf(GridPoint2.class)
            .add(g -> g.x, INT)
            .add(g -> g.y, INT)
            .build();

    PacketCodec<GridPoint3> GRID_POINT3 = unionOf(GridPoint3.class)
            .add(g -> g.x, INT)
            .add(g -> g.y, INT)
            .add(g -> g.z, INT)
            .build();

    PacketCodec<Block> BLOCK = INT.map(Registries.BLOCK::byId, Registries.BLOCK::getRawId);

    PacketCodec<BlockState> BLOCK_STATE = duo(BLOCK, INT).flatten(
            BlockState::getBlock,
            BlockState::getStateId,
            (block, integer) -> block.getDefinition().byId(integer)
    );

    PacketCodec<Item> ITEM = INT.map(Registries.ITEM::byId, Registries.ITEM::getRawId);
    PacketCodec<MapType> UBO_MAP = of(PacketIO::readUbo, PacketIO::writeUbo);

    PacketCodec<ItemStack> ITEM_STACK = unionOf(ItemStack.class)
            .add(ItemStack::getItem, ITEM)
            .add(ItemStack::getCount, INT)
            .add(ItemStack::getData, UBO_MAP)
            .build();

    PacketCodec<NamespaceID> NAMESPACE_ID = unionOf(NamespaceID.class)
            .add(NamespaceID::getDomain, STRING)
            .add(NamespaceID::getPath, STRING)
            .build();

    PacketCodec<UUID> UUID = unionOf(UUID.class)
            .add(java.util.UUID::getMostSignificantBits, LONG)
            .add(java.util.UUID::getLeastSignificantBits, LONG)
            .build();

    PacketCodec<Point> POINT = of(packetIO -> {
        double x = packetIO.readDouble();
        double y = packetIO.readDouble();
        double z = packetIO.readDouble();
        return new Vec(x, y, z);
    }, (packetIO, point) -> {
        packetIO.writeDouble(point.getX());
        packetIO.writeDouble(point.getY());
        packetIO.writeDouble(point.getZ());
    });

    PacketCodec<Vec> VEC = unionOf(Vec.class)
            .add(Vec::getX, DOUBLE)
            .add(Vec::getY, DOUBLE)
            .add(Vec::getZ, DOUBLE)
            .build();

    static <T extends Record> RecordPacketCodecBuilder<T> recordOf(Class<T> recordClass) {
        return new RecordPacketCodecBuilder<>(recordClass);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    static <T extends Record> RecordPacketCodecBuilder<T> recordOf(T... recordGetter) {
        return new RecordPacketCodecBuilder<>((Class<T>) recordGetter.getClass().getComponentType());
    }

    static <T> UnionPacketCodecBuilder<T> unionOf(Class<T> type) {
        return new UnionPacketCodecBuilder<>(type);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    static <T> UnionPacketCodecBuilder<T> unionOf(T... typeGetter) {
        return new UnionPacketCodecBuilder<>((Class<T>) typeGetter.getClass().getComponentType());
    }

    static <T> PacketCodec<T> defaulted(PacketCodec<T> codec, T defaultValue) {
        return of(packetIO -> {
            if (packetIO.readBoolean()) {
                return codec.read(packetIO);
            } else {
                return defaultValue;
            }
        }, (packetIO, optional) -> {
            if (optional != null) {
                packetIO.writeBoolean(true);
                codec.write(packetIO, optional);
            } else {
                packetIO.writeBoolean(false);
            }
        });
    }

    static <T> PacketCodec<Optional<T>> optional(PacketCodec<T> codec) {
        return of(packetIO -> {
            if (packetIO.readBoolean()) {
                return Optional.of(codec.read(packetIO));
            } else {
                return Optional.empty();
            }
        }, (packetIO, optional) -> {
            if (optional.isPresent()) {
                packetIO.writeBoolean(true);
                codec.write(packetIO, optional.get());
            } else {
                packetIO.writeBoolean(false);
            }
        });
    }

    static <K, V> DuoCodec<K, V> duo(PacketCodec<K> block, PacketCodec<V> anInt) {
        return new DuoCodec<>(block, anInt);
    }

    class DuoCodec<K, V> {
        private final PacketCodec<K> key;
        private final PacketCodec<V> value;

        public DuoCodec(PacketCodec<K> key, PacketCodec<V> value) {
            this.key = key;
            this.value = value;
        }

        public <T> PacketCodec<T> flatten(
                Function<T, K> keyExtractor,
                Function<T, V> valueExtractor,
                BiFunction<K, V, T> constructor
        ) {
            return of(
                    packetIO -> constructor.apply(key.read(packetIO), value.read(packetIO)),
                    (packetIO, object) -> {
                        key.write(packetIO, keyExtractor.apply(object));
                        value.write(packetIO, valueExtractor.apply(object));
                    }
            );
        }
    }

    class RecordPacketCodecBuilder<T extends Record> {
        private final Class<T> recordClass;
        private final Map<String, PacketCodec<?>> fields = new HashMap<>();
        private final Map<String, RecordComponent> components = new HashMap<>();
        private final List<String> componentNames = new ArrayList<>();
        private final Class<?>[] componentTypes;

        public RecordPacketCodecBuilder(Class<T> recordClass) {
            this.recordClass = recordClass;

            RecordComponent[] recordComponents = recordClass.getRecordComponents();
            componentTypes = new Class[recordComponents.length];
            int i = 0;
            for (RecordComponent recordComponent : recordComponents) {
                components.put(recordComponent.getName(), recordComponent);
                componentNames.add(recordComponent.getName());
                componentTypes[i++] = recordComponent.getType();
            }
        }

        public <V> RecordPacketCodecBuilder<T> field(String name, PacketCodec<V> codec) {
            if (!components.containsKey(name)) throw new IllegalArgumentException("Unknown component: " + name);
            this.fields.put(name, codec);
            return this;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public PacketCodec<T> build() {
            return of(packetIO -> {
                try {
                    List<Object> values = new ArrayList<>();
                    for (String name : componentNames) {
                        values.add(fields.get(name).read(packetIO));
                    }
                    return recordClass.getConstructor(componentTypes).newInstance(values.toArray());
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new DecoderException(e);
                }
            }, (packetIO, record) -> {
                try {
                    for (String name : componentNames) {
                        ((PacketCodec) fields.get(name)).write(packetIO, components.get(name).getAccessor().invoke(record));
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new EncoderException(e);
                }
            });
        }
    }

    class UnionPacketCodecBuilder<T> {
        private final List<Entry<T, ?>> entries = new ArrayList<>();
        private final Class<T> type;

        public UnionPacketCodecBuilder(Class<T> type) {
            this.type = type;
        }

        private static class Entry<T, V> {
            final Class<V> type;
            final PacketCodec<V> codec;
            final Getter<T, V> getter;

            public Entry(Class<V> type, PacketCodec<V> codec, Getter<T, V> getter) {
                this.type = type;
                this.codec = codec;
                this.getter = getter;
            }

            public void write(PacketIO packetIO, T value) {
                V obj = getter.get(value);
                if (type.isInstance(obj)) {
                    codec.write(packetIO, obj);
                }
            }
        }

        public <V> UnionPacketCodecBuilder<T> add(Class<V> type, Getter<T, V> getter, PacketCodec<V> codec) {
            entries.add(new Entry<>(type, codec, getter));
            return this;
        }

        @SafeVarargs
        @SuppressWarnings("unchecked")
        public final <V> UnionPacketCodecBuilder<T> add(Getter<T, V> getter, PacketCodec<V> codec, T... typeGetter) {
            return add((Class<V>) typeGetter.getClass().getComponentType(), getter, codec);
        }

        @FunctionalInterface
        public interface Getter<T, V> {
            V get(T t);
        }

        public PacketCodec<T> build() {
            final Constructor<T> constructor;

            try {
                constructor = type.getDeclaredConstructor(entries.stream().map(tEntry -> tEntry.type).toArray(Class[]::new));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            if (!constructor.canAccess(null)) {
                try {
                    constructor.setAccessible(true);
                } catch (SecurityException e) {
                    throw new RuntimeException(e);
                }
            }

            return of(packetIO -> {
                Object[] values = new Object[entries.size()];
                for (Entry<T, ?> entry : entries) {
                    entry.codec.read(packetIO);
                }

                try {
                    return type.getConstructor(Object[].class).newInstance(values);
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }, (packetIO, value) -> {
                for (Entry<T, ?> entry : entries) {
                    entry.write(packetIO, value);
                }
            });
        }
    }
}
