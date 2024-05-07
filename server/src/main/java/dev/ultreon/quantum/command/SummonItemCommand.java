package dev.ultreon.quantum.command;

import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.entity.DroppedItem;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import org.jetbrains.annotations.Nullable;

public class SummonItemCommand extends Command {
    public SummonItemCommand() {
        this.requirePermission("quantum.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("summonItem", "si");
    }

    @DefineCommand("<item>")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, Item item) {
        if (sender instanceof Player player) {
            DroppedItem droppedItem = new DroppedItem(player.getWorld(), item.defaultStack(), player.getPosition(), new Vec3d());
            player.getWorld().spawn(droppedItem);
            return successMessage("Spawned " + droppedItem.getName() + " item");
        }

        return needPlayer();
    }
}
