package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class MenuTypes {
    public static final MenuType<Inventory> INVENTORY = MenuTypes.register("inventory", (type, world, entity, pos) -> {
        if (entity instanceof Player player) {
            return player.inventory;
        } else return null;
    });
    public static final MenuType<CrateMenu> CRATE = MenuTypes.register("crate", CrateMenu::new);
    public static final MenuType<BlastFurnaceMenu> BLAST_FURNACE = MenuTypes.register("blast_furnace", BlastFurnaceMenu::new);

    private static <T extends ContainerMenu> MenuType<T> register(String name, MenuType.MenuBuilder<T> menuBuilder) {
        MenuType<T> menuType = new MenuType<>(menuBuilder);
        Registries.MENU_TYPE.register(new NamespaceID(name), menuType);
        return menuType;
    }
}
