package dev.ultreon.quantum.scala.testmod.init

import dev.ultreon.quantum.block.Block
import dev.ultreon.quantum.item.ItemStack
import dev.ultreon.quantum.registry.{DeferRegistry, DeferredElement, Registries}
import dev.ultreon.quantum.scala.testmod.Constants

import java.util.function.Supplier
import scala.language.postfixOps

object ModBlocks {
  private final val REGISTER = DeferRegistry.of(Constants.MOD_ID, Registries.BLOCK)

  final val TEST_BLOCK: DeferredElement[Block] = REGISTER.defer("test_block", { () =>
    new Block(new Block.Properties().dropsItems(new ItemStack(ModItems.TEST_ITEM.get())))
  })

  def register(): Unit = {
    REGISTER.register()
  }
}
