package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.ClientPlayer;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.events.api.EventResult;

public class ClientTickEvents {
    public static final Event<PreGameTick> PRE_GAME_TICK = Event.withResult();
    public static final Event<PostGameTick> POST_GAME_TICK = Event.create();
    public static final Event<PrePlayerTick> PRE_PLAYER_TICK = Event.withResult();
    public static final Event<PostPlayerTick> POST_PLAYER_TICK = Event.create();
    public static final Event<PreWorldTick> PRE_WORLD_TICK = Event.withResult();
    public static final Event<PostWorldTick> POST_WORLD_TICK = Event.create();

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
