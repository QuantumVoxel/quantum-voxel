package dev.ultreon.quantum.api.commands;

import org.jetbrains.annotations.Nullable;

@Deprecated
public class CommandExecuteException extends Exception {
    public CommandExecuteException(@Nullable String message) {
        super(message);
    }
}