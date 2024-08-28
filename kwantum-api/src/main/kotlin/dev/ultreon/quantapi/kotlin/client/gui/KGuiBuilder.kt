package dev.ultreon.quantapi.kotlin.client.gui

import dev.ultreon.quantum.client.gui.GuiBuilder
import dev.ultreon.quantum.client.gui.Screen
import dev.ultreon.quantum.client.gui.widget.TextButton

class KGuiBuilder(private val builder: GuiBuilder) {
  val screen: Screen = builder.screen()

  fun textButton(text: String, callback: TextButtonBuilder.() -> Unit) {
//    builder.add(TextButton.of(text).callback(callback))
  }
}
