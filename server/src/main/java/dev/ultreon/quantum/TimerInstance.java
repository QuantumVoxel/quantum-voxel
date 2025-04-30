package dev.ultreon.quantum;

public interface TimerInstance {

    void cancel();

    void schedule(TimerTask timerTask, long millis);

    void schedule(TimerTask timerTask, long millis, long interval);
}
