//package dev.ultreon.quantum.command;
//
//import com.badlogic.gdx.math.GridPoint2;
//import com.badlogic.gdx.math.GridPoint3;
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.entity.player.Player;
//
//public class PositionCommand extends Command {
//    public static final GridPoint3 FIRST_POSITION = new GridPoint3();
//    public static final GridPoint3 SECOND_POSITION = new GridPoint3();
//
//    public PositionCommand() {
//        data().aliases("position", "pos");
//        this.requirePermission("quantum.commands.edit.position");
//        this.setCategory(CommandCategory.EDIT);
//    }
//
//    @DefineCommand("1")
//    public CommandResult executeFirst(CommandSender sender, CommandContext commandContext, String alias) {
//        if (!(sender instanceof Player)) return needPlayer();
//        Player player = (Player) sender;
//
//        FIRST_POSITION.set((int) player.getX(), (int) player.getY(), (int) player.getZ());
//
//        return successMessage("First position set to " + FIRST_POSITION);
//    }
//
//    @DefineCommand("2")
//    public CommandResult executeSecond(CommandSender sender, CommandContext commandContext, String alias) {
//        if (!(sender instanceof Player)) return needPlayer();
//        Player player = (Player) sender;
//
//        SECOND_POSITION.set((int) player.getX(), (int) player.getY(), (int) player.getZ());
//
//        return successMessage("Second position set to " + SECOND_POSITION);
//    }
//}
