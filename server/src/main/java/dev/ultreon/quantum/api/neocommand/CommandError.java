package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.chat.Chat;
import dev.ultreon.quantum.text.MutableText;
import dev.ultreon.quantum.text.TextObject;

public abstract class CommandError implements CommandResult {
    protected final String message;
    private final MessageCode messageCode;
    private int index;

    public CommandError(String msg) {
        this(MessageCode.GENERIC, msg);
    }

    public CommandError(String msg, int index) {
        this(MessageCode.GENERIC, msg, index);
    }

    public CommandError(MessageCode messageCode, String msg) {
        this.message = msg;
        this.index = -1;
        this.messageCode = messageCode;
    }

    public CommandError(MessageCode messageCode, String msg, int index) {
        this.message = msg;
        this.index = index;
        this.messageCode = messageCode;
    }

    public CommandError setIndex(int index) {
        this.index = index;
        return this;
    }

    public CommandError addIndex(int i) {
        this.index += i;
        return this;
    }

    @Override
    public void send(CommandSender sender) {
        if (this.index >= 0) {
            TextObject argErr = Chat.formatError(sender, "[light red]" + this.message + " - At argument " + this.index, this.getName());
            sender.sendMessage(argErr);
        } else {
            MutableText msgErr = Chat.formatError(sender, "[light red]" + this.message, this.getName());
            sender.sendMessage(msgErr);
        }
        MutableText msgErr = Chat.formatError(sender, "[light red]" + this.message, this.getName());
        MutableText msgCode = Chat.formatError(sender, "  [#cccccc]" + "Error code: " + this.messageCode.getCode() + " (" + this.messageCode + ")", this.getName());
        sender.sendMessage(msgErr);
        sender.sendMessage(msgCode);
    }

    public abstract String getName();

    protected void setGeneric() {
    }

    public void setOnlyOverloads(boolean onlyOverloads) {
    }

    public String getMessage() {
        return this.message;
    }

    public MessageCode getMessageCode() {
        return this.messageCode;
    }

    public int getIndex() {
        return this.index;
    }
}