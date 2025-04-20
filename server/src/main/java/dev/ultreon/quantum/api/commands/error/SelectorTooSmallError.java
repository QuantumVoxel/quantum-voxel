package dev.ultreon.quantum.api.commands.error;

import dev.ultreon.quantum.api.commands.MessageCode;

@Deprecated
public class SelectorTooSmallError extends CommandError {

    public SelectorTooSmallError(String got) {
        super(MessageCode.SELECTOR_TOO_SMALL,
                "Selector is too small, got \"" + got.replaceAll("\"", "\\\\\"") + "\"");
    }

    public SelectorTooSmallError(String got, int index) {
        super(MessageCode.SELECTOR_TOO_SMALL,
                "Selector is too small, got \"" + got.replaceAll("\"", "\\\\\"") + "\"",
                index);
    }

    @Override
    public String getName() {
        return "Invalid";
    }
}