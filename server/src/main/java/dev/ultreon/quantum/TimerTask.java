package dev.ultreon.quantum;

import org.jetbrains.annotations.ApiStatus;

public abstract class TimerTask implements Runnable {
    public int id;
    boolean cancelled;
    @ApiStatus.Internal
    public Runnable onCancelled;

    public abstract void run();

    public void cancel() {
        if (this.onCancelled != null) this.onCancelled.run();
        this.cancelled = true;
    }
}
