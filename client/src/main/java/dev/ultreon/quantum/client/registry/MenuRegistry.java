package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.client.gui.screens.container.ContainerScreen;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.MenuType;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the registry of menus.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class MenuRegistry {
    private static final Map<NamespaceID, ScreenBuilder<?>> REGISTRY = new HashMap<>();

    /**
     * Registers a screen for a menu.
     *
     * @param menu the menu.
     * @param builder the builder.
     */
    public static <T extends ContainerMenu> void registerScreen(MenuType<T> menu, ScreenBuilder<T> builder) {
        MenuRegistry.REGISTRY.put(menu.getId(), builder);
    }

    /**
     * Gets the screen for a menu.
     *
     * @param menu the menu.
     * @return the screen.
     */
    public static ContainerScreen getScreen(ContainerMenu menu) {
        return MenuRegistry.getScreen0(menu);
    }

    /**
     * Gets the screen for a menu.
     *
     * @param menu the menu.
     * @return the screen.
     */
    @SuppressWarnings("unchecked")
    private static <T extends ContainerMenu> ContainerScreen getScreen0(T menu) {
        ScreenBuilder<T> builder = (ScreenBuilder<T>) MenuRegistry.REGISTRY.get(menu.getType().getId());
        if (builder == null) return null;
        return builder.build(menu, menu.getTitle());
    }

    /**
     * Represents a builder for a screen.
     *
     * @param <T> the type of the menu.
     */
    public interface ScreenBuilder<T extends ContainerMenu> {
        ContainerScreen build(T menu, TextObject title);
    }
}
