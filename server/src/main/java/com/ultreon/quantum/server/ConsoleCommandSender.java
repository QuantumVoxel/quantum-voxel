package com.ultreon.quantum.server;

import com.ultreon.quantum.api.commands.CommandSender;
import com.ultreon.quantum.api.commands.perms.Permission;
import com.ultreon.quantum.text.Formatter;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.world.Location;
import com.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ConsoleCommandSender implements CommandSender {
    @Override
    public @NotNull Location getLocation() {
        return new Location(World.OVERWORLD, 0, 0, 0, 0, 0);
    }

    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public @Nullable String getPublicName() {
        return "<red>Console";
    }

    @Override
    public TextObject getDisplayName() {
        return Formatter.format(this.getPublicName());
    }

    @Override
    public UUID getUuid() {
        return UUID.nameUUIDFromBytes("Hello Console".getBytes());
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
}
