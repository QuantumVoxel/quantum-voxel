package com.ultreon.quantum.api.commands.output;

import com.ultreon.quantum.api.commands.CommandSender;

public interface CommandResult {
    void send(CommandSender sender);
}