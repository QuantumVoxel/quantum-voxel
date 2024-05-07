package dev.ultreon.quantum.groovy.testmod.init

import dev.ultreon.quantum.groovy.testmod.GroovyTestMod
import dev.ultreon.quantum.item.Item
import dev.ultreon.quantum.registry.DeferRegistry
import dev.ultreon.quantum.registry.Registries
import dev.ultreon.quantum.registry.DeferredElement

class ModItems {
    private static final def REGISTER = DeferRegistry.of(GroovyTestMod.MOD_ID, Registries.ITEM)

    static final DeferredElement<Item> TEST_ITEM = REGISTER.defer("test_item") { return new Item(new Item.Properties()) }

    static void register() {
        REGISTER.register()
    }
}
