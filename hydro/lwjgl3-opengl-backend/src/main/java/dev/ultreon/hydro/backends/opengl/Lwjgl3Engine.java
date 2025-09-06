package dev.ultreon.hydro.backends.opengl;

import dev.ultreon.hydro.CompletablePromise;
import dev.ultreon.hydro.Engine;
import dev.ultreon.hydro.backends.opengl.engines.GLFWWindow;
import dev.ultreon.hydro.backends.opengl.engines.GLGraphicsEngine;
import dev.ultreon.hydro.backends.opengl.engines.ALAudioEngine;
import dev.ultreon.hydro.concurrent.Timer;
import dev.ultreon.hydro.core.Application;
import dev.ultreon.hydro.core.HydroSettings;
import dev.ultreon.hydro.core.ThreadLike;
import dev.ultreon.hydro.engine.AudioEngine;
import dev.ultreon.hydro.engine.ComputeEngine;
import dev.ultreon.hydro.engine.GraphicsEngine;
import dev.ultreon.hydro.engine.InputEngine;

public class Lwjgl3Engine implements Engine {
    private final GLFWWindow window;
    private final ALAudioEngine audioEngine;

    public Lwjgl3Engine(HydroSettings settings, Application app) {
        GLFWWindow.init();
        window = new GLFWWindow(settings, app);
        audioEngine = new ALAudioEngine();
    }

    @Override
    public GraphicsEngine getGraphicsEngine() {
        return new GLGraphicsEngine();
    }

    @Override
    public InputEngine getInputEngine() {
        return window.getInputEngine();
    }

    @Override
    public AudioEngine getAudioEngine() {
        return audioEngine;
    }

    @Override
    public ComputeEngine getComputeEngine() {
        return null;
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public void halt() {
        Runtime.getRuntime().halt(1);
    }

    @Override
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void yield() {
        Thread.yield();
    }

    @Override
    public ThreadLike createThread(Runnable runnable) {
        return new JavaThread(runnable);
    }

    @Override
    public Timer createTimer() {
        return new JavaTimer();
    }

    @Override
    public CompletablePromise<Void> createPromise() {
        return null;
    }
}
