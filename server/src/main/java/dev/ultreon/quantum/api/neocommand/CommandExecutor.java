package dev.ultreon.quantum.api.neocommand;

@FunctionalInterface
public interface CommandExecutor {
    CommandResult execute(CommandContext context);
}
