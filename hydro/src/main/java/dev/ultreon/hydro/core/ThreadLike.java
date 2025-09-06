package dev.ultreon.hydro.core;

import dev.ultreon.hydro.InterruptionException;

public interface ThreadLike {
    void start();
    boolean isAlive();

    void joinThread() throws InterruptionException;
}
