package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.QuantumServer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandRegistrant {
    private final HashMap<List<Parameter<?>>, CommandExecutor> overloads = new HashMap<>();
    private final List<String> aliases = new ArrayList<>();
    private final String name;
    private CommandExecutor executor;
    private String description;
    private boolean frozen;

    CommandRegistrant(String name) {
        this.name = name;
    }

    public CommandRegistrant overload(CommandExecutor executor, Parameter<?>... arguments) {
        this.overloads.put(List.of(arguments), executor);
        this.executor = executor;
        return this;
    }

    CommandExecutor getExecutor() {
        return this.executor;
    }

    public CommandResult execute(QuantumServer server, CommandSender sender, String... args) throws CommandParseException {
        List<Argument<?>> arguments = new ArrayList<>();
        CommandReader reader = new CommandReader(name, args, server, sender);
        for (List<Parameter<?>> parameters : this.overloads.keySet()) {
            if (parameters.size() != args.length) {
                continue;
            }
            for (int i = 0; i < parameters.size(); i++) {
                Parameter<?> parameter = parameters.get(i);
                if (!parameter.type().matches(reader)) {
                    break;
                }
                Object parse = parameter.type().parse(reader);
                arguments.add(new Argument<>(parse.getClass(), parameter.name(), parse));
                if (i == parameters.size() - 1) {
                    return this.overloads.get(parameters).execute(new CommandContext(server, sender, arguments.toArray(Argument[]::new)));
                }
            }
            reader.seek(0);
            arguments.clear();
        }

        throw new CommandParseException("Invalid arguments");
    }

    public String getName() {
        return name;
    }

    public void check() {
        if (!frozen)
            throw new IllegalStateException("Command '" + name + "' is not frozen yet!");

        List<List<Parameter<?>>> parameterLists = new ArrayList<>(this.overloads.keySet());

        for (int i = 0; i < parameterLists.size(); i++) {
            for (int j = i + 1; j < parameterLists.size(); j++) {
                if (parametersConflict(parameterLists.get(i), parameterLists.get(j))) {
                    throw new IllegalStateException("Command '" + name + "' has conflicting parameter lists");
                }
            }
        }
    }

    private boolean parametersConflict(List<Parameter<?>> list1, List<Parameter<?>> list2) {
        if (list1.size() != list2.size()) return false;

        for (int i = 0; i < list1.size(); i++) {
            Parameter<?> param1 = list1.get(i);
            Parameter<?> param2 = list2.get(i);

            if (!param1.type().equals(param2.type())) {
                return false;
            }
        }
        return true;
    }

    public CommandRegistrant description(String description) {
        if (this.description != null)
            throw new IllegalStateException("Command '" + name + "' already has a description");
        this.description = description;
        return this;
    }

    public CommandRegistrant aliases(String... aliases) {
        for (String alias : aliases) Commands.ALIASES.put(alias, this);
        this.aliases.addAll(List.of(aliases));
        return this;
    }

    public CommandRegistrant aliases(Collection<String> aliases) {
        checkFrozen();
        for (String alias : aliases) Commands.ALIASES.put(alias, this);
        this.aliases.addAll(aliases);
        return this;
    }

    public CommandRegistrant alias(String alias) {
        Commands.ALIASES.put(alias, this);
        this.aliases.add(alias);
        return this;
    }

    public List<String> getAliases() {
        return Collections.unmodifiableList(this.aliases);
    }

    public String getDescription() {
        return description;
    }

    private void checkFrozen() {
        if (this.frozen)
            throw new IllegalStateException("Command '" + name + "' is frozen");
    }

    public void build() {
        this.frozen = true;
    }

    public List<String> complete(CommandSender sender, CommandReader reader, String[] args) {
        int start = reader.tell();
        for (List<Parameter<?>> overload : this.overloads.keySet()) {
            if (overload.size() != args.length) {
                continue;
            }
            for (int i = 0; i < overload.size(); i++) {
                if (!overload.get(i).type().matches(reader)) {
                    break;
                }
                if (reader.isEOF()) {
                    reader.seek(start);
                    return doSuggest(sender, reader, args, overload);
                }
            }

            reader.seek(start);
        }

        return Collections.emptyList();
    }

    private @NotNull List<String> doSuggest(CommandSender sender, CommandReader reader, String[] args, List<Parameter<?>> overload) {
        List<String> suggestions = new ArrayList<>();
        for (Parameter<?> parameter : overload) {
            int start = reader.tell();
            boolean matches = parameter.type().matches(reader);
            if (reader.isEOF()) {
                reader.seek(start);
                parameter.type().complete(new SuggestionHandler(
                        suggestions,
                        name,
                        args,
                        sender.getServer(),
                        reader,
                        sender
                ));
                break;
            }
            if (!matches) {
                break;
            }

        }
        return suggestions;
    }

    private static class SuggestionHandler extends CommandReader implements SuggestionProvider {
        private final List<String> suggestions;
        private final CommandReader reader;
        private final CommandSender sender;

        public SuggestionHandler(List<String> suggestions, String commandName, String[] args, QuantumServer server, CommandReader reader, CommandSender sender) {
            super(commandName, args, server, sender);
            this.suggestions = suggestions;
            this.reader = reader;
            this.sender = sender;
        }

        @Override
        public void suggest(String value) {
            suggestions.add(value);
        }

        @Override
        public String getCurrent() {
            return reader.current();
        }

        @Override
        public QuantumServer getServer() {
            return sender.getServer();
        }

        @Override
        public CommandSender getSender() {
            return sender;
        }
    }
}
