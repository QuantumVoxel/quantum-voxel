package dev.ultreon.quantum.command;

import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.server.chat.Chat;
import dev.ultreon.quantum.util.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@SuppressWarnings("deprecation")
public class GameModeCommand extends Command {
    public GameModeCommand() {
        this.requirePermission("quantum.commands.gamemode");
        this.setCategory(CommandCategory.CHEATS);
        this.data().aliases("gamemode", "gm");
    }

    @DefineCommand("<game-mode>")
    @Perm("quantum.commands.gamemode.self")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, GameMode gamemode) {
        if (!(sender instanceof Player target))
            return this.needPlayer();

        target.setGameMode(gamemode);
        return this.successMessage(String.format("Gamemode set to %s", gamemode.name().toLowerCase(Locale.ROOT)));
    }

    @DefineCommand("<player> <game-mode>")
    public @Nullable CommandResult executeOnPlayer(CommandSender sender, CommandContext commandContext, String alias, Player target, GameMode gamemode) {
        if (sender != target && !sender.hasPermission("quantum.commands.gamemode.others"))
            return this.errorMessage("Cannot set gamemode for other players");

        target.setGameMode(gamemode);

        if (sender == target)
            return this.successMessage(String.format("Gamemode set to %s", gamemode.name().toLowerCase(Locale.ROOT)));

        Chat.sendInfo(target, String.format("%s set your gamemode to %s", target.getName(), gamemode.name().toLowerCase(Locale.ROOT)));
        return this.successMessage(String.format("Gamemode set to %s for %s", gamemode.name().toLowerCase(Locale.ROOT), target.getName()));
    }
}
