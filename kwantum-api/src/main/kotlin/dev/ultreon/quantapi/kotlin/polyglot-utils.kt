package dev.ultreon.quantapi.kotlin

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import org.intellij.lang.annotations.Language

// Python
private val globalPyContext = Context.newBuilder("python")
  .allowAllAccess(true)
  .allowExperimentalOptions(true)
  .build()

val String.py: Value get() = globalPyContext.eval("python", this)

fun String.py(vararg args: Any?): Value = globalPyContext.eval("python", this).execute(*args)

// JS
private val globalJsContext = Context.newBuilder("js")
  .allowAllAccess(true)
  .allowExperimentalOptions(true)
  .build()

val String.js: Value get() = globalJsContext.eval("js", this)

fun String.js(vararg args: Any?): Value = globalJsContext.eval("js", this).execute(*args)

// Polyglot
inline fun <reified T> Value.invoke(vararg args: Any?): T = this.execute(*args).`as`(T::class.java)

inline fun <reified T> Value.invoke(): T = this.execute().`as`(T::class.java)

@JvmInline
value class Javascript(@Language("js") val code: String) {
  operator fun invoke(vararg args: Any?): Value = globalJsContext.eval("js", code).execute(*args)
}

@JvmInline
value class Python(@Language("python") val code: String) {
  operator fun invoke(vararg args: Any?): Value = globalPyContext.eval("python", code).execute(*args)
}
