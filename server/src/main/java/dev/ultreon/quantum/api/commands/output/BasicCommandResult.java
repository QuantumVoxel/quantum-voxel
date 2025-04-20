package dev.ultreon.quantum.api.commands.output;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.chat.Chat;

@Deprecated
public class BasicCommandResult implements CommandResult {
    private final String message;
    private final MessageType type;

    public BasicCommandResult(String message, MessageType type) {
        this.message = message;
        this.type = type;
    }

    public static CommandResult server(String s) {
        return new BasicCommandResult(s, MessageType.SERVER);
    }

    public static CommandResult success(String s) {
        return new BasicCommandResult(s, MessageType.SUCCESS);
    }

    public static CommandResult info(String s) {
        return new BasicCommandResult(s, MessageType.INFO);
    }

    public static CommandResult warning(String s) {
        return new BasicCommandResult(s, MessageType.WARNING);
    }

    public static CommandResult error(String s) {
        return new BasicCommandResult(s, MessageType.ERROR);
    }

    public static CommandResult fatal(String s) {
        return new BasicCommandResult(s, MessageType.FATAL);
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