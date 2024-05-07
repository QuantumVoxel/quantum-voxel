package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.Nullable;

public class MenuType<T extends ContainerMenu> {
    private final MenuBuilder<T> menuBuilder;

    public MenuType(MenuBuilder<T> menuBuilder) {
        this.menuBuilder = menuBuilder;
    }

    public @Nullable T create(World world, Entity entity, @Nullable BlockPos pos) {
        return this.menuBuilder.create(this, world, entity, pos);
    }

    public Identifier getId() {
        return Registries.MENU_TYPE.getId(this);
    }

    @Override
    public String toString() {
        return "MenuType[" + this.getId() + "]";
    }

    public interface MenuBuilder<T extends ContainerMenu> {
        @Nullable T create(MenuType<T> menuType, World world, Entity entity, @Nullable BlockPos pos);
    }
}
