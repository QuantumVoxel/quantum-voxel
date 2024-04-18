package com.ultreon.quantum.kotlin.dsl

import com.ultreon.quantum.client.gui.Callback
import com.ultreon.quantum.client.gui.widget.TextButton
import com.ultreon.quantum.text.TextObject
import com.ultreon.quantum.util.Color

class TextButtonDSL(var text: TextObject) {
    private var callback: Callback<TextButton> = Callback { }
    private var position: () -> PositionDSL? = { PositionDSL() }

    var textColor: Color = Color.rgb(1, 1, 1)

    infix fun position(dsl: PositionDSL.() -> Unit) {
        this.position = { PositionDSL().apply(dsl) }
    }

    infix fun text(text: TextObject) {
        this.text = text
    }

    infix fun text(text: String) {
        this.text = TextObject.literal(text)
    }

    infix fun color(color: Color) {
        this.textColor = color
    }

    fun clicked(dsl: TextButton.() -> Unit) {
        callback = Callback { button -> button.dsl() }
    }

    internal fun build(): TextButton = TextButton.of(text).position(position).apply {
        textColor().set(textColor)
    }
}