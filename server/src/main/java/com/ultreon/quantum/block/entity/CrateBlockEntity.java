package com.ultreon.quantum.block.entity;

import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.menu.CrateMenu;
import com.ultreon.quantum.menu.MenuTypes;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

public class CrateBlockEntity extends ContainerBlockEntity<CrateMenu> {
    public static final int ITEM_CAPACITY = 27;

    public CrateBlockEntity(BlockEntityType<?> type, World world, BlockPos pos) {
        super(type, world, pos, ITEM_CAPACITY);
    }

    @Override
    public @NotNull CrateMenu createMenu(Player player) {
        return new CrateMenu(MenuTypes.CRATE, this.world, player, this, this.pos);
    }
}
