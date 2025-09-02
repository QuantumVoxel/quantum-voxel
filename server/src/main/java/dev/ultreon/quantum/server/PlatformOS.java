package dev.ultreon.quantum.server;

import dev.ultreon.quantum.GamePlatform;

public class PlatformOS {
    public static boolean isWindows = GamePlatform.get().isWindows();
    public static boolean isLinux = GamePlatform.get().isLinux();
    public static boolean isMac = GamePlatform.get().isMacOSX();
    public static boolean isIos = GamePlatform.get().isIOS();
    public static boolean isAndroid = GamePlatform.get().isAndroid();
    public static boolean isWeb = GamePlatform.get().isWeb();
    public static boolean isHeadless = GamePlatform.get().isHeadless();
    public static boolean isDesktop = GamePlatform.get().isDesktop();
    public static boolean isMobile = GamePlatform.get().isMobile();
    public static boolean isServer = GamePlatform.get().isServer();
    public static boolean isClient = GamePlatform.get().isClient();
    public static boolean isSwitch = GamePlatform.get().isSwitch();
    public static boolean isXbox = GamePlatform.get().isXbox();
    public static boolean isSwitchGDX = GamePlatform.get().isSwitchGDX();
}
