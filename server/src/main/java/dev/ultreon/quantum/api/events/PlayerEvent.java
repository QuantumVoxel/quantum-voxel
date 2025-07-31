package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.api.events.entity.LivingEntityEvent;
import dev.ultreon.quantum.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface PlayerEvent extends LivingEntityEvent {
    @Nullable Player getEntity();
}
