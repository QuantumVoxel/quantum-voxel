package com.ultreon.quantum.menu;

import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.World;
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
