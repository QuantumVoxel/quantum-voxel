package dev.ultreon.quantum.network.system

import com.esotericsoftware.minlog.Log
import dev.ultreon.quantum.log.Logger
import dev.ultreon.quantum.log.LoggerFactory

class KyroNetSlf4jLogger private constructor() : Log.Logger() {
  val logger: Logger = LoggerFactory.getLogger("KryoNet")

  override fun log(level: Int, category: String, message: String, ex: Throwable) {
    when (level) {
      com.esotericsoftware.kryo.kryo5.minlog.Log.LEVEL_INFO -> logger.info(message, ex)
      com.esotericsoftware.kryo.kryo5.minlog.Log.LEVEL_WARN -> logger.warn(message, ex)
      com.esotericsoftware.kryo.kryo5.minlog.Log.LEVEL_ERROR -> logger.error(message, ex)
      else -> logger.debug(message, ex)
    }
  }

  companion object {
    @JvmField
    val INSTANCE: Log.Logger = KyroNetSlf4jLogger()
  }
}
