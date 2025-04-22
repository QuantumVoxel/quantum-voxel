//package dev.ultreon.quantum.command;
//
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.entity.Entity;
//import org.jetbrains.annotations.Nullable;
//
//public class EntityCommand extends Command {
//    public EntityCommand() {
//        this.requirePermission("quantum.commands.entity");
//        this.setCategory(CommandCategory.EDIT);
//
//        this.data().aliases("entity");
//    }
//
//    @DefineCommand("<entity>")
//    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, Entity entity) {
//        return objectResult(entity);
//    }
//}
