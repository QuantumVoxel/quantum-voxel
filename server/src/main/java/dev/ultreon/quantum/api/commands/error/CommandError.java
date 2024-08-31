package dev.ultreon.quantum.api.commands.error;

import dev.ultreon.quantum.api.commands.CommandData;
import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.commands.CommandSpec;
import dev.ultreon.quantum.api.commands.MessageCode;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.server.chat.Chat;
import dev.ultreon.quantum.text.MutableText;
import dev.ultreon.quantum.text.TextObject;

public abstract class CommandError implements CommandResult {
    protected final String message;
    private final MessageCode messageCode;
    private int index;
    private boolean isGeneric = false;
    private boolean onlyOverloads = false;

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

    public void send(CommandSender sender, CommandData cmdData) {
        if (this.isGeneric) {
            this.send(sender);
            return;
        }
        if (this.onlyOverloads) {
            for (CommandSpec key : cmdData.getOverloads().keySet()) {
                MutableText text = Chat.formatError(sender, "  [#cccccc]" + key.toString(), this.getName());
                sender.sendMessage(text);
            }
        } else {
            cmdData.sendUsage(this.getName(), sender);
        }
        if (this.index >= 0) {
            MutableText argErr = Chat.formatError(sender, "[light red]" + this.message + " - At argument " + this.index, this.getName());
            sender.sendMessage(argErr);
        } else {
            MutableText msgErr = Chat.formatError(sender, "[light red]" + this.message, this.getName());
            sender.sendMessage(msgErr);
        }
        MutableText msgCode = Chat.formatError(sender, "  [#cccccc]" + "Error code: " + this.messageCode.getCode() + " (" + this.messageCode + ")", this.getName());
        sender.sendMessage(msgCode);
    }

    protected void setGeneric() {
        this.isGeneric = true;
    }

    public void setOnlyOverloads(boolean onlyOverloads) {
        this.onlyOverloads = onlyOverloads;
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