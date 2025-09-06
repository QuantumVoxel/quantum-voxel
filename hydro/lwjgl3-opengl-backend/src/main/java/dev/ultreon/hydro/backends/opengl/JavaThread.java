package dev.ultreon.hydro.backends.opengl;

import dev.ultreon.hydro.InterruptionException;
import dev.ultreon.hydro.core.ThreadLike;

public class JavaThread extends Thread implements ThreadLike {
    public JavaThread(Runnable runnable) {
        super(runnable);
    }

    @Override
    public void joinThread() throws InterruptionException {
        try {
            join();
        } catch (InterruptedException e) {
            throw new InterruptionException(e.getMessage(), e);
        }
    }
}
