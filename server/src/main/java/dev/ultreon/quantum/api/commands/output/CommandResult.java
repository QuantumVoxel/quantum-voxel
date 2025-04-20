package dev.ultreon.quantum.api.commands.output;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.neocommand.SuccessCommandResult;

@Deprecated
public interface CommandResult {
    void send(CommandSender sender);
}