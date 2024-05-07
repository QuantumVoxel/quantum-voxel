package dev.ultreon.quantum.api.commands.output;

import dev.ultreon.quantum.api.commands.CommandSender;

public interface CommandResult {
    void send(CommandSender sender);
}