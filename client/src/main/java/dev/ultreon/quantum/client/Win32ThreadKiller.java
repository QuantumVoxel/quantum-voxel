package dev.ultreon.quantum.client;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface Win32ThreadKiller extends Library {
    Win32ThreadKiller INSTANCE = Native.load("Kernel32", Win32ThreadKiller.class);

    boolean TerminateThread(long hThread, int dwExitCode);
}
