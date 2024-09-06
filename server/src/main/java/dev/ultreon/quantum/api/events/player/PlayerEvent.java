package dev.ultreon.quantum.api.events.player;

import dev.ultreon.quantum.api.events.entity.LivingEntityEvent;
import dev.ultreon.quantum.entity.player.Player;

public abstract class PlayerEvent extends LivingEntityEvent {
    public PlayerEvent(Player entity) {
        super(entity);
    }

    public Player getPlayer() {
        return (Player) this.getEntity();
    }
}
