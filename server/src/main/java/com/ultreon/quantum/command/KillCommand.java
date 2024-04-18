package com.ultreon.quantum.command;

import com.ultreon.quantum.api.commands.*;
import com.ultreon.quantum.api.commands.output.CommandResult;
import com.ultreon.quantum.entity.LivingEntity;
import com.ultreon.quantum.entity.Player;
import org.jetbrains.annotations.Nullable;

public class KillCommand extends Command {
    public KillCommand() {
        this.requirePermission("quantum.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("kill", "murder");
    }

    @DefineCommand
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof LivingEntity player)) return this.needLivingEntity();

        player.kill();

        return this.successMessage("You successfully killed yourself");
    }

    @DefineCommand("<entity>")
    public @Nullable CommandResult executeCoords(CommandSender sender, CommandContext commandContext, String alias, Player player) {
        if (sender != player && !sender.hasPermission("quantum.commands.kill.others")) return this.noPermission();

        player.kill();

        if (sender == player) return this.successMessage("You successfully killed yourself");
        return this.successMessage("You successfully killed " + player.getName());
    }
}
