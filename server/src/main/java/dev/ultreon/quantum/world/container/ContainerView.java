package dev.ultreon.quantum.world.container;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;

public interface ContainerView {
    void onItemChanged(int slot, ItemStack newStack);

    void onSlotClick(int slot, Player player, ContainerInteraction interaction);

    void onContainerClosed(Player player);

    boolean hasPlaceFor(ItemStack item);

    ItemStack moveInto(ItemStack item);
}
