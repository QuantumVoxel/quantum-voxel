package dev.ultreon.quantapi.kotlin.text

import dev.ultreon.quantum.text.LiteralText
import dev.ultreon.quantum.text.MutableText
import dev.ultreon.quantum.text.TextObject
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value

// Generic
val String.literal: LiteralText get() = TextObject.literal(this)

val String.translation: TextObject get() = TextObject.translation(this)

fun String.translate(vararg args: Any?): TextObject = TextObject.translation(this, *args)

val Any?.literal: TextObject
  get() = TextObject.nullToEmpty(this?.toString())

operator fun String.plus(text: TextObject): TextObject = TextObject.literal(this) + text

operator fun TextObject.plus(text: TextObject): TextObject = this.copy().append(text)
operator fun MutableText.plus(text: TextObject): TextObject = this.copy().append(text)
operator fun MutableText.plusAssign(text: TextObject) {
  this.append(text)
}
