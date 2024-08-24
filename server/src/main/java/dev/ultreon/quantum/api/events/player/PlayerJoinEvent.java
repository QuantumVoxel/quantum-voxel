package dev.ultreon.quantum.api.events.player;

import dev.ultreon.quantum.entity.player.Player;

public class PlayerJoinEvent extends PlayerEvent {
    public PlayerJoinEvent(Player entity) {
        super(entity);
    }
}
