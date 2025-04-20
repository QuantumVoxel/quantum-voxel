package dev.ultreon.quantum.api.commands.error;

import dev.ultreon.quantum.api.commands.MessageCode;

@Deprecated
public class InternalError extends CommandError {
    private final String name;

    public InternalError(String msg) {
        super(MessageCode.SERVER_ERROR, "An unknown internal error occurred: " + msg);
        this.setGeneric();
        this.name = "Internal";
    }

    @Override
    public String getName() {
        return this.name;
    }
}