package dev.ultreon.quantum.util;

public interface Shutdownable {
    void shutdown(Runnable finalizer) throws InterruptedException;
}
