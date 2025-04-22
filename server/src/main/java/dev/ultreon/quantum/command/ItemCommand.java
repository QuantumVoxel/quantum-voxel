//package dev.ultreon.quantum.command;
//
//import dev.ultreon.quantum.api.commands.*;
//import dev.ultreon.quantum.api.commands.output.CommandResult;
//import dev.ultreon.quantum.entity.Entity;
//import dev.ultreon.quantum.item.ItemStack;
//import org.jetbrains.annotations.Nullable;
//
//public class ItemCommand extends Command {
//    public ItemCommand() {
//        this.requirePermission("quantum.commands.item.edit");
//        this.setCategory(CommandCategory.EDIT);
//
//        this.data().description("Edit an item's properties");
//        this.data().aliases("item", "i");
//    }
//
//    @DefineCommand("<item-stack>")
//    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, ItemStack entity) {
//        return objectResult(entity);
//    }
//}
