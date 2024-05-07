package dev.ultreon.quantum.scala.testmod.init

import dev.ultreon.quantum.item.Item
import dev.ultreon.quantum.registry.{DeferRegistry, Registries, DeferredElement}
import dev.ultreon.quantum.scala.testmod.Constants

object ModItems {
  private final val REGISTER = DeferRegistry.of(Constants.MOD_ID, Registries.ITEM)

  final val TEST_ITEM: DeferredElement[Item] = REGISTER.defer("test_item", { () =>
    new Item(new Item.Properties)
  })

  def register(): Unit = {
    REGISTER.register()
  }
}
