package dev.ultreon.quantum.world.container;

import dev.ultreon.quantum.block.entity.CapabilityHolder;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.world.Audience;
import dev.ultreon.quantum.world.capability.CapabilityType;

public interface Container<T extends ContainerMenu> {
    T getMenu();

    void open(Player player);

    void onGainedViewer(Player player, T menu);

    void onLostViewer(Player player, T menu);
}
