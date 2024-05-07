package dev.ultreon.quantum.scala.testmod

import dev.ultreon.quantum.scala.testmod.init.ModItems
import net.fabricmc.api.ModInitializer

class ScalaTestMod extends ModInitializer {
  override def onInitialize(): Unit = {
    ModItems.register()
  }
}
