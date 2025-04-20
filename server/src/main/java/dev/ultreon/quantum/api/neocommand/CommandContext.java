package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.QuantumServer;

public record CommandContext(QuantumServer server, CommandSender sender, Argument<?>... args) {
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        for (Argument<?> arg : this.args) {
            if (arg.name().equals(name)) {
                return (T) arg.value();
            }
        }
        throw new IllegalArgumentException("No such argument: " + name);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int index) {
        return (T) this.args[index].value();
    }
}
