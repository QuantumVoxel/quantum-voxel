package dev.ultreon.hydro.backends.opengl;

import dev.ultreon.hydro.concurrent.Timer;
import dev.ultreon.hydro.concurrent.TimerHandle;

import java.util.TimerTask;
import java.util.function.Consumer;

public class JavaTimer implements Timer {
    private final java.util.Timer timer = new java.util.Timer();

    @Override
    public TimerHandle schedule(Consumer<TimerHandle> runnable, long delay) {
        TimerHandle[] handle = new TimerHandle[1];
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runnable.accept(handle[0]);
            }
        };
        handle[0] = new JavaTimerHandle(task);
        timer.schedule(task, delay);
        return handle[0];
    }

    @Override
    public TimerHandle scheduleAtFixedRate(Consumer<TimerHandle> runnable, long delay, long period) {
        TimerHandle[] handle = new TimerHandle[1];
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runnable.accept(handle[0]);
            }
        };
        handle[0] = new JavaTimerHandle(task);
        timer.schedule(task, delay, period);
        return handle[0];
    }

    @Override
    public void destroy() {
        timer.cancel();
    }
}
