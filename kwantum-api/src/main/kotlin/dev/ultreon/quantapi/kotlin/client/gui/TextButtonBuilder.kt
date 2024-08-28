package dev.ultreon.quantapi.kotlin.client.gui

import dev.ultreon.quantum.client.gui.widget.TextButton
import dev.ultreon.quantum.text.TextObject

class TextButtonBuilder(text: TextObject) {
  private val button: TextButton = TextButton.of(text)

  infix fun text(text: TextObject) = this.button.text().set(text)

  infix fun text(text: String) = this.button.text().set(text.literal)

  infix fun width(width: Int) = this.button.width(width)

  infix fun height(height: Int) = this.button.height(height)

  fun build() = this.button
}
