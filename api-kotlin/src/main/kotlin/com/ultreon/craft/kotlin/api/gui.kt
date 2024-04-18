package com.ultreon.quantum.kotlin.api


import com.ultreon.quantum.client.gui.icon.Icon
import com.ultreon.quantum.client.gui.widget.IconButton
import com.ultreon.quantum.client.gui.widget.TextButton
import com.ultreon.quantum.kotlin.dsl.IconButtonDSL
import com.ultreon.quantum.kotlin.dsl.TextButtonDSL
import com.ultreon.quantum.text.TextObject

fun button(text: TextObject = TextObject.empty(), dsl: TextButtonDSL.() -> Unit): TextButton = TextButtonDSL(text).apply { dsl(this) }.build()

fun button(icon: Icon, dsl: IconButtonDSL.() -> Unit): IconButton = IconButtonDSL(icon).apply { dsl(this) }.build()
