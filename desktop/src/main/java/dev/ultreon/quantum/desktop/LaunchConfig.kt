package dev.ultreon.quantum.desktop

import oshi.SystemInfo

class LaunchConfig {
  @JvmField
  var maxMemoryMB: Long = 4096

  fun fix(systemInfo: SystemInfo) {
    if (maxMemoryMB <= 1024) maxMemoryMB = 4096

    if (maxMemoryMB >= systemInfo.hardware.memory.total / 1572864) {
      maxMemoryMB = systemInfo.hardware.memory.total / 1572864
    }
  }
}
