package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.api.events.world.WorldEvent;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.util.Hit;
import dev.ultreon.quantum.util.DVec3;
import dev.ultreon.quantum.world.World;

public interface ItemStackEvent {
    ItemStack getItemStack();

    class Use implements ItemStackEvent, PlayerEvent, WorldEvent {
        private final UseItemContext context;

        public Use(UseItemContext context) {
            this.context = context;
        }

        public UseItemContext getContext() {
            return context;
        }

        @Override
        public World getWorld() {
            return context.world();
        }

        @Override
        public ItemStack getItemStack() {
            return context.stack();
        }

        @Override
        public Player getEntity() {
            return context.player();
        }

        public float getAmount() {
            return context.amount();
        }

        public Hit getHit() {
            return context.hit();
        }
    }

    public class Drop implements ItemStackEvent, WorldEvent {
        private final ItemStack itemStack;
        private final DVec3 position;
        private final World world;

        public Drop(ItemStack itemStack, DVec3 position, World world) {
            this.itemStack = itemStack;
            this.position = position;
            this.world = world;
        }

        public World getWorld() {
            return world;
        }

        public DVec3 getPosition() {
            return position;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }
    }
}
