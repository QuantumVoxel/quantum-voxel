package dev.ultreon.quantum.groovy.testmod

import dev.ultreon.quantum.ModInit
import dev.ultreon.quantum.events.PlayerEvents
import dev.ultreon.quantum.groovy.testmod.init.ModBlocks
import dev.ultreon.quantum.groovy.testmod.init.ModItems
import dev.ultreon.quantum.item.ItemStack

class GroovyTestMod implements ModInit {
    public static def MOD_ID = "groovy_testmod"

    @Override
    void onInitialize() {
        println("Hello from Groovy! Mod ID: ${MOD_ID}")

        ModBlocks.register()
        ModItems.register()

        PlayerEvents.INITIAL_ITEMS.subscribe {
            it.inventory.addItems([new ItemStack(ModItems.TEST_ITEM.get())])
        }
    }
}
