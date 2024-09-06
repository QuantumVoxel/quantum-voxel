package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.events.api.Event;

public class ClientRegistrationEvents {
    public static final Event<Registration> ENTITY_MODELS = Event.create();
    public static final Event<Registration> ENTITY_RENDERERS = Event.create();
    public static final Event<Registration> BLOCK_RENDERERS = Event.create();
    public static final Event<Registration> BLOCK_RENDER_TYPES = Event.create();
    public static final Event<Registration> BLOCK_ENTITY_MODELS = Event.create();
    public static final Event<Registration> BLOCK_MODELS = Event.create();

    @FunctionalInterface
    public interface Registration {
        void onRegister();
    }
}
