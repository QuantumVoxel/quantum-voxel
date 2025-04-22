package dev.ultreon.quantum.events;

import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.UseItemContext;
import org.jetbrains.annotations.ApiStatus;

public class ItemEvents {
    public static final Event<Use> USE = Event.create(listeners -> (item, context) -> {
        for (Use listener : listeners) {
            listener.onUseItem(item, context);
        }
    });

    @ApiStatus.Experimental
    public static final Event<Dropped> DROPPED = Event.create(listeners -> item -> {
        for (Dropped listener : listeners) {
            listener.onDropped(item);
        }
    });

    @FunctionalInterface
    public interface Use {
        void onUseItem(Item item, UseItemContext context);
    }

    @FunctionalInterface
    @ApiStatus.Experimental
    public interface Dropped {
        void onDropped(ItemStack item);
    }
}
