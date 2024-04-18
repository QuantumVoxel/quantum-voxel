package io.github.xypercode.init;

import com.ultreon.quantum.item.Item;
import com.ultreon.quantum.registry.DeferRegistry;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.registry.DeferredElement;
import io.github.xypercode.TestMod;

public class ModItems {
    private static final DeferRegistry<Item> REGISTER = DeferRegistry.of(TestMod.MOD_ID, Registries.ITEM);

    public static final DeferredElement<Item> TEST_ITEM = REGISTER.defer("test_item", () -> new Item(new Item.Properties()));

    public static void register() {
        REGISTER.register();
    }
}
