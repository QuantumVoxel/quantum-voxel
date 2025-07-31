package dev.ultreon.quantum.api.events.tick;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.api.events.PlayerEvent;
import dev.ultreon.quantum.api.events.world.WorldEvent;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.Nullable;

public interface WorldTickEvent extends TickEvent, WorldEvent {

}
