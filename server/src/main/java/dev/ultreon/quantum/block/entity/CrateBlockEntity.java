package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.menu.CrateMenu;
import dev.ultreon.quantum.menu.MenuTypes;
import dev.ultreon.quantum.world.BlockVec;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

public class CrateBlockEntity extends ContainerBlockEntity<CrateMenu> {
    public static final int ITEM_CAPACITY = 27;

    public CrateBlockEntity(BlockEntityType<?> type, World world, BlockVec pos) {
        super(type, world, pos, ITEM_CAPACITY);
    }

    @Override
    public @NotNull CrateMenu createMenu(Player player) {
        return new CrateMenu(MenuTypes.CRATE, this.world, player, this, this.pos);
    }
}
