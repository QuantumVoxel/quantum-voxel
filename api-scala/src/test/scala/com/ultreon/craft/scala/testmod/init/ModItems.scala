package com.ultreon.quantum.scala.testmod.init

import com.ultreon.quantum.item.Item
import com.ultreon.quantum.registry.{DeferRegistry, Registries, DeferredElement}
import com.ultreon.quantum.scala.testmod.Constants

object ModItems {
  private final val REGISTER = DeferRegistry.of(Constants.MOD_ID, Registries.ITEM)

  final val TEST_ITEM: DeferredElement[Item] = REGISTER.defer("test_item", { () =>
    new Item(new Item.Properties)
  })

  def register(): Unit = {
    REGISTER.register()
  }
}
