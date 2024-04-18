package com.ultreon.quantum.api.commands.selector;

import com.ultreon.quantum.api.commands.CommandContext;
import com.ultreon.quantum.api.commands.CommandSender;
import com.ultreon.quantum.api.commands.TabCompleting;
import com.ultreon.quantum.api.commands.error.InvalidIntegerError;
import com.ultreon.quantum.api.commands.error.NeedPlayerError;
import com.ultreon.quantum.api.commands.error.OverloadError;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.util.Numbers;

import java.util.ArrayList;

public class ItemBaseSelector extends BaseSelector<ItemStack> {
    private final CommandSender sender;

    public ItemBaseSelector(CommandSender sender, Parsed parsed) {
        super(parsed);
        this.sender = sender;
        this.result = this.calculateData();
    }

    public ItemBaseSelector(CommandSender sender, String text) {
        super(text);
        this.sender = sender;
        this.result = this.calculateData();
    }

    @Override
    protected Result<ItemStack> calculateData() {
        CommandSender player = this.sender;
        if (!(player instanceof Player)) {
            return new Result<>(null, new NeedPlayerError());
        }
        if (this.error != null) {
            return new Result<>(null, this.error);
        }
        switch (this.key) {
            case TAG:
                return switch (this.stringValue) {
                    case "selected" -> {
                        ItemStack targetMain = ((Player) player).getSelectedItem();
                        yield new Result<>(targetMain, null);
                    }
                    default -> new Result<>(null, new OverloadError());
                };

            case ID:
                Integer anInt = Numbers.toIntOrNull(this.stringValue);
                if (anInt == null) {
                    return new Result<>(null, new InvalidIntegerError());
                }
                ItemStack targetId = ((Player) player).inventory.getItem(anInt);
                return new Result<>(targetId, null);
        }
        return new Result<>(null, new OverloadError());
    }

    public static ArrayList<String> tabComplete(CommandSender sender, CommandContext commandCtx, String arg) {
        ArrayList<String> output = new ArrayList<>();
        TabCompleting.selectors(output, SelectorKey.TAG, arg, "mainHand", "offHand", "helmet", "chestplate", "leggings", "boots");
        TabCompleting.selectors(output, SelectorKey.ID, arg, TabCompleting.ints(new ArrayList<>(), arg.length() >= 1 ? arg.substring(1) : ""));
        return output;
    }
}