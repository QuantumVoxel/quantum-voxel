package dev.ultreon.quantum.command;

import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.server.chat.Chat;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.ServerChunk;

public class DebugCommand extends Command {
    public DebugCommand() {
        data().aliases("debug");

        this.requirePermission("quantum.commands.debug");
        this.setCategory(CommandCategory.EDIT);
    }

    @DefineCommand("chunk info")
    public CommandResult execute(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof ServerPlayer player)) return needPlayer();

        Chat.sendDebug(player, "Chunk at " + player.getLocation().getBlockVec() + ":");

        ServerChunk chunk = player.getWorld().getChunkNoLoad(player.getChunkVec());
        Chat.sendDebug(player, "Loaded: " + (chunk != null));
        if (chunk == null) return null;

        Chat.sendDebug(player, "Ready: " + chunk.isReady());
        Chat.sendDebug(player, "Tracking: " + chunk.getTracker().isTracking(player));
        Chat.sendDebug(player, "Tracked: " + chunk.isBeingTracked());
        Chat.sendDebug(player, "Build Duration: " + chunk.info.buildDuration);
        Chat.sendDebug(player, "Should Save: " + chunk.shouldSave());
        Chat.sendDebug(player, "Uniform: " + chunk.isUniform());
        Chat.sendDebug(player, "Active: " + chunk.isActive());
        Chat.sendDebug(player, "Original: " + chunk.isOriginal());
        Chat.sendDebug(player, "Block Entities: " + chunk.getBlockEntities().size());

        return null;
    }
}
