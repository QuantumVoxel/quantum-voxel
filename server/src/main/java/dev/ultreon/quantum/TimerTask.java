package dev.ultreon.quantum;

import groovy.transform.Internal;

public abstract class TimerTask implements Runnable {
    public int id;
    boolean cancelled;
    @Internal
    public Runnable onCancelled;

    public abstract void run();

    public void cancel() {
        if (this.onCancelled != null) this.onCancelled.run();
        this.cancelled = true;
    }
}
