package dev.ultreon.hydro.backends.opengl;

import java.util.TimerTask;

public class JavaTimerHandle implements dev.ultreon.hydro.concurrent.TimerHandle {

    private final TimerTask task;

    public JavaTimerHandle(TimerTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }
}
