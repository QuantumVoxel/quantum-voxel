package com.ultreon.quantum.command;

import com.ultreon.quantum.api.commands.*;
import com.ultreon.quantum.api.commands.output.CommandResult;
import com.ultreon.quantum.entity.DroppedItem;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.item.Item;
import com.ultreon.libs.commons.v0.vector.Vec3d;
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
