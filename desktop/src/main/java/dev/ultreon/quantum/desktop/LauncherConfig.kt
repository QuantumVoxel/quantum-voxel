package dev.ultreon.quantum.desktop

import de.marhali.json5.Json5
import de.marhali.json5.Json5Object
import de.marhali.json5.Json5OptionsBuilder
import de.marhali.json5.exception.Json5Exception
import dev.ultreon.mixinprovider.PlatformOS
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class LauncherConfig private constructor() {
  var schemaVersion: Int = 1
  var windowVibrancyEnabled: Boolean = PlatformOS.isWindows
  var enableFullVibrancy: Boolean = false
  var useAngleGraphics: Boolean = PlatformOS.isWindows
  var frameless: Boolean = false
  var removeBorder: Boolean = false

  companion object {
    private val JSON5: Json5 = Json5.builder { it: Json5OptionsBuilder ->
      it
        .indentFactor(2)
        .quoteless()
        .prettyPrinting().build()
    }
    private var instance: LauncherConfig? = null
    private fun load(): LauncherConfig {
      var config: LauncherConfig
      try {
        val json = JSON5.parse(Files.readString(Path.of("config.json5"))).asJson5Object
        val version = json.getAsJson5Primitive("schemaVersion").asInt
        config = LauncherConfig()
        if (version == 1) {
          config.schemaVersion = version
          config.windowVibrancyEnabled = json.getAsJson5Primitive("windowVibrancyEnabled").asBoolean
          config.enableFullVibrancy = json.getAsJson5Primitive("enableFullVibrancy").asBoolean
          config.useAngleGraphics = json.getAsJson5Primitive("useAngleGraphics").asBoolean
          config.frameless = json.getAsJson5Primitive("frameless").asBoolean
          config.removeBorder = json.getAsJson5Primitive("removeBorder").asBoolean
        } else {
          config.schemaVersion = 1
          config.windowVibrancyEnabled = true
          config.enableFullVibrancy = false
          config.useAngleGraphics = true
          config.frameless = false
          config.removeBorder = false
        }
      } catch (e: IOException) {
        config = LauncherConfig()
      } catch (e: Json5Exception) {
        config = LauncherConfig()
      } catch (e: NullPointerException) {
        config = LauncherConfig()
      }
      instance = Objects.requireNonNullElseGet(
        config
      ) { LauncherConfig() }

      return config
    }

    fun get(): LauncherConfig {
      return instance ?: load()
    }

    fun save() {
      val json = Json5Object()
      json.addProperty("schemaVersion", 1)
      json.setComment(
        "schemaVersion",
        "Version of the launcher config file.\nThis would be incremented every time the config changes."
      )

      json.addProperty("windowVibrancyEnabled", get().windowVibrancyEnabled)
      json.setComment(
        "windowVibrancyEnabled",
        "Whether the window should be vibrancy enabled.\nThis is only supported on Windows.\nOn by default"
      )

      json.addProperty("enableFullVibrancy", get().enableFullVibrancy)
      json.setComment(
        "enableFullVibrancy",
        "Whether to enable full vibrancy.\nThis is only supported on Windows.\nOff by default"
      )

      json.addProperty("useAngleGraphics", get().useAngleGraphics)
      json.setComment(
        "useAngleGraphics",
        "Whether to use ANGLE graphics.\nThis is only supported on Windows.\nOn by default for performance."
      )

      json.addProperty("frameless", get().frameless)
      json.setComment(
        "frameless",
        "Whether the window should be frameless.\nThis is only supported on Windows for now.\nOff by default"
      )

      json.addProperty("removeBorder", get().removeBorder)
      json.setComment(
        "removeBorder",
        "Whether the border should be removed.\nThis is only supported on Windows for now.\nOff by default"
      )

      try {
        Files.writeString(Path.of("config.json5"), JSON5.serialize(json))
      } catch (e: IOException) {
        DesktopLauncher.LOGGER.warn("Failed to save launcher config", e)
      }
    }
  }
}
