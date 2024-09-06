package dev.ultreon.quantum.api.commands.output;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.chat.Chat;

public class BasicCommandResult implements CommandResult {
    private final String message;
    private final MessageType type;

    public BasicCommandResult(String message, MessageType type) {
        this.message = message;
        this.type = type;
    }

    @Override
    public void send(CommandSender sender) {
        switch (this.type) {
            case SERVER:
                Chat.sendServerMessage(sender, this.message);
                break;
            case SUCCESS:
                Chat.sendSuccess(sender, this.message);
                break;
            case INFO:
                Chat.sendInfo(sender, this.message);
                break;
            case WARNING:
                Chat.sendWarning(sender, this.message);
                break;
            case DENIED:
                Chat.sendDenied(sender, this.message);
                break;
            case ERROR:
                Chat.sendError(sender, this.message);
                break;
            case FATAL:
                Chat.sendFatal(sender, this.message);
                break;
        }
    }

    public enum MessageType {
        SERVER, SUCCESS, INFO, WARNING, DENIED, EDIT_MODE, ERROR, FATAL
    }
}