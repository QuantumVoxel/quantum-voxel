package com.ultreon.quantum.api.commands.variables;

import com.ultreon.libs.commons.v0.Either;
import com.ultreon.quantum.api.commands.CommandParseException;
import com.ultreon.quantum.api.commands.CommandReader;
import com.ultreon.quantum.api.commands.CommandSender;
import com.ultreon.quantum.server.player.ServerPlayer;

import java.util.List;

public interface ObjectSource<T> {
    default Class<T> getType() {
        return getObjectType().getType();
    }
    ObjectType<T> getObjectType();

    Object get(CommandSender sender, CommandReader ctx) throws CommandParseException;

    Either<Object, List<String>> tabComplete(ServerPlayer serverPlayer, CommandReader ctx, StringBuilder code) throws CommandParseException.EndOfArgument;
}
