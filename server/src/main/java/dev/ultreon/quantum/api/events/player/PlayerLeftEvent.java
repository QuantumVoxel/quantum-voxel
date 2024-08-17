package dev.ultreon.quantum.api.events.player;

import dev.ultreon.quantum.entity.player.Player;

public class PlayerLeftEvent extends PlayerEvent {
    private final String message;

    public PlayerLeftEvent(Player entity, String message) {
        super(entity);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
