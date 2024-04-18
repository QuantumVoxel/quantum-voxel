package com.ultreon.quantum.command;

import com.ultreon.quantum.api.commands.*;
import com.ultreon.quantum.api.commands.output.CommandResult;
import com.ultreon.quantum.api.ubo.UboFormatter;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.Nullable;

public class PlayerCommand extends Command {
    public PlayerCommand() {
        this.requirePermission("quantum.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("player", "person");
    }

    @DefineCommand("dump-data <player>")
    public @Nullable CommandResult executeDumpData(CommandSender sender, CommandContext commandContext, String alias, Player player) {
        MapType save = player.save(new MapType());

        TextObject formatted = UboFormatter.format(save);
        sender.sendMessage(TextObject.translation("quantum.commands.player.dumpData.success").append(formatted));

        return null;
    }
}
