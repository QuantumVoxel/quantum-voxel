package dev.ultreon.quantum.api.commands;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.api.commands.error.CommandError;
import dev.ultreon.quantum.api.commands.output.BasicCommandResult;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.registry.CommandRegistry;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.text.Formatter;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.world.Location;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * The CommandSender interface represents an object that can send commands and receive messages.
 * <p>
 * This interface provides methods to get the name, public name (if available), and display name of the command sender.
 * It also allows sending messages to the command sender and executing commands with it.
 */
public interface CommandSender {
    @NotNull Location getLocation();

    String getName();
    @Nullable String getPublicName();
    TextObject getDisplayName();

    UUID getUuid();

    /**
     * Sends the command sender a message back.
     *
     * @param message a message that can be formatted by {@link Formatter}.
     */
    void sendMessage(@NotNull String message);

    void sendMessage(@NotNull TextObject component);

    default boolean hasExplicitPermission(@NotNull String permission) {
        return this.hasExplicitPermission(new Permission(permission));
    }

    boolean hasExplicitPermission(@NotNull Permission permission);

    default boolean hasPermission(@NotNull String permission) {
        return this.hasPermission(new Permission(permission));
    }

    default boolean hasPermission(@NotNull Permission permission) {
        QuantumServer server = QuantumServer.get();
        if (server != null) {
            server.getDefaultPermissions().has(permission);
        }
        return this.hasExplicitPermission(permission);
    }

    boolean isAdmin();

    /**
     * Executes a slash command with the given input.
     * The method expects to have the first slash pre-removed from the input.
     * <p>
     * So, if the actual command is "/example", the method should be called with "example" as the input.<br>
     * And if the actual command is "//example", the method should be called with "/example" as the input.
     * <p>
     * This method doesn't need to be implemented. As it executes the command already by default.
     *
     * @param input The input string containing the command and arguments.
     * @return
     */
    @CanIgnoreReturnValue
    default CommandResult execute(String input) {
        return execute(input, true);
    }

    /**
     * Executes a slash command with the given input.
     * The method expects to have the first slash pre-removed from the input.
     * <p>
     * So, if the actual command is "/example", the method should be called with "example" as the input.<br>
     * And if the actual command is "//example", the method should be called with "/example" as the input.
     * <p>
     * This method doesn't need to be implemented. As it executes the command already by default.
     *
     * @param input The input string containing the command and arguments.
     * @return
     */
    @CanIgnoreReturnValue
    default CommandResult execute(String input, boolean sendToChat) {
        // Trim the input to remove any leading or trailing whitespace
        var commandline = input.trim();

        // If the input is empty, do nothing
        if (commandline.isEmpty()) {
            return null;
        }

        // Separate the command and arguments
        String command;
        String[] argv;
        if (!commandline.contains(" ")) {
            // If no arguments, set argv as an empty array
            argv = new String[0];
            command = commandline;
        } else {
            // Split the commandline at the space to separate command and arguments
            argv = commandline.split(" ");
            command = argv[0];
            // Remove the command from the arguments array
            argv = ArrayUtils.remove(argv, 0);
        }
        // Log the command being executed
        QuantumServer.LOGGER.info(this.getName() + " ran command: " + commandline);

        // Retrieve the base command from the registry
        Command baseCommand = CommandRegistry.get(command);
        if (baseCommand == null) {
            // If the command is not found, send an error message
            return new BasicCommandResult("Unknown command&: " + command, BasicCommandResult.MessageType.ERROR);
        }

        CommandResult commandResult = baseCommand.onCommand(this, new CommandContext(command), command, argv);
        if (sendToChat) {
            if (commandResult instanceof CommandError error) {
                // Command has an error
                error.send(this, baseCommand.data());
            } else {
                // Command has an output
                commandResult.send(this);
            }
        }
        return commandResult;
    }
}
