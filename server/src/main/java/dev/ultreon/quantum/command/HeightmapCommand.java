//package dev.ultreon.quantum.command;
//
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.world.HeightmapType;
//import dev.ultreon.quantum.world.Location;
//import dev.ultreon.quantum.world.ServerWorld;
//import org.jetbrains.annotations.Nullable;
//
//public class HeightmapCommand extends Command {
//    public HeightmapCommand() {
//        this.requirePermission("quantum.commands.helloworld");
//        this.setCategory(CommandCategory.TELEPORT);
//        this.data().aliases("heightmap", "hm");
//    }
//
//    @DefineCommand
//    public @Nullable CommandResult executeCoordsInWorld(CommandSender sender, CommandContext commandContext, String alias) {
//        Location location = sender.getLocation();
//
//        ServerWorld severWorld = location.getServerWorld();
//        int height = severWorld.getHeight((int) location.x, (int) location.z, HeightmapType.MOTION_BLOCKING);
//        return this.infoMessage(String.format("The height at %s,%s is %s", location.getBlockVec().x, location.getBlockVec().z, height));
//    }
//}
