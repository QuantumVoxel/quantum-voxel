package com.ultreon.quantum.groovy.testmod.init

import com.ultreon.quantum.groovy.testmod.GroovyTestMod
import com.ultreon.quantum.item.Item
import com.ultreon.quantum.registry.DeferRegistry
import com.ultreon.quantum.registry.Registries
import com.ultreon.quantum.registry.DeferredElement

class ModItems {
    private static final def REGISTER = DeferRegistry.of(GroovyTestMod.MOD_ID, Registries.ITEM)

    static final DeferredElement<Item> TEST_ITEM = REGISTER.defer("test_item") { return new Item(new Item.Properties()) }

    static void register() {
        REGISTER.register()
    }
}
