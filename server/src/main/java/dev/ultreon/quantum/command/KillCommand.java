//package dev.ultreon.quantum.command;
//
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.entity.Entity;
//import dev.ultreon.quantum.entity.LivingEntity;
//import org.jetbrains.annotations.Nullable;
//
//public class KillCommand extends Command {
//    public KillCommand() {
//        this.requirePermission("quantum.commands.kill");
//        this.setCategory(CommandCategory.TELEPORT);
//        this.data().aliases("kill", "murder");
//    }
//
//    @DefineCommand
//    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias) {
//        if (!(sender instanceof LivingEntity)) return this.needLivingEntity();
//        LivingEntity player = (LivingEntity) sender;
//
//        player.kill();
//
//        return this.successMessage("You successfully killed yourself");
//    }
//
//    @DefineCommand("<entity>")
//    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, Entity entity) {
//        if (sender != entity && !sender.hasPermission("quantum.commands.kill.others")) return this.noPermission();
//        if (!(entity instanceof LivingEntity)) return errorMessage("Cannot kill " + entity.getName() + " because it is not a living entity");
//        LivingEntity livingEntity = (LivingEntity) entity;
//
//        livingEntity.kill();
//
//        if (sender == entity) return this.successMessage("You successfully killed yourself");
//        return this.successMessage("You successfully killed " + entity.getName());
//    }
//}
