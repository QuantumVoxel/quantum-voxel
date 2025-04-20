package dev.ultreon.quantum.api.neocommand;

public class CommandParseException extends Exception {
    public final int location;

    public CommandParseException(String message, int location) {
        super(message);
        this.location = location;
    }

    public CommandParseException(String message) {
        super(message);
        this.location = -1;
    }

    public int getLocation() {
        return this.location;
    }
}
