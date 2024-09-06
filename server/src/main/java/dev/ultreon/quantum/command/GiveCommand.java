package dev.ultreon.quantum.command;

import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GiveCommand extends Command {
    public GiveCommand() {
        this.requirePermission("quantum.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("give", "g");
    }

    @DefineCommand("<player> <item>")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, Player player, Item item) {
        player.inventory.addItem(item.defaultStack());
        return successMessage("Gave " + player.getName() + " " + item.getTranslation().getText());
    }

    @DefineCommand("<player> <item> <int:count>")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, Player player, Item item, Integer count) {
        player.inventory.addItem(new ItemStack(item, count));
        return successMessage("Gave " + player.getName() + " " + count + "x " + item.getTranslation().getText());
    }
}
