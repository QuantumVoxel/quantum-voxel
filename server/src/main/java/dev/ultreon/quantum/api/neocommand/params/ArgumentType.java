package dev.ultreon.quantum.api.neocommand.params;

import dev.ultreon.quantum.api.neocommand.CommandParseException;
import dev.ultreon.quantum.api.neocommand.CommandReader;
import dev.ultreon.quantum.api.neocommand.SuggestionProvider;

import java.util.List;

public interface ArgumentType<T> {
    T parse(CommandReader ctx) throws CommandParseException;

    void complete(SuggestionProvider ctx);

    boolean matches(CommandReader arg);

    List<String> getExamples();
}
