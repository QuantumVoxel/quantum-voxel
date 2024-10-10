package dev.ultreon.gameprovider.quantum;

public class PlatformOS {
    public static boolean isWindows = System.getProperty("os.name").contains("Windows");
    public static boolean isLinux = System.getProperty("os.name").contains("Linux") || System.getProperty("os.name").contains("FreeBSD");
    public static boolean isMac = System.getProperty("os.name").contains("Mac");
    public static boolean isARM = System.getProperty("os.arch").startsWith("arm") || System.getProperty("os.arch").startsWith("aarch64");
    public static boolean is64Bit = System.getProperty("os.arch").contains("64") || System.getProperty("os.arch").startsWith("armv8");
    public static boolean isIos = false;
    public static boolean isAndroid = false;
}
