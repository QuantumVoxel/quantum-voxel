package com.ultreon.quantum.util;

public interface Shutdownable {
    void shutdown() throws InterruptedException;
}
