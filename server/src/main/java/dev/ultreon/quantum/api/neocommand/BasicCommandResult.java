package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.chat.Chat;

public class BasicCommandResult implements CommandResult {
    private final String message;
    private final MessageType type;

    private BasicCommandResult(String message, MessageType type) {
        this.message = message;
        this.type = type;
    }

    public static BasicCommandResult server(String s) {
        return new BasicCommandResult(s, MessageType.SERVER);
    }

    public static BasicCommandResult success(String s) {
        return new BasicCommandResult(s, MessageType.SUCCESS);
    }

    public static BasicCommandResult info(String s) {
        return new BasicCommandResult(s, MessageType.INFO);
    }

    public static BasicCommandResult warning(String s) {
        return new BasicCommandResult(s, MessageType.WARNING);
    }

    public static BasicCommandResult error(String s) {
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

    private enum MessageType {
        SERVER, SUCCESS, INFO, WARNING, DENIED, EDIT_MODE, ERROR, FATAL
    }
}