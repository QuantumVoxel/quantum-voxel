package dev.ultreon.quantum.api.commands.output;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.chat.Chat;

@Deprecated
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