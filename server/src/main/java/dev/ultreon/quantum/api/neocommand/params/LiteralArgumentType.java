package dev.ultreon.quantum.api.neocommand.params;

import dev.ultreon.quantum.api.neocommand.CommandParseException;
import dev.ultreon.quantum.api.neocommand.CommandReader;
import dev.ultreon.quantum.api.neocommand.SuggestionProvider;

import java.util.List;

public class LiteralArgumentType implements ArgumentType<String> {
    private final String literal;

    public LiteralArgumentType(String literal) {
        this.literal = literal;
    }

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
        return arg.equals(literal);
    }

    @Override
    public List<String> getExamples() {
        return List.of(literal);
    }
}
