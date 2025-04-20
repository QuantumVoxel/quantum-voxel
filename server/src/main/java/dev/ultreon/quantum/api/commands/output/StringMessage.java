package dev.ultreon.quantum.api.commands.output;

import dev.ultreon.quantum.api.commands.CommandSender;

import java.util.Objects;

@Deprecated
public final class StringMessage implements CommandResult {
    private final String text;

    public StringMessage(String text) {
        this.text = text;
    }

    @Override
    public void send(CommandSender sender) {
        sender.sendMessage(this.text);
    }

    public String text() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StringMessage) obj;
        return Objects.equals(this.text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "StringMessage[" +
               "text=" + text + ']';
    }

}