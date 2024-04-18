package com.ultreon.quantum.groovy.testmod

import com.ultreon.quantum.ModInit
import com.ultreon.quantum.events.PlayerEvents
import com.ultreon.quantum.groovy.testmod.init.ModBlocks
import com.ultreon.quantum.groovy.testmod.init.ModItems
import com.ultreon.quantum.item.ItemStack

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
