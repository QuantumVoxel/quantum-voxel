package dev.ultreon.quantum.api.neocommand.params;

import dev.ultreon.quantum.api.neocommand.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandArgumentType implements ArgumentType<CommandRegistrant> {
    private @Nullable List<String> commands;

    private CommandArgumentType() {}

    @Override
    public CommandRegistrant parse(CommandReader ctx) throws CommandParseException {
        String name = ctx.nextWord();
        CommandRegistrant commandRegistrant = Commands.getCommand(name);
        if (commandRegistrant == null)
            throw new CommandParseException("Unknown command: " + name, ctx.tell());
        return commandRegistrant;
    }

    @Override
    public void complete(SuggestionProvider ctx) {
        for (String example : getExamples()) {
            if (example.startsWith(ctx.getCurrent()))
                ctx.suggest(example);
        }
    }

    @Override
    public boolean matches(CommandReader arg) {
        return getExamples().contains(arg);
    }

    @Override
    public List<String> getExamples() {
        if (commands != null && !Commands.needRefresh())
            return commands;
        return commands = retrieveCommandNames();
    }

    private static @NotNull List<@NotNull String> retrieveCommandNames() {
        List<String> list = new ArrayList<>();
        for (CommandRegistrant commandRegistrant : Commands.getCommands()) {
            String name = commandRegistrant.getName();
            list.add(name);
        }
        return Collections.unmodifiableList(list);
    }

    public static Parameter<CommandRegistrant> commands(String name) {
        return new Parameter<>(name, new CommandArgumentType());
    }

}
