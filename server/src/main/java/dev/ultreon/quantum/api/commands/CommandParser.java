package dev.ultreon.quantum.api.commands;

public interface CommandParser<T> {
    T parse(CommandReader ctx) throws CommandParseException;
}