package dev.ultreon.quantum.api.neocommand.params;

import dev.ultreon.quantum.api.neocommand.CommandParseException;
import dev.ultreon.quantum.api.neocommand.CommandReader;
import dev.ultreon.quantum.api.neocommand.Parameter;
import dev.ultreon.quantum.api.neocommand.SuggestionProvider;

import java.util.List;

public class IntArgumentType implements ArgumentType<Integer> {
    private IntArgumentType() {}

    @Override
    public Integer parse(CommandReader ctx) throws CommandParseException {
        return ctx.nextInt();
    }

    @Override
    public void complete(SuggestionProvider ctx) {
        String current = ctx.getCurrent();
        for (int i = 0; i < 10; i++) {
            ctx.suggest(current + i);
        }
    }

    @Override
    public boolean matches(CommandReader arg) {
        try {
            Integer.parseInt(arg.nextWord());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public List<String> getExamples() {
        return List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    }

    public static Parameter<Integer> ints(String name) {
        return new Parameter<>(name, new IntArgumentType());
    }
}
