package dev.ultreon.quantum.desktop

import com.badlogic.gdx.files.FileHandle
import dev.ultreon.quantum.desktop.darwin.foundation.NSApplication
import dev.ultreon.quantum.desktop.darwin.foundation.NSImage
import java.io.IOException

object MacOSDockIcon {
  @Throws(IOException::class)
  fun setDockIcon(imagePath: FileHandle?) {
    val icon = NSImage(imagePath)

    val application = NSApplication.sharedApplication()
    application.applicationIconImage = icon
  }
}