package dev.ultreon.quantum.server;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.text.Formatter;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.world.Location;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class ConsoleCommandSender implements CommandSender {
    private final @Nullable QuantumServer server;

    public ConsoleCommandSender(@Nullable QuantumServer server) {
        this.server = server;
    }

    @Override
    public @NotNull Location getLocation() {
        return new Location(World.OVERWORLD, 0, 0, 0, 0, 0);
    }

    @Override
    public @NotNull String getName() {
        return "Console";
    }

    @Override
    public @Nullable String getPublicName() {
        return "[light red]Console";
    }

    @Override
    public @NotNull TextObject getDisplayName() {
        return Formatter.format(this.getPublicName());
    }

    @Override
    public @NotNull UUID getUuid() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    @Override
    public void sendMessage(@NotNull String message) {
        QuantumServer.LOGGER.info(Formatter.format(message).getText());
    }

    @Override
    public void sendMessage(@NotNull TextObject component) {
        QuantumServer.LOGGER.info(component.getText());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return true;
    }

    @Override
    public boolean hasExplicitPermission(@NotNull String permission) {
        return true;
    }

    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return true;
    }

    @Override
    public boolean isAdmin() {
        return true;
    }

    @Override
    public @Nullable QuantumServer getServer() {
        return server;
    }
}
