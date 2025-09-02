package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.QuantumServer;

import java.util.Objects;

public final class CommandContext {
    private final QuantumServer server;
    private final CommandSender sender;
    private final Argument<?>[] args;

    public CommandContext(QuantumServer server, CommandSender sender, Argument<?>... args) {
        this.server = server;
        this.sender = sender;
        this.args = args;
    }

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

    public QuantumServer server() {
        return server;
    }

    public CommandSender sender() {
        return sender;
    }

    public Argument<?>[] args() {
        return args;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CommandContext) obj;
        return Objects.equals(this.server, that.server) &&
               Objects.equals(this.sender, that.sender) &&
               Objects.equals(this.args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, sender, args);
    }

    @Override
    public String toString() {
        return "CommandContext[" +
               "server=" + server + ", " +
               "sender=" + sender + ", " +
               "args=" + args + ']';
    }

}
