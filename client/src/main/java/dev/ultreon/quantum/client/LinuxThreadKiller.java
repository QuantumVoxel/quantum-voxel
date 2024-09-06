package dev.ultreon.quantum.client;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface LinuxThreadKiller extends Library {
    LinuxThreadKiller INSTANCE = Native.load("c", LinuxThreadKiller.class);

    int pthread_kill(long thread, int signal);
}
