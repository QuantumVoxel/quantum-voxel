package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.QuantumServer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Commands {
    static final SequencedMap<String, CommandRegistrant> ALIASES = new LinkedHashMap<>();
    private static final SequencedMap<String, CommandRegistrant> COMMANDS = new LinkedHashMap<>();
    private static boolean refresh;

    public static CommandRegistrant register(String name) {
        CommandRegistrant commandRegistrant = new CommandRegistrant(name);
        COMMANDS.put(name, commandRegistrant);
        return commandRegistrant;
    }

    public static void check() {
        for (CommandRegistrant commandRegistrant : COMMANDS.values()) {
            commandRegistrant.check();
        }
    }

    public static CommandResult execute(QuantumServer server, CommandSender sender, String command) {
        String[] s = command.split(" ");
        String cmd = s[0];
        String[] args = new String[s.length - 1];
        System.arraycopy(s, 1, args, 0, args.length);
        try {
            return Commands.getCommand(cmd).execute(server, sender, args);
        } catch (CommandParseException e) {
            return new CommandError(MessageCode.GENERIC, "[red]Error: [light red]" + e.getMessage()) {
                @Override
                public String getName() {
                    return cmd;
                }
            };
        }
    }

    public static List<String> complete(CommandSender sender, QuantumServer server, String[] argv) {
        if (argv.length == 0) {
            return new ArrayList<>(COMMANDS.keySet());
        } else if (argv.length == 1) {
            return completeCommands(argv);
        }

        String[] args = new String[argv.length - 1];
        if (!argv[0].startsWith("/"))
            return new ArrayList<>();

        System.arraycopy(argv, 1, args, 0, args.length);

        CommandRegistrant commandRegistrant = COMMANDS.get(argv[0].substring(1));
        if (commandRegistrant == null) commandRegistrant = ALIASES.get(argv[0]);
        if (commandRegistrant == null) return new ArrayList<>();

        return commandRegistrant.complete(sender, new CommandReader(argv[0], args, server, sender), argv);
    }

    private static @NotNull ArrayList<String> completeCommands(String[] args) {
        if (args[0].startsWith("/")) {
            Set<String> list = new HashSet<>();
            for (CommandRegistrant commandRegistrant : COMMANDS.values()) {
                for (String alias : commandRegistrant.getAliases()) {
                    if (alias.startsWith(args[0].substring(1))) {
                        list.add("/" + alias);
                    }
                }
            }

            return new ArrayList<>(list);
        } else {
            return new ArrayList<>(COMMANDS.keySet());
        }
    }

    public static CommandRegistrant getCommand(String cmd) {
        return COMMANDS.get(cmd);
    }

    public static SequencedCollection<CommandRegistrant> getCommands() {
        return COMMANDS.sequencedValues();
    }

    public static int size() {
        return COMMANDS.size();
    }

    public static boolean needRefresh() {
        return refresh;
    }

    public static void clearRefresh() {
        refresh = false;
    }

    public static void refresh() {
        refresh = true;
    }
}
