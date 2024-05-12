package dev.ultreon.quantum.events;

import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.util.Env;

public class ConfigEvents {
    public static final Event<Load> LOAD = Event.create();

    @FunctionalInterface
    public interface Load {
        void onConfigLoad(Env Env);
    }
}
