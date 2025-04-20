package dev.ultreon.quantum.api.neocommand.params;

import dev.ultreon.quantum.api.neocommand.CommandParseException;
import dev.ultreon.quantum.api.neocommand.CommandReader;
import dev.ultreon.quantum.api.neocommand.Parameter;
import dev.ultreon.quantum.api.neocommand.SuggestionProvider;

import java.util.List;

public class StringArgumentType implements ArgumentType<String> {
    private StringArgumentType() {}

    @Override
    public String parse(CommandReader ctx) throws CommandParseException {
        return ctx.nextWord();
    }

    @Override
    public void complete(SuggestionProvider ctx) {
        // do nothing
    }

    @Override
    public boolean matches(CommandReader arg) {
        return true;
    }

    @Override
    public List<String> getExamples() {
        return List.of("hello", "world", "hello world");
    }

    public static Parameter<String> strings(String name) {
        return new Parameter<>(name, new StringArgumentType());
    }
}
