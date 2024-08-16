package dev.ultreon.quantum.command;

import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class WhereAmICommand extends Command {
    public WhereAmICommand() {
        this.requirePermission("quantum.commands.helloworld");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("whereAmI", "pos");
    }

    @DefineCommand
    public @Nullable CommandResult executeCoordsInWorld(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof Entity entity)) return this.needEntity();

        return this.infoMessage(String.format("You are at %s", entity.getBlockVec()));
    }
}
