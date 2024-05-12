package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Identifier;

public class MenuTypes {
    public static final MenuType<Inventory> INVENTORY = MenuTypes.register("inventory", (type, world, entity, pos) -> {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            return player.inventory;
        } else return null;
    });
    public static final MenuType<CrateMenu> CRATE = MenuTypes.register("crate", CrateMenu::new);

    private static <T extends ContainerMenu> MenuType<T> register(String name, MenuType.MenuBuilder<T> menuBuilder) {
        MenuType<T> menuType = new MenuType<>(menuBuilder);
        Registries.MENU_TYPE.register(new Identifier(name), menuType);
        return menuType;
    }
}
