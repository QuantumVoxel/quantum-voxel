package dev.ultreon.quantum.groovy.testmod.block

import dev.ultreon.quantum.block.Block
import dev.ultreon.quantum.groovy.testmod.init.ModItems

class TestBlock extends Block {
    TestBlock() {
        super(new Properties().dropsItems(ModItems.TEST_ITEM))
    }
}
