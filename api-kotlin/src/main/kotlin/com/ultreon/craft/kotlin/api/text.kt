package dev.ultreon.quantum.kotlin.api

import dev.ultreon.quantum.text.TextObject

val String.literal: TextObject
    get() = TextObject.literal(this)

val String.translation: TextObject
    get() = TextObject.translation(this)

fun String.translation(vararg args: Any): TextObject {
    return TextObject.translation(this, *args)
}
