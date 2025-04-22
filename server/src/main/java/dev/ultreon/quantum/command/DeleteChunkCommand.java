//package dev.ultreon.quantum.command;
//
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.entity.player.Player;
//import dev.ultreon.quantum.server.chat.Chat;
//import dev.ultreon.quantum.world.ChunkAccess;
//
//public class DeleteChunkCommand extends Command {
//    public DeleteChunkCommand() {
//        data().aliases("deletechunk");
//        this.requirePermission("quantum.commands.edit.chunk.delete");
//        this.setCategory(CommandCategory.EDIT);
//    }
//
//    @DefineCommand
//    public CommandResult execute(CommandSender sender, CommandContext commandContext, String alias) {
//        if (!(sender instanceof Player)) return needPlayer();
//        Player player = (Player) sender;
//
//        ChunkAccess chunk = player.getWorld().getChunk(player.getChunkVec());
//        if (chunk == null) return errorMessage("Chunk not found");
//
//        chunk.reset();
//        return successMessage("Chunk deleted");
//    }
//}
