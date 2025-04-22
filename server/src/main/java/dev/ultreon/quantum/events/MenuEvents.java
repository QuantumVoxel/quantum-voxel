package dev.ultreon.quantum.events;

import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.events.api.EventResult;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.server.player.ServerPlayer;

public class MenuEvents {
    public static final Event<MenuClickEvent> MENU_CLICK = Event.withResult(listeners ->(menu, player, slot, rightClick) -> {
        EventResult result = EventResult.pass();
        for (MenuClickEvent listener : listeners) {
            EventResult eventResult = listener.onMenuClick(menu, player, slot, rightClick);
            if (eventResult.isCanceled()) return eventResult;
            if (eventResult.isInterrupted()) result = eventResult;

        }
        return result;
    });
    public static final Event<MenuCloseEvent> MENU_CLOSE = Event.create(listeners -> (menu, player) -> {
        for (MenuCloseEvent listener : listeners) {
            listener.onMenuClose(menu, player);
        }
    });
    public static final Event<MenuOpenEvent> MENU_OPEN = Event.withResult(listeners -> (menu, player) -> {
        EventResult result = EventResult.pass();
        for (MenuOpenEvent listener : listeners) {
            EventResult eventResult = listener.onMenuOpen(menu, player);
            if (eventResult.isCanceled()) return eventResult;
            if (eventResult.isInterrupted()) result = eventResult;
        }
        return result;
    });

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
