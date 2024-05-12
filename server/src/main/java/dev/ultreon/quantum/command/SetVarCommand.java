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
        this.data().aliases("setvar", "svar", "assign");
        this.requirePlayer();
    }

    @DefineCommand("<variable> set <value>")
    public @Nullable CommandResult executeSet(CommandSender sender, CommandContext commandContext, String alias, PlayerVariable variable, Object value) {
        variable.setValue(value);
        return new ObjectCommandResult(variable.getValue(), ObjectCommandResult.Type.Object);
    }

    @DefineCommand("<variable> run <command>")
    public @Nullable CommandResult executeRun(CommandSender sender, CommandContext commandContext, String alias, PlayerVariable variable, CommandResult result) {
        var player = (ServerPlayer) sender;

        if (result instanceof ObjectCommandResult) {
            ObjectCommandResult commandResult = (ObjectCommandResult) result;
            switch (commandResult.getType()) {
                case Object -> variable.setValue(commandResult.getObject());
                case Void -> variable.setValue(null);
            }
            return new ObjectCommandResult(variable.getValue(), variable.getValue() == null ? ObjectCommandResult.Type.Void : ObjectCommandResult.Type.Object);
        }

        return result;
    }
}
