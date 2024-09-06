package dev.ultreon.quantum.api.commands.variables;

import dev.ultreon.libs.commons.v0.Either;
import dev.ultreon.quantum.api.commands.CommandParseException;
import dev.ultreon.quantum.api.commands.CommandReader;
import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.player.ServerPlayer;

import java.util.List;
import java.util.function.Function;

public class ObjectField<T, R> implements ObjectSource<R> {
    private final String name;
    private ObjectSource<T> source;
    private final ObjectType<R> objectType;
    private final Function<T, R> getter;

    public ObjectField(String name, ObjectType<R> objectType, Function<T, R> getter) {
        this.name = name;
        this.objectType = objectType;
        this.getter = getter;
    }

    void setSource(ObjectSource<T> source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public ObjectSource<?> getSource() {
        return source;
    }

    @Override
    public ObjectType<R> getObjectType() {
        return this.objectType;
    }

    @Override
    public Object get(CommandSender sender, CommandReader ctx) throws CommandParseException {
        Object o = source.get(sender, ctx);
        ObjectType<T> sourceType = this.source.getObjectType();
        if (sourceType.isInstance(o)) {
            return getter.apply(sourceType.cast(o));
        }

        throw new CommandParseException("Expected " + this.objectType.getName() + " but got " + o.getClass().getName());
    }

    @Override
    public Either<Object, List<String>> tabComplete(ServerPlayer serverPlayer, CommandReader ctx, StringBuilder code) throws CommandParseException.EndOfArgument {
        return source.tabComplete(serverPlayer, ctx, code);
    }

    public Function<T, R> getter() {
        return getter;
    }
}
