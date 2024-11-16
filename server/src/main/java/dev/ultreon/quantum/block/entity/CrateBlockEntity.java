package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.menu.CrateMenu;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.menu.MenuTypes;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CrateBlockEntity extends ContainerBlockEntity<CrateMenu> {
    public static final int ITEM_CAPACITY = 27;

    public CrateBlockEntity(BlockEntityType<?> type, World world, BlockVec pos) {
        super(type, world, pos, ITEM_CAPACITY);
    }

    public CrateBlockEntity(World world, BlockVec pos) {
        this(BlockEntityTypes.CRATE, world, pos);
    }

    @Override
    public @NotNull CrateMenu createMenu(Player player) {
        return new CrateMenu(MenuTypes.CRATE, this.world, player, this, this.pos);
    }

    @Override
    public List<ItemSlot> getInputs() {
        return List.of();
    }

    @Override
    public List<ItemSlot> getOutputs() {
        return List.of();
    }
}
