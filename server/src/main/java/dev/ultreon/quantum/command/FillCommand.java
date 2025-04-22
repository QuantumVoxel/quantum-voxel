//package dev.ultreon.quantum.command;
//
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.block.Block;
//import dev.ultreon.quantum.entity.player.Player;
//import dev.ultreon.quantum.world.WorldAccess;
//
//public class FillCommand extends Command {
//    public FillCommand() {
//        data().aliases("set", "fill");
//        this.requirePermission("quantum.commands.edit.fill");
//        this.setCategory(CommandCategory.EDIT);
//    }
//
//    @DefineCommand("<block>")
//    public CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, Block block) {
//        if (!(sender instanceof Player)) return needPlayer();
//        Player player = (Player) sender;
//
//        WorldAccess world = player.getWorld();
//        int blocks = 0;
//        for (int x = Math.min(PositionCommand.FIRST_POSITION.x, PositionCommand.SECOND_POSITION.x); x <= Math.max(PositionCommand.FIRST_POSITION.x, PositionCommand.SECOND_POSITION.x); x++) {
//            for (int y = Math.min(PositionCommand.FIRST_POSITION.y, PositionCommand.SECOND_POSITION.y); y <= Math.max(PositionCommand.FIRST_POSITION.y, PositionCommand.SECOND_POSITION.y); y++) {
//                for (int z = Math.min(PositionCommand.FIRST_POSITION.z, PositionCommand.SECOND_POSITION.z); z <= Math.max(PositionCommand.FIRST_POSITION.z, PositionCommand.SECOND_POSITION.z); z++) {
//                    if (world.get(x, y, z).getBlock() == block) continue;
//                    world.set(x, y, z, block.getDefaultState());
//                    blocks++;
//                }
//            }
//        }
//
//        return successMessage("Successfully filled " + blocks + " blocks");
//    }
//}
