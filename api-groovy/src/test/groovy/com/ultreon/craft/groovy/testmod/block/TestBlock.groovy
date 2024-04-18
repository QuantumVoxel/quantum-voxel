package com.ultreon.quantum.groovy.testmod.block

import com.ultreon.quantum.block.Block
import com.ultreon.quantum.groovy.testmod.init.ModItems

class TestBlock extends Block {
    TestBlock() {
        super(new Properties().dropsItems(ModItems.TEST_ITEM))
    }
}
