package dev.ultreon.quantum.command;

import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class FlyCommand extends Command {
    public FlyCommand() {
        this.requirePermission("quantum.commands.flight");
        this.setCategory(CommandCategory.CHEATS);
        this.data().aliases("allowFlight", "fly");
    }

    @DefineCommand
    @Perm("quantum.commands.flight.self")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        player.setAllowFlight(!player.isAllowFlight());

        if (player.isAllowFlight()) return this.successMessage("Flight has been enabled!");
        else return this.successMessage("Flight has been disabled!");
    }

    @DefineCommand("<player>")
    public @Nullable CommandResult executeOnPlayer(CommandSender sender, CommandContext commandContext, String alias, Player player) {
        if (player != sender && sender.hasPermission("quantum.commands.flight.others")) return this.errorMessage("You cannot use this command on others");

        player.setAllowFlight(!player.isAllowFlight());

        if (player.isAllowFlight()) return this.successMessage("Flight has been enabled!");
        else return this.successMessage("Flight has been disabled!");
    }

    @DefineCommand("<player> <boolean>")
    public @Nullable CommandResult executeOnPlayer(CommandSender sender, CommandContext commandContext, String alias, Player player, boolean enable) {
        if (player != sender && sender.hasPermission("quantum.commands.flight.others")) return this.errorMessage("You cannot use this command on others");

        if (player.isAllowFlight() == enable) return this.errorMessage("Flight is already set to " + enable);

        player.setAllowFlight(enable);

        if (player.isAllowFlight()) return this.successMessage("Flight has been enabled!");
        else return this.successMessage("Flight has been disabled!");
    }
}
