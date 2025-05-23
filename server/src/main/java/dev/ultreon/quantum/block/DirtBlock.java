package dev.ultreon.quantum.block;

import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.entity.Pig;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public class DirtBlock extends Block {
    public DirtBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull UseResult use(@NotNull WorldAccess world, @NotNull Player player, @NotNull Item item, @NotNull BlockVec pos) {
        Pig entity = new Pig(EntityTypes.PIG, (World) world);
        entity.setPosition(pos.above().d());
        world.spawn(entity);

        return UseResult.ALLOW;
    }
}
