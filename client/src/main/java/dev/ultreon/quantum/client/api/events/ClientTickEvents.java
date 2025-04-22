package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.ClientPlayer;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.events.api.EventResult;

public class ClientTickEvents {
    public static final Event<PreGameTick> PRE_GAME_TICK = Event.withResult(listeners -> client -> {
        EventResult result = EventResult.pass();
        for (PreGameTick listener : listeners) {
            EventResult eventResult = listener.onGameTick(client);
            if (eventResult.isCanceled()) return eventResult;
            if (eventResult.isInterrupted()) result = eventResult;
        }
        return result;
    });
    public static final Event<PostGameTick> POST_GAME_TICK = Event.create(listeners -> client -> {
        for (PostGameTick listener : listeners) {
            listener.onGameTick(client);
        }
    });
    public static final Event<PrePlayerTick> PRE_PLAYER_TICK = Event.withResult(listeners -> player -> {
        EventResult result = EventResult.pass();
        for (PrePlayerTick listener : listeners) {
            EventResult eventResult = listener.onPlayerTick(player);
            if (eventResult.isCanceled()) return eventResult;
            if (eventResult.isInterrupted()) result = eventResult;
        }
        return result;
    });
    public static final Event<PostPlayerTick> POST_PLAYER_TICK = Event.create(listeners -> player -> {
        for (PostPlayerTick listener : listeners) {
            listener.onPlayerTick(player);
        }
    });
    public static final Event<PreWorldTick> PRE_WORLD_TICK = Event.withResult(listeners -> world -> {
        EventResult result = EventResult.pass();
        for (PreWorldTick listener : listeners) {
            EventResult eventResult = listener.onWorldTick(world);
            if (eventResult.isCanceled()) return eventResult;
            if (eventResult.isInterrupted()) result = eventResult;
        }
        return result;
    });
    public static final Event<PostWorldTick> POST_WORLD_TICK = Event.create(listeners -> world -> {
        for (PostWorldTick listener : listeners) {
            listener.onWorldTick(world);
        }
    });

    @FunctionalInterface
    public interface PreGameTick {
        EventResult onGameTick(QuantumClient client);
    }

    @FunctionalInterface
    public interface PostGameTick {
        void onGameTick(QuantumClient client);
    }

    @FunctionalInterface
    public interface PrePlayerTick {
        EventResult onPlayerTick(ClientPlayer player);
    }

    @FunctionalInterface
    public interface PostPlayerTick {
        void onPlayerTick(ClientPlayer player);
    }

    @FunctionalInterface
    public interface PreWorldTick {
        EventResult onWorldTick(ClientWorldAccess world);
    }

    @FunctionalInterface
    public interface PostWorldTick {
        void onWorldTick(ClientWorldAccess world);
    }
}
