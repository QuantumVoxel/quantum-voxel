package dev.ultreon.quantum.item;

import dev.ultreon.quantum.text.ChatColor;
import dev.ultreon.quantum.text.TextStyle;
import dev.ultreon.quantum.util.Color;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class ItemRarity {
    public static final ItemRarity COMMON = new ItemRarity(ChatColor.WHITE, "common");
    public static final ItemRarity UNCOMMON = new ItemRarity(ChatColor.YELLOW, "uncommon");
    public static final ItemRarity RARE = new ItemRarity(ChatColor.BLUE, "rare");
    public static final ItemRarity EPIC = new ItemRarity(ChatColor.ORANGE, "epic");
    public static final ItemRarity LEGENDARY = new ItemRarity(ChatColor.RED, "legendary");
    public static final ItemRarity MYTHIC = new ItemRarity(ChatColor.LIME, "mythic");
    public static final ItemRarity GODLIKE = new ItemRarity(ChatColor.DARK_PURPLE, "godlike");

    private final Color color;
    private final String name;

    public ItemRarity(ChatColor color, Identifier name) {
        this.color = Color.rgb(color.getColor());
        this.name = name.toString();
    }

    public ItemRarity(Color color, Identifier name) {
        this.color = color;
        this.name = name.toString();
    }

    @ApiStatus.Internal
    public ItemRarity(ChatColor color, String name) {
        this.color = Color.rgb(color.getColor());
        this.name = name;
    }

    public void applyStyle(TextStyle textStyle) {
        textStyle.color(color);
    }

    public String getName() {
        return name;
    }
}
