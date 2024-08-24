package dev.ultreon.quantum.command;

import dev.ultreon.ubo.types.MapType;
import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.api.ubo.UboFormatter;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.Nullable;

public class PlayerCommand extends Command {
    public PlayerCommand() {
        this.requirePermission("quantum.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);

        this.data().aliases("player", "person");
    }

    @DefineCommand("<player> dump-data")
    public @Nullable CommandResult executeDumpData(CommandSender sender, CommandContext commandContext, String alias, Player player) {
        MapType save = player.save(new MapType());

        TextObject formatted = UboFormatter.format(save);
        sender.sendMessage(TextObject.translation("quantum.commands.player.dumpData.success").append(formatted));

        return null;
    }

    @DefineCommand("<player>")
    public @Nullable CommandResult executeVar(CommandSender sender, CommandContext commandContext, String alias, Player player) {
        return objectResult(player);
    }
}
