package com.ultreon.quantum.api.commands.output;

import com.ultreon.quantum.api.commands.CommandSender;
import com.ultreon.quantum.text.TextObject;

public class ComponentMessage implements CommandResult {
    private final TextObject component;

    public ComponentMessage(TextObject component) {
        this.component = component;
    }

    @Override
    public void send(CommandSender sender) {
        sender.sendMessage(this.component);
    }
}