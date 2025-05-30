package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.QuantumServer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Commands {
    static final Map<String, CommandRegistrant> ALIASES = new LinkedHashMap<>();
    private static final Map<String, CommandRegistrant> COMMANDS = new LinkedHashMap<>();
    private static boolean refresh;

    public static CommandRegistrant register(String name) {
        // Create a new command registrant, and save it into the COMMANDS map.
        CommandRegistrant commandRegistrant = new CommandRegistrant(name);
        COMMANDS.put(name, commandRegistrant);
        return commandRegistrant;
    }

    public static void check() {
        // Check every command registered.
        for (CommandRegistrant commandRegistrant : COMMANDS.values()) {
            commandRegistrant.check();
        }
    }

    /**
     * Executes the specified command sent by a command sender within the context of the provided server.
     * This method processes the input command, determines the corresponding registered command, and executes it.
     * If the command is not recognized or an error occurs during execution, it returns an appropriate error result.
     *
     * @param server the QuantumServer instance where the command is being executed
     * @param sender the entity or object that issued the command
     * @param command the raw command input as a string
     * @return a CommandResult representing the outcome of the command execution, either a success or an error
     */
    public static CommandResult execute(QuantumServer server, CommandSender sender, String command) {
        // Extract command name
        String[] s = command.split(" ");
        String cmd = s[0];

        // Extract arguments
        String[] args = new String[s.length - 1];
        System.arraycopy(s, 1, args, 0, args.length);

        // Try to parse command input from the split arguments.
        try {
            CommandRegistrant command1 = Commands.getCommand(cmd);
            if (command1 == null) {
                // Command isn't found, return a new command error as output.
                return new CommandError(MessageCode.GENERIC, "[red]Unknown command: [light red]" + cmd) {

                    /**
                     * Returns the command's name.
                     */
                    @Override
                    public String getName() {
                        return cmd;
                    }
                };
            }

            // No issues so far, execute the command!
            return command1.execute(server, sender, args);
        } catch (CommandParseException e) {
            return new CommandError(MessageCode.GENERIC, "[red]Error: [light red]" + e.getMessage()) {
                @Override
                public String getName() {
                    return cmd;
                }
            };
        }
    }

    /**
     * Provides command completion suggestions based on the input arguments.
     * This method handles different levels of command arguments to suggest possible completions,
     * including top-level commands, subcommands, or no commands when the input is invalid.
     *
     * @param sender the entity or object that issued the command and requested the completion suggestions
     * @param server the current server instance containing the command registry and execution context
     * @param argv the array of input arguments that specifies the current state and context of the command
     * @return a list of possible command completions based on the current arguments; an empty list if no completions are available
     */
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

    /**
     * Provides a list of possible command completions based on the input arguments.
     * If the input starts with "/", the method returns a list of commands that match
     * the current input as registered command aliases. Otherwise, it returns the top-level commands.
     *
     * @param args the input arguments used to determine command completions
     * @return a list of suggested command completions; never null
     */
    private static @NotNull List<String> completeCommands(String[] args) {
        // Only complete commands if starting with '/'
        if (!args[0].startsWith("/")) return Collections.emptyList();
        Set<String> list = new HashSet<>();
        String cmdName = args[0].substring(1);

        // Loop through all commands and check the name.
        for (CommandRegistrant commandRegistrant : COMMANDS.values()) {
            for (String alias : commandRegistrant.getAliases()) {

                // If the name or alias starts with the command name currently in input,
                // add it to the list of completions
                if (alias.startsWith(cmdName)) {
                    list.add("/" + alias);
                }
            }
        }

        return List.copyOf(list);
    }

    public static CommandRegistrant getCommand(String cmd) {
        // Return the comand instance assigned by the name.
        // TODO: Might be better to check for aliases too.
        return COMMANDS.get(cmd);
    }

    public static Collection<CommandRegistrant> getCommands() {
        return COMMANDS.values();
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
