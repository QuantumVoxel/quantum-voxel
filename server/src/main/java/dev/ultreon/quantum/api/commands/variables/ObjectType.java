package dev.ultreon.quantum.api.commands.variables;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;

public class ObjectType<T> implements ObjectSource<T> {
    private static final Map<Class<?>, ObjectType<?>> TYPES = new HashMap<>();

    public static final ObjectType<String> STRING = new ObjectType<>("string");
    public static final ObjectType<Number> NUMBER = new ObjectType<>("number");
    public static final ObjectType<Boolean> BOOLEAN = new ObjectType<>("boolean");

    public static final ObjectType<ServerPlayer> PLAYER = new ObjectType<>("player");
    public static final ObjectType<Map<String, ServerPlayer>> PLAYER_LIST = new ObjectType<>("player-list");
    public static final ObjectType<QuantumServer> SERVER = new ObjectType<>("server");
    public static final ObjectType<ServerWorld> WORLD = new ObjectType<>("world");
    public static final ObjectType<Block> BLOCK = new ObjectType<>("block");
    public static final ObjectType<Entity> ENTITY = new ObjectType<>("entity");
    public static final ObjectType<Item> ITEM = new ObjectType<>("item");
    public static final ObjectType<ItemStack> ITEM_STACK = new ObjectType<>("item");
    public static final ObjectType<Void> NONE = new ObjectType<>("none");

    static {
        SERVER.registerField("player-list", new ObjectField<>("player-list", PLAYER_LIST, QuantumServer::getPlayersByName));
    }

    private final String name;
    private final Map<String, ObjectField<T, ?>> fields = new HashMap<>();
    private final Class<T> type;

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public ObjectType(String name, T... typeGetter) {
        this.name = name;
        this.type = (Class<T>) typeGetter.getClass().getComponentType();

        TYPES.put(this.type, this);
    }

    @SuppressWarnings("unchecked")
    public static <T> ObjectType<T> get(Class<T> type) {
        for (Class<?> t : TYPES.keySet()) if (t.isAssignableFrom(type)) return (ObjectType<T>) TYPES.get(t);
        return null;
    }

    public String getName() {
        return name;
    }

    public void registerField(String name, ObjectField<T, ?> field) {
        this.fields.put(name, field);
        field.setSource(this);
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public ObjectType<T> getObjectType() {
        return this;
    }

    public boolean isInstance(Object obj) {
        return type.isInstance(obj);
    }

    public T cast(Object obj) {
        return type.cast(obj);
    }
}
