package dev.ultreon.quantum.api.commands;

import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.api.neocommand.SuccessCommandResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public class CompatCommandResult {
    public static @Nullable CommandResult wrap(@NotNull dev.ultreon.quantum.api.neocommand.CommandResult commandResult) {
        if (commandResult instanceof SuccessCommandResult) {
            return null;
        }
        return commandResult::send;
    }
}
