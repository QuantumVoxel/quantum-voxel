package dev.ultreon.quantum.command;

import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.api.commands.output.ObjectCommandResult;
import dev.ultreon.quantum.api.commands.variables.PlayerVariable;
import dev.ultreon.quantum.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class SetVarCommand extends Command {
    public SetVarCommand() {
        this.requirePermission("quantum.commands.variable.set");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("set", "export", "assign");
        this.requirePlayer();
    }

    @DefineCommand("<variable> set <value>")
    public @Nullable CommandResult executeSet(CommandSender sender, CommandContext commandContext, String alias, PlayerVariable variable, Object value) {
        variable.setValue(value);
        return new ObjectCommandResult(variable.getValue(), ObjectCommandResult.Type.Object);
    }

    @DefineCommand("<variable> run <command>")
    public @Nullable CommandResult executeRun(CommandSender sender, CommandContext commandContext, String alias, PlayerVariable variable, CommandResult result) {
        if (result instanceof ObjectCommandResult commandResult) {
            switch (commandResult.getType()) {
                case Object:
                    variable.setValue(commandResult.getObject());
                    break;
                case Void:
                    variable.setValue(null);
                    break;
            }
            return new ObjectCommandResult(variable.getValue(), variable.getValue() == null ? ObjectCommandResult.Type.Void : ObjectCommandResult.Type.Object);
        }

        return errorMessage("Invalid command result");
    }
}
