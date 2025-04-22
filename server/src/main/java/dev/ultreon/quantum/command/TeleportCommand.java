//package dev.ultreon.quantum.command;
//
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.entity.Entity;
//import dev.ultreon.quantum.entity.player.Player;
//import dev.ultreon.quantum.server.player.ServerPlayer;
//import dev.ultreon.quantum.util.Vec3d;
//import dev.ultreon.quantum.world.ServerWorld;
//import org.jetbrains.annotations.Nullable;
//
//public class TeleportCommand extends Command {
//    public TeleportCommand() {
//        this.requirePermission("quantum.commands.teleport");
//        this.setCategory(CommandCategory.TELEPORT);
//        this.data().aliases("teleport", "tp");
//    }
//
//    @DefineCommand("at <position>")
//    public @Nullable CommandResult executeCoords(CommandSender sender, CommandContext commandContext, String alias, Vec3d position) {
//        if (!(sender instanceof ServerPlayer)) return this.needPlayer();
//        ServerPlayer player = (ServerPlayer) sender;
//
//        player.teleportTo(position);
//
//        return this.successMessage("Teleported to " + position);
//    }
//
//    @DefineCommand("at <position> <world>")
//    public @Nullable CommandResult executeCoordsInWorld(CommandSender sender, CommandContext commandContext, String alias, Vec3d position, ServerWorld world) {
//        if (!(sender instanceof ServerPlayer)) return this.needPlayer();
//        ServerPlayer player = (ServerPlayer) sender;
//
//        player.teleportDimension(position, world);
//
//        return this.successMessage("Teleported to " + position + " in " + world.getDimension().id());
//    }
//
//    @DefineCommand("relative <position>")
//    public @Nullable CommandResult executeRelative(CommandSender sender, CommandContext commandContext, String alias, Vec3d offset) {
//        if (!(sender instanceof ServerPlayer)) return this.needPlayer();
//        ServerPlayer player = (ServerPlayer) sender;
//
//        player.teleportTo(player.getPosition().add(offset));
//
//        return this.successMessage("Teleported to " + player.getPosition());
//    }
//
//    @DefineCommand("relative <position> <world>")
//    public @Nullable CommandResult executeRelativeInWorld(CommandSender sender, CommandContext commandContext, String alias, Vec3d offset, ServerWorld world) {
//        if (!(sender instanceof ServerPlayer)) return this.needPlayer();
//        ServerPlayer player = (ServerPlayer) sender;
//
//        player.teleportDimension(player.getPosition().add(offset), world);
//
//        return this.successMessage("Teleported to " + player.getPosition() + " in " + world.getDimension().id());
//    }
//
//    @DefineCommand("to <entity>")
//    public @Nullable CommandResult executePlayer(CommandSender sender, CommandContext commandContext, String alias, Entity target) {
//        if (!(sender instanceof ServerPlayer)) return this.needPlayer();
//        ServerPlayer player = (ServerPlayer) sender;
//
//        player.teleportTo(target);
//
//        return this.successMessage("Teleported to " + target.getName());
//    }
//
//    @DefineCommand("entity <entity> to <player>")
//    public @Nullable CommandResult executeEntity(CommandSender sender, CommandContext commandContext, String alias, Entity source, Player target) {
//        if (!(sender instanceof ServerPlayer)) return this.needPlayer();
//        ServerPlayer player = (ServerPlayer) sender;
//
//        source.teleportTo(target);
//
//        return this.successMessage("Teleported " + source.getName() + " to " + target.getName());
//    }
//}
