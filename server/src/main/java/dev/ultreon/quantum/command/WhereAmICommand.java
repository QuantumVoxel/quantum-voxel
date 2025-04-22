//package dev.ultreon.quantum.command;
//
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.world.Location;
//import org.jetbrains.annotations.Nullable;
//
//public class WhereAmICommand extends Command {
//    public WhereAmICommand() {
//        this.requirePermission("quantum.commands.helloworld");
//        this.setCategory(CommandCategory.TELEPORT);
//        this.data().aliases("whereAmI", "pos");
//    }
//
//    @DefineCommand
//    public @Nullable CommandResult executeCoordsInWorld(CommandSender sender, CommandContext commandContext, String alias) {
//        Location location = sender.getLocation();
//        if (location == null) return this.errorMessage("Failed to get location");
//        return this.infoMessage(String.format("You are at %s", location.getBlockVec()));
//    }
//}
