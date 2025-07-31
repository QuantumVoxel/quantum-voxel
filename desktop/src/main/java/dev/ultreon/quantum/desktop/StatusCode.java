package dev.ultreon.quantum.desktop;

import dev.ultreon.quantum.server.PlatformOS;

public class StatusCode {
    public static int forAbort() {
        if (PlatformOS.isWindows) {
            return 3;
        } else if (PlatformOS.isMac) {
            return 6;
        } else if (PlatformOS.isLinux) {
            return 6;
        }
        return -1;
    }

    public static int forException() {
        return PlatformOS.isWindows ? -1 : 1;
    }
}
