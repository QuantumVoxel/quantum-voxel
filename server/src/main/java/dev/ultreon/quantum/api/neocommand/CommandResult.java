package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.CommandSender;

public interface CommandResult {
    static CommandResult success() {
        return new SuccessCommandResult();
    }

    void send(CommandSender sender);
}