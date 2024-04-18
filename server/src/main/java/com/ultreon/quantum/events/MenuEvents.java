package com.ultreon.quantum.events;

import com.ultreon.quantum.events.api.Event;
import com.ultreon.quantum.events.api.EventResult;
import com.ultreon.quantum.menu.ContainerMenu;
import com.ultreon.quantum.menu.ItemSlot;
import com.ultreon.quantum.server.player.ServerPlayer;

public class MenuEvents {
    public static final Event<MenuClickEvent> MENU_CLICK = Event.withResult();
    public static final Event<MenuCloseEvent> MENU_CLOSE = Event.create();
    public static final Event<MenuOpenEvent> MENU_OPEN = Event.withResult();

    @FunctionalInterface
    public interface MenuClickEvent {
        EventResult onMenuClick(ContainerMenu menu, ServerPlayer player, ItemSlot slot, boolean rightClick);
    }

    @FunctionalInterface
    public interface MenuCloseEvent {
        void onMenuClose(ContainerMenu menu, ServerPlayer player);
    }

    @FunctionalInterface
    public interface MenuOpenEvent {
        EventResult onMenuOpen(ContainerMenu menu, ServerPlayer player);
    }
}
