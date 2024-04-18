package com.ultreon.quantum.api.commands.output;

import com.ultreon.quantum.api.commands.CommandSender;

public record StringMessage(String text) implements CommandResult {
    @Override
    public void send(CommandSender sender) {
        sender.sendMessage(this.text);
    }
}