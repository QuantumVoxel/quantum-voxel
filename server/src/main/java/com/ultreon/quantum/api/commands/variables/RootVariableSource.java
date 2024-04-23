package com.ultreon.quantum.api.commands.variables;

import com.ultreon.libs.commons.v0.Either;
import com.ultreon.quantum.api.commands.CommandParseException;
import com.ultreon.quantum.api.commands.CommandReader;
import com.ultreon.quantum.api.commands.CommandSender;
import com.ultreon.quantum.server.player.ServerPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RootVariableSource<T> implements ObjectSource<T> {
    static final Map<String, RootVariableSource<?>> VARIABLES = new HashMap<>();

    private final ObjectType<T> type;
    private final Supplier<T> getter;

    public RootVariableSource(ObjectType<T> type, Supplier<T> getter) {
        this.type = type;
        this.getter = getter;
        VARIABLES.put(type.getName(), this);
    }

    public RootVariableSource(String name, ObjectType<T> type, Supplier<T> getter) {
        this.type = type;
        this.getter = getter;
        VARIABLES.put(name, this);
    }

    @Override
    public ObjectType<T> getObjectType() {
        return this.type;
    }

    @Override
    public Object get(CommandSender sender, CommandReader ctx) {
        return this.getter.get();
    }

    @Override
    public Either<Object, List<String>> tabComplete(ServerPlayer serverPlayer, CommandReader ctx, StringBuilder code) throws CommandParseException.EndOfArgument {
        ObjectType<T> type = this.type;
        return type.tabComplete(serverPlayer, ctx, code);
    }

    public Supplier<T> getter() {
        return getter;
    }
}
