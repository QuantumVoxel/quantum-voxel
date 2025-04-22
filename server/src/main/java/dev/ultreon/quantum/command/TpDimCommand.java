//package dev.ultreon.quantum.command;
//
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.entity.player.Player;
//import dev.ultreon.quantum.util.Vec3d;
//import dev.ultreon.quantum.world.ServerWorld;
//import org.jetbrains.annotations.Nullable;
//
//public class TpDimCommand extends Command {
//    public TpDimCommand() {
//        super();
//
//        this.requirePermission("quantum.commands.tpdim");
//        this.setCategory(CommandCategory.TELEPORT);
//        this.data().aliases("tpdim");
//    }
//
//    @DefineCommand("<world>")
//    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, ServerWorld world) {
//        if (!(sender instanceof Player)) return needPlayer();
//        Player player = (Player) sender;
//
//        int height = world.getHeight(0, 0);
//        player.teleportDimension(new Vec3d(0, height, 0), world);
//
//        return successMessage("Successfully teleported to " + world.getDimension().id());
//    }
//}
