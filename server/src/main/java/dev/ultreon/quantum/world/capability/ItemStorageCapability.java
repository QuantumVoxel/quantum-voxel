package dev.ultreon.quantum.world.capability;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.ubo.types.MapType;

public class ItemStorageCapability extends ModifiableCapability<ItemStack> {
    public ItemStorageCapability() {
        super(Capabilities.ITEM_STORAGE, ItemStack.class);
    }

    public ItemStorageCapability(CapabilityType<? extends ItemStorageCapability, Array<ItemStack>> type) {
        super(type, ItemStack.class);
    }

    public ItemStorageCapability(Array<ItemStack> entries) {
        super(Capabilities.ITEM_STORAGE, entries, ItemStack.class);
    }

    @Override
    public MapType saveEntry(ItemStack entry, MapType data) {
        return entry.save();
    }

    @Override
    public ItemStack loadEntry(MapType data) {
        return ItemStack.load(data);
    }
}
