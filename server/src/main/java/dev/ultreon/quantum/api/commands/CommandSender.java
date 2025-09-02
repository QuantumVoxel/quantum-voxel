package dev.ultreon.quantum.api.commands;

import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.api.neocommand.Commands;
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
     * Executes a command with the given input string. This method is a shorthand
     * for {@link #runCommand(String, boolean)} with `sendToChat` set to true by default.
     *
     * @param input The input string containing the command and arguments.
     *              The first slash should already be removed from the input.
     * @return The result of executing the command as a {@link dev.ultreon.quantum.api.neocommand.CommandResult}.
     *         Returns <code>null</code> if the input is empty or the command execution fails.
     */
    default dev.ultreon.quantum.api.neocommand.CommandResult runCommand(String input) {
        return runCommand(input, true);
    }

    /**
     * Executes a command with the provided input string, optionally sending the output to chat.
     * This method processes the input by parsing the command and its arguments,
     * then executes it within the context of the command sender.
     * If the input is empty, the method returns null and no further action is taken.
     *
     * @param input The input string containing the command and its arguments.
     *              The first slash should already be removed, and the string is trimmed internally.
     * @param sendToChat A boolean value indicating whether the command's output should be sent to the chat.
     *                   If true, the output (or error, if applicable) will be displayed in the chat.
     * @return The result of executing the command as a {@link dev.ultreon.quantum.api.neocommand.CommandResult}.
     *         Returns null if the input is empty, or if executing the command fails.
     */
    default @NotNull dev.ultreon.quantum.api.neocommand.CommandResult runCommand(String input, boolean sendToChat) {
        // Trim the input to remove any leading or trailing whitespace
        var commandline = input.trim();

        // If the input is empty, do nothing
        if (commandline.isEmpty()) {
            return dev.ultreon.quantum.api.neocommand.CommandResult.success();
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
        QuantumServer.LOGGER.info("{} ran command: {}", this.getName(), commandline);

        // Retrieve the base command from the registry
        dev.ultreon.quantum.api.neocommand.CommandResult result = Commands.execute(this.getServer(), this, command);
        if (sendToChat) {
            result.send(this);
        }
        return result;
    }

    @Nullable QuantumServer getServer();
}
