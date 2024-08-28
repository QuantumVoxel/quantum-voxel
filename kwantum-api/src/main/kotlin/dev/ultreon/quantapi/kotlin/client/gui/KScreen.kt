package dev.ultreon.quantapi.kotlin.client.gui

import dev.ultreon.quantum.client.gui.GuiBuilder
import dev.ultreon.quantum.client.gui.Screen
import dev.ultreon.quantum.text.TextObject

abstract class KScreen(text: TextObject? = null, parent: Screen? = null) : Screen(text, parent) {
  override fun build(builder: GuiBuilder) {
    KGuiBuilder(builder).create()
  }

  abstract fun KGuiBuilder.create();
}