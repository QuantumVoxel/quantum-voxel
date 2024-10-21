package dev.ultreon.quantum.desktop

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.PointerType
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.ptr.IntByReference

interface Dwmapi : Library {
  fun DwmSetWindowAttribute(hwnd: HWND?, dwAttribute: UINT?, pvAttribute: PointerType?, cbAttribute: Int): Int

  companion object {
    @JvmStatic
    fun setUseImmersiveDarkMode(hwnd: HWND?, enabled: Boolean) {
      INSTANCE.DwmSetWindowAttribute(
        hwnd,
        UINT(DWMWA_USE_IMMERSIVE_DARK_MODE.toLong()),
        BOOLByReference(BOOL(enabled)),
        4
      )
    }

    @JvmStatic
    fun setAcrylicBackground(hwnd: HWND?) {
      INSTANCE.DwmSetWindowAttribute(hwnd, UINT(DWMWA_SYSTEMBACKDROP_TYPE.toLong()), IntByReference(3), 4)
    }

    @JvmStatic
    fun removeBorder(hwnd: HWND?) {
      setBorderColor(hwnd, 4294967294L)
    }

    fun setBorderColor(hwnd: HWND?, color: Long) {
      INSTANCE.DwmSetWindowAttribute(hwnd, UINT(DWMWA_BORDER_COLOR.toLong()), UINTByReference(UINT(color)), 4)
    }

    val INSTANCE: Dwmapi = Native.load("dwmapi", Dwmapi::class.java)

    const val DWMWA_USE_IMMERSIVE_DARK_MODE: Int = 20
    const val DWMWA_SYSTEMBACKDROP_TYPE: Int = 38
    const val DWMWA_BORDER_COLOR: Int = 34
  }
}