package dev.ultreon.quantum.api.commands;

import dev.ultreon.quantum.api.commands.output.CommandResult;

@FunctionalInterface
public interface CommandRunnable {
    CommandResult invoke(Object... objects);
}