package dev.ultreon.hydro.concurrent;

import dev.ultreon.hydro.core.Destroyable;

import java.util.function.Consumer;

public interface Timer extends Destroyable {
    TimerHandle schedule(Consumer<TimerHandle> runnable, long delay);
    TimerHandle scheduleAtFixedRate(Consumer<TimerHandle> runnable, long delay, long period);
}
