package dev.ultreon.quantum.api.neocommand;

/**
 * A functional interface for executing commands.
 * Implementors of this interface handle the actual execution logic of commands.
 *
 * @see CommandRegistration
 */
@FunctionalInterface
public interface CommandExecutor {

    /**
     * Executes a command with the given context.
     *
     * @param context The command execution context containing sender, arguments and server details
     * @return The result of executing the command
     */
    CommandResult execute(CommandContext context);
}
