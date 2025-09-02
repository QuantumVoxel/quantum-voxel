package dev.ultreon.quantum.api.neocommand.params;

import dev.ultreon.quantum.api.neocommand.CommandParseException;
import dev.ultreon.quantum.api.neocommand.CommandReader;
import dev.ultreon.quantum.api.neocommand.Parameter;
import dev.ultreon.quantum.api.neocommand.SuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanArgumentType implements ArgumentType<Boolean> {
    private final List<@NotNull String> examples = List.of("true", "false", "0", "1", "yes", "no", "enabled", "disabled");

    private BooleanArgumentType() {}

    @Override
    public Boolean parse(CommandReader ctx) throws CommandParseException {
        String s = ctx.nextWord();
        if ("true".equalsIgnoreCase(s)) return true;
        if ("false".equalsIgnoreCase(s)) return false;
        if ("0".equalsIgnoreCase(s)) return false;
        if ("1".equalsIgnoreCase(s)) return true;
        if ("yes".equalsIgnoreCase(s)) return true;
        if ("no".equalsIgnoreCase(s)) return false;
        if ("enabled".equalsIgnoreCase(s)) return true;
        if ("disabled".equalsIgnoreCase(s)) return false;
        throw new CommandParseException("Invalid boolean: " + s, ctx.tell());
    }

    @Override
    public void complete(SuggestionProvider ctx) {
        String current = ctx.getCurrent();
        if ("true".startsWith(current)) ctx.suggest("true");
        if ("false".startsWith(current)) ctx.suggest("false");
        if ("0".startsWith(current)) ctx.suggest("0");
        if ("1".startsWith(current)) ctx.suggest("1");
        if ("yes".startsWith(current)) ctx.suggest("yes");
        if ("no".startsWith(current)) ctx.suggest("no");
        if ("enabled".startsWith(current)) ctx.suggest("enabled");
        if ("disabled".startsWith(current)) ctx.suggest("disabled");
    }

    @Override
    public boolean matches(CommandReader arg) {
        String s = arg.nextWord();
        return "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)
                || "0".equalsIgnoreCase(s) || "1".equalsIgnoreCase(s)
                || "yes".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)
                || "enabled".equalsIgnoreCase(s) || "disabled".equalsIgnoreCase(s);
    }

    @Override
    public List<String> getExamples() {
        return examples;
    }

    public static Parameter<Boolean> booleans(String name) {
        return new Parameter<>(name, new BooleanArgumentType());
    }
}
