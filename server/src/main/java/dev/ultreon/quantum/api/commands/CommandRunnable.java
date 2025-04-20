package dev.ultreon.quantum.api.commands;

import dev.ultreon.quantum.api.commands.output.CommandResult;

@FunctionalInterface
@Deprecated
public interface CommandRunnable {
    CommandResult invoke(Object... objects);
}