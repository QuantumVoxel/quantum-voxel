package dev.ultreon.quantum.desktop

import dev.ultreon.quantum.OS

object StatusCode {
  @JvmStatic
  fun forAbort(): Int {
    if (OS.isWindows()) {
      return 3
    } else if (OS.isMac()) {
      return 6
    } else if (OS.isLinux()) {
      return 6
    }
    return -1
  }

  @JvmStatic
  fun forException(): Int {
    return if (OS.isWindows()) -1 else 1
  }
}
