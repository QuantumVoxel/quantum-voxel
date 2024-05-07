package dev.ultreon.quantum.groovy.testmod.init

import dev.ultreon.quantum.groovy.testmod.GroovyTestMod
import dev.ultreon.quantum.groovy.testmod.block.TestBlock
import dev.ultreon.quantum.registry.DeferRegistry
import dev.ultreon.quantum.registry.Registries
import dev.ultreon.quantum.registry.DeferredElement

class ModBlocks {
    private static final def REGISTER = DeferRegistry.of(GroovyTestMod.MOD_ID, Registries.BLOCK)

    static final DeferredElement<TestBlock> TEST_FUNCTIONAL_BLOCK = REGISTER.defer("test_functional_block") { return new TestBlock() }

    static void register() {
        REGISTER.register()
    }
}
