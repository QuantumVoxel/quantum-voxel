package dev.ultreon.quantum.item.group;

import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemGroups {
    private static final List<ItemGroup> groups = new ArrayList<>();

    public static final ItemGroup TOOLS = register(new ItemGroup(TextObject.translation("quantum.groups.tools"), () -> null));
    public static final ItemGroup BUILDING_BLOCKS = register(new ItemGroup(TextObject.translation("quantum.groups.building_blocks"), () -> null));
    public static final ItemGroup DECORATIONS = register(new ItemGroup(TextObject.translation("quantum.groups.decorations"), () -> null));
    public static final ItemGroup COMBAT = register(new ItemGroup(TextObject.translation("quantum.groups.combat"), () -> null));
    public static final ItemGroup CONSUMABLES = register(new ItemGroup(TextObject.translation("quantum.groups.consumables"), () -> null));
    public static final ItemGroup MISC = register(new ItemGroup(TextObject.translation("quantum.groups.misc"), () -> null));

    public static ItemGroup register(ItemGroup group) {
        groups.add(group);
        return group;
    }

    @ApiStatus.Internal
    public static void clear() {
        groups.clear();
    }

    public static List<ItemGroup> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public static void init() {
        for (Item item : Registries.ITEM.values()) {
            item.fillItemGroup();
        }
    }
}
