package dev.ultreon.quantum.api.commands.error;

import dev.ultreon.quantum.api.commands.MessageCode;

public class NotFoundError extends CommandError {

    public NotFoundError(String what) {
        super(MessageCode.NOT_FOUND, "That " + what + " was not found!");
    }

    public NotFoundError(String what, int index) {
        super(MessageCode.NOT_FOUND, "That " + what + " was not found!", index);
    }

    @Override
    public String getName() {
        return "NotFound";
    }
}