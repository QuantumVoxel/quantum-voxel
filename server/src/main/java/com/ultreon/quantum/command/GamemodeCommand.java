package com.ultreon.quantum.command;

import com.ultreon.quantum.api.commands.*;
import com.ultreon.quantum.api.commands.output.CommandResult;
import com.ultreon.quantum.entity.player.Player;
import com.ultreon.quantum.server.chat.Chat;
import com.ultreon.quantum.util.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        this.requirePermission("quantum.commands.gamemode");
        this.setCategory(CommandCategory.CHEATS);
        this.data().aliases("gamemode", "gm");
    }

    @DefineCommand("<game-mode>")
    @Perm("quantum.commands.gamemode.self")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, GameMode gamemode) {
        if (!(sender instanceof Player target)) return this.needPlayer();

        target.setGameMode(gamemode);

        return this.successMessage("Gamemode set to %s".formatted(gamemode.name().toLowerCase(Locale.ROOT)));
    }

    @DefineCommand("<player> <game-mode>")
    public @Nullable CommandResult executeOnPlayer(CommandSender sender, CommandContext commandContext, String alias, Player target, GameMode gamemode) {
        if (sender != target && !sender.hasPermission("quantum.commands.gamemode.others")) return this.errorMessage("Cannot set gamemode for other players");

        target.setGameMode(gamemode);

        if (sender == target) return this.successMessage("Gamemode set to %s".formatted(gamemode.name().toLowerCase(Locale.ROOT)));
        Chat.sendInfo(target, "%s set your gamemode to %s".formatted(target.getName(), gamemode.name().toLowerCase(Locale.ROOT)));
        return this.successMessage("Gamemode set to %s for %s".formatted(gamemode.name().toLowerCase(Locale.ROOT), target.getName()));
    }
}
