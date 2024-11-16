package dev.ultreon.quantum.world.container;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.Menu;

public interface Container<T extends ContainerMenu> extends Menu {
    T getMenu();

    void open(Player player);

    void onGainedViewer(Player player);

    void onLostViewer(Player player);
}
