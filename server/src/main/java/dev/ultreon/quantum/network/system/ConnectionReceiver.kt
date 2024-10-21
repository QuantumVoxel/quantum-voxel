package dev.ultreon.quantum.network.system

import java.io.Closeable
import java.io.IOException
import java.net.Socket

interface ConnectionReceiver : Closeable {
  @Throws(IOException::class)
  fun accept(): Socket?
}
