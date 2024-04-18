package com.ultreon.quantum.command;

import com.ultreon.quantum.api.commands.*;
import com.ultreon.quantum.api.commands.output.CommandResult;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.network.packets.s2c.S2CTimePacket;
import com.ultreon.quantum.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class TimeCommand extends Command {
    public TimeCommand() {
        this.requirePermission("quantum.commands.gamemode");
        this.setCategory(CommandCategory.CHEATS);
        this.data().aliases("time", "t");
    }

    @DefineCommand("add <int>")
    public @Nullable CommandResult executeAdd(CommandSender sender, CommandContext commandContext, String alias, int time) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.connection.send(new S2CTimePacket(S2CTimePacket.Operation.ADD, time));
        }

        return this.successMessage("Added time: " + time);
    }

    @DefineCommand("set <int>")
    public @Nullable CommandResult executeSet(CommandSender sender, CommandContext commandContext, String alias, int time) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.connection.send(new S2CTimePacket(S2CTimePacket.Operation.SET, time));
        }

        return this.successMessage("Set time: " + time);
    }

    @DefineCommand("sub <int>")
    public @Nullable CommandResult executeSubtract(CommandSender sender, CommandContext commandContext, String alias, int time) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.connection.send(new S2CTimePacket(S2CTimePacket.Operation.SUB, time));
        }

        return this.successMessage("Subtracted time: " + time);
    }
}
