package dev.ultreon.quantum.lwjgl2;

import dev.ultreon.quantum.OS;

public class StatusCode {
    public static int forAbort() {
        if (OS.isWindows()) {
            return 3;
        } else if (OS.isMac()) {
            return 6;
        } else if (OS.isLinux()) {
            return 6;
        }
        return -1;
    }

    public static int forException() {
        return OS.isWindows() ? -1 : 1;
    }
}
