package dev.ultreon.quantum.block;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.menu.AdvancedCraftingMenu;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a WorkbenchBlock, which is a type of Block that players can interact with
 * to open the advanced crafting menu.
 */
public class WorkbenchBlock extends Block {
    public WorkbenchBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull UseResult use(@NotNull WorldAccess world, @NotNull Player player, @NotNull Item item, @NotNull BlockVec pos) {
        if (world.isClientSide()) player.openMenu(new AdvancedCraftingMenu(player.getWorld(), player, pos, null));
        return UseResult.ALLOW;
    }
}
