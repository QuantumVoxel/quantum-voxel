package dev.ultreon.hydro;

import dev.ultreon.hydro.concurrent.Timer;
import dev.ultreon.hydro.core.ThreadLike;
import dev.ultreon.hydro.engine.AudioEngine;
import dev.ultreon.hydro.engine.ComputeEngine;
import dev.ultreon.hydro.engine.GraphicsEngine;
import dev.ultreon.hydro.engine.InputEngine;
import dev.ultreon.hydro.util.Mat4;

public interface Engine {
    GraphicsEngine getGraphicsEngine();
    InputEngine getInputEngine();
    AudioEngine getAudioEngine();
    ComputeEngine getComputeEngine();

    void exit();
    void halt();

    void sleep(long millis);
    void yield();

    ThreadLike createThread(Runnable runnable);

    Timer createTimer();

    CompletablePromise<Void> createPromise();

}
