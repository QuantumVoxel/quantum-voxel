package dev.ultreon.quantum.text;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

public final class ClickEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private final Action action;
    private final String value;

    public ClickEvent(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    public static ClickEvent openUri(URI url) {
        return new ClickEvent(Action.OPEN_URL, url.toString());
    }

    public static ClickEvent copyToClipboard(String text) {
        return new ClickEvent(Action.COPY_TO_CLIPBOARD, text);
    }

    public static ClickEvent runCommand(String text) {
        return new ClickEvent(Action.RUN_COMMAND, text);
    }

    public static ClickEvent suggestMessage(String text) {
        return new ClickEvent(Action.SUGGEST_MESSAGE, text);
    }

    public Action action() {
        return action;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClickEvent) obj;
        return Objects.equals(this.action, that.action) &&
               Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, value);
    }

    @Override
    public String toString() {
        return "ClickEvent[" +
               "action=" + action + ", " +
               "value=" + value + ']';
    }


    public enum Action {
        OPEN_URL,
        COPY_TO_CLIPBOARD,
        RUN_COMMAND,
        SUGGEST_MESSAGE,
    }
}
