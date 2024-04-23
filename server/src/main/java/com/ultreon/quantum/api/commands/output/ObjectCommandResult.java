package com.ultreon.quantum.api.commands.output;

import com.ultreon.quantum.api.commands.CommandSender;
import com.ultreon.quantum.server.chat.Chat;

public class ObjectCommandResult implements CommandResult {
    private final Object object;
    private final Type type;

    public ObjectCommandResult(Object object, Type type) {
        this.object = object;
        this.type = type;
    }

    @Override
    public void send(CommandSender sender) {
        switch (this.type) {
            case Void:
                Chat.sendVoidObject(sender);
                break;

            case Object:
                Chat.sendObject(sender, this.object);
                break;
        }
    }

    public Object getObject() {
        return object;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        Void, Object
    }
}