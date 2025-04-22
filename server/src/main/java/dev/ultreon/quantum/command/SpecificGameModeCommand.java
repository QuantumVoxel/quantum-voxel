//package dev.ultreon.quantum.command;
//
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.entity.player.Player;
//import dev.ultreon.quantum.server.chat.Chat;
//import dev.ultreon.quantum.util.GameMode;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Locale;
//
//public class SpecificGameModeCommand extends Command {
//    private final GameMode gameMode;
//
//    public SpecificGameModeCommand(GameMode gameMode) {
//        this.gameMode = gameMode;
//        this.requirePermission("quantum.commands.gamemode");
//        this.setCategory(CommandCategory.CHEATS);
//        switch (gameMode) {
//            case SURVIVAL:
//                this.data().aliases("gms");
//                break;
//            case SPECTATOR:
//                this.data().aliases("gmsp");
//                break;
//            case BUILDER:
//                this.data().aliases("gmb");
//                break;
//            case BUILDER_PLUS:
//                this.data().aliases("gmbp");
//                break;
//            case ADVENTUROUS:
//                this.data().aliases("gma");
//                break;
//        }
//    }
//
//    @DefineCommand()
//    @Perm("quantum.commands.gamemode.self")
//    public @Nullable CommandResult execute(CommandSender sender, CommandContext ignoredCommandContext, String ignoredAlias) {
//        if (!(sender instanceof Player)) return this.needPlayer();
//        Player target = (Player) sender;
//
//        target.setGameMode(gameMode);
//
//        return this.successMessage(String.format("Gamemode set to %s", gameMode.name().toLowerCase(Locale.ROOT)));
//    }
//
//    @DefineCommand("<player>")
//    public @Nullable CommandResult executeOnPlayer(CommandSender sender, CommandContext ignoredCommandContext, String alias, Player target) {
//        if (sender != target && !sender.hasPermission("quantum.commands.gamemode.others")) return this.errorMessage("Cannot set gamemode for other players");
//
//        target.setGameMode(gameMode);
//
//        if (sender == target) return this.successMessage(String.format("Gamemode set to %s", gameMode.name().toLowerCase(Locale.ROOT)));
//        Chat.sendInfo(target, String.format("%s set your gamemode to %s", target.getName(), gameMode.name().toLowerCase(Locale.ROOT)));
//        return this.successMessage(String.format("Gamemode set to %s for %s", gameMode.name().toLowerCase(Locale.ROOT), target.getName()));
//    }
//}
