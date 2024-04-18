package com.ultreon.quantum.command;

import com.ultreon.quantum.api.commands.*;
import com.ultreon.quantum.api.commands.output.CommandResult;
import com.ultreon.quantum.entity.Player;
import org.jetbrains.annotations.Nullable;

public class InvincibleCommand extends Command {
    public InvincibleCommand() {
        this.requirePermission("quantum.commands.invincible");
        this.setCategory(CommandCategory.CHEATS);
        this.data().aliases("noDamage", "invincible");
    }

    @DefineCommand
    public @Nullable CommandResult executeCoords(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        player.setInvincible(!player.isInvincible());

        if (player.isInvincible()) return this.successMessage("Invincibility has been enabled!");
        else return this.successMessage("Invincibility has been disabled!");
    }

    @DefineCommand("<player>")
    public @Nullable CommandResult executeCoords(CommandSender sender, CommandContext commandContext, String alias, Player player) {
        player.setInvincible(!player.isInvincible());

        if (player.isInvincible()) return this.successMessage("Invincibility has been enabled!");
        else return this.successMessage("Invincibility has been disabled!");
    }

    @DefineCommand("<player> <boolean>")
    public @Nullable CommandResult executeCoords(CommandSender sender, CommandContext commandContext, String alias, Player player, boolean enable) {
        if (player.isInvincible() == enable) return this.errorMessage("Invincibility is already set to " + enable);

        player.setInvincible(enable);

        if (player.isInvincible()) return this.successMessage("Invincibility has been enabled!");
        else return this.successMessage("Invincibility has been disabled!");
    }
}
