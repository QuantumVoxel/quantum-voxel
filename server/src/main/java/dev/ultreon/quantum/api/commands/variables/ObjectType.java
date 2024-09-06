package dev.ultreon.quantum.api.commands.variables;

import dev.ultreon.libs.commons.v0.Either;
import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.world.ServerWorld;

import java.util.HashMap;
import java.util.List;
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
    public static final ObjectType<Command> COMMAND = new ObjectType<>("command");
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

    public ObjectField<T, ?> getField(String name) {
        return this.fields.get(name);
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public ObjectType<T> getObjectType() {
        return this;
    }

    @Override
    public Object get(CommandSender sender, CommandReader ctx) throws CommandParseException {
        String name = ctx.readUntil('.');
        ObjectField<T, ?> field = getField(name);
        if (field == null) throw new CommandParseException("Unknown field " + ctx.readString(), ctx.getOffset());
        Object o = field.get(sender, ctx);

        ctx.readChar();
        if (ctx.getCurChar() == '.') {
            ObjectType<?> objectType = ObjectType.get(o.getClass());
            if (objectType == null) {
                throw new CommandParseException("Illegal type " + o.getClass().getName(), ctx.getOffset());
            }

            return objectType.get(sender, ctx);
        }

        return o;
    }

    @Override
    public Either<Object, List<String>> tabComplete(ServerPlayer serverPlayer, CommandReader ctx, StringBuilder currentCode) throws CommandParseException.EndOfArgument {
        String s = ctx.readUntil('.');
        if (ctx.getLastChar() == '.') {
            ObjectField<T, ?> tObjectField = fields.get(s);
            if (tObjectField == null) {
                return Either.right(TabCompleting.prefixed(currentCode.toString(), ctx.getArgument(), TabCompleting.strings(ctx.getArgument().substring(currentCode.length()), fields.keySet().toArray(new String[0]))));
            }

            return tObjectField.tabComplete(serverPlayer, ctx, currentCode);
        }

        return Either.right(TabCompleting.prefixed(currentCode.toString(), ctx.getArgument(), TabCompleting.strings(ctx.getArgument().substring(currentCode.length()), fields.keySet().toArray(new String[0]))));

    }

    public boolean isInstance(Object obj) {
        return type.isInstance(obj);
    }

    public T cast(Object obj) {
        return type.cast(obj);
    }
}
