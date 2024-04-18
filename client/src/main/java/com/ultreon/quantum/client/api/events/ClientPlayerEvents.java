package com.ultreon.quantum.client.api.events;

import com.ultreon.quantum.client.player.ClientPlayer;
import com.ultreon.quantum.events.api.Event;

public class ClientPlayerEvents {
    public static final Event<PlayerJoined> PLAYER_JOINED = Event.create();
    public static final Event<PlayerLeft> PLAYER_LEFT = Event.create();

    @FunctionalInterface
    public interface PlayerJoined {
        void onPlayerJoined(ClientPlayer clientPlayer);
    }

    @FunctionalInterface
    public interface PlayerLeft {
        void onPlayerLeft(ClientPlayer clientPlayer, String message);
    }
}
