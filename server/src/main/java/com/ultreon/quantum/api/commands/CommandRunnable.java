package com.ultreon.quantum.api.commands;

import com.ultreon.quantum.api.commands.output.CommandResult;

@FunctionalInterface
public interface CommandRunnable {
    CommandResult invoke(Object... objects);
}