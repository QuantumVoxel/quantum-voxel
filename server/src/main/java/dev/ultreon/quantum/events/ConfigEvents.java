package dev.ultreon.quantum.events;

import dev.ultreon.quantum.events.api.Event;
import net.fabricmc.api.EnvType;

public class ConfigEvents {
    public static final Event<Load> LOAD = Event.create();

    @FunctionalInterface
    public interface Load {
        void onConfigLoad(EnvType envType);
    }
}
