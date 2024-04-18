package com.ultreon.quantum.events;

import com.ultreon.quantum.events.api.Event;
import com.ultreon.quantum.item.Item;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.item.UseItemContext;
import org.jetbrains.annotations.ApiStatus;

public class ItemEvents {
    public static final Event<Use> USE = Event.create();

    @ApiStatus.Experimental
    public static final Event<Dropped> DROPPED = Event.create();

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
