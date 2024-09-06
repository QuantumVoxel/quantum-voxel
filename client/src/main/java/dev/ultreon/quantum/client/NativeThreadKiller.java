package dev.ultreon.quantum.client;

import dev.ultreon.quantum.GamePlatform;

public class NativeThreadKiller {
    public static void killThread(Thread thread) {
        if (GamePlatform.get().isWindows()) {
            Win32ThreadKiller.INSTANCE.TerminateThread(thread.threadId(), -1);
        } else if (GamePlatform.get().isLinux() || GamePlatform.get().isMacOSX()) {
            LinuxThreadKiller.INSTANCE.pthread_kill(thread.threadId(), 1);
        } else {
            throw new UnsupportedOperationException("Unsupported platform");
        }
    }
}
