package dev.ultreon.quantum;

import com.badlogic.gdx.utils.Disposable;

public interface TimerInstance extends Disposable {

    void dispose();

    void schedule(TimerTask timerTask, long millis);

    void schedule(TimerTask timerTask, long millis, long interval);
}
