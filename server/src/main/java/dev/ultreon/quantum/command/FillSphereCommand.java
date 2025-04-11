package dev.ultreon.quantum.command;

import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.world.ChunkAccess;
import dev.ultreon.quantum.world.WorldAccess;

public class FillSphereCommand extends Command {
    public FillSphereCommand() {
        data().aliases("fillsphere", "fsphere");
        this.requirePermission("quantum.commands.edit.fillsphere");
        this.setCategory(CommandCategory.EDIT);
    }

    @DefineCommand("<float>")
    public CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, float radius) {
        if (!(sender instanceof Player player)) return needPlayer();

        for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++) {
            for (int y = (int) Math.floor(-radius); y <= Math.ceil(radius); y++) {
                for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++) {
                    int distance = (int) Math.sqrt(x * x + y * y + z * z);
                    if (distance <= radius) {
                        WorldAccess world = player.getWorld();
                        world.set((int) (player.getX() + x), (int) (player.getY() + y), (int) (player.getZ() + z), Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        return successMessage("Successfully filled sphere of radius " + radius);
    }
}
