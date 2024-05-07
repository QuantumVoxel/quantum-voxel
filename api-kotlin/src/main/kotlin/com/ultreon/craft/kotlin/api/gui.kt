package dev.ultreon.quantum.kotlin.api


import dev.ultreon.quantum.client.gui.icon.Icon
import dev.ultreon.quantum.client.gui.widget.IconButton
import dev.ultreon.quantum.client.gui.widget.TextButton
import dev.ultreon.quantum.kotlin.dsl.IconButtonDSL
import dev.ultreon.quantum.kotlin.dsl.TextButtonDSL
import dev.ultreon.quantum.text.TextObject

fun button(text: TextObject = TextObject.empty(), dsl: TextButtonDSL.() -> Unit): TextButton = TextButtonDSL(text).apply { dsl(this) }.build()

fun button(icon: Icon, dsl: IconButtonDSL.() -> Unit): IconButton = IconButtonDSL(icon).apply { dsl(this) }.build()
