package dev.ultreon.quantum.item;

import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.text.TextStyle;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class ItemRarity {
    public static final ItemRarity COMMON = new ItemRarity(ColorCode.WHITE, "common");
    public static final ItemRarity UNCOMMON = new ItemRarity(ColorCode.YELLOW, "uncommon");
    public static final ItemRarity RARE = new ItemRarity(ColorCode.BLUE, "rare");
    public static final ItemRarity EPIC = new ItemRarity(ColorCode.ORANGE, "epic");
    public static final ItemRarity LEGENDARY = new ItemRarity(ColorCode.RED, "legendary");
    public static final ItemRarity MYTHIC = new ItemRarity(ColorCode.LIME, "mythic");
    public static final ItemRarity GODLIKE = new ItemRarity(ColorCode.DARK_PURPLE, "godlike");

    private final RgbColor color;
    private final String name;

    public ItemRarity(ColorCode color, Identifier name) {
        this.color = RgbColor.rgb(color.getColor());
        this.name = name.toString();
    }

    public ItemRarity(RgbColor color, Identifier name) {
        this.color = color;
        this.name = name.toString();
    }

    @ApiStatus.Internal
    public ItemRarity(ColorCode color, String name) {
        this.color = RgbColor.rgb(color.getColor());
        this.name = name;
    }

    public void applyStyle(TextStyle textStyle) {
        textStyle.color(color);
    }

    public String getName() {
        return name;
    }
}
