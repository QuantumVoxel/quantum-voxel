package dev.ultreon.quantum.api.commands;

@Deprecated
public interface CommandParser<T> {
    T parse(CommandReader ctx) throws CommandParseException;
}