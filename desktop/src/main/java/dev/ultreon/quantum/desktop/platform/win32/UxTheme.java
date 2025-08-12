package dev.ultreon.quantum.desktop.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import dev.ultreon.quantum.desktop.AeroInsetDemo;

// UxTheme API
public interface UxTheme extends StdCallLibrary {
    UxTheme INSTANCE = Native.load("uxtheme", UxTheme.class);

    WinNT.HANDLE OpenThemeData(WinDef.HWND hwnd, WString pszClassList);

    int DrawThemeBackground(WinNT.HANDLE hTheme, WinDef.HDC hdc, int iPartId, int iStateId, RECT pRect, RECT pClipRect);

    int CloseThemeData(WinNT.HANDLE hTheme);
}
