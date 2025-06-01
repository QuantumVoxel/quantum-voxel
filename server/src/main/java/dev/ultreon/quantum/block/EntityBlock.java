package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public abstract class EntityBlock extends Block {
    public EntityBlock() {
    }

    public EntityBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(@NotNull World world, @NotNull BlockVec pos, @NotNull BlockState blockState) {
        super.onPlace(world, pos, blockState);

        world.setBlockEntity(pos, this.createBlockEntity(world, pos));
    }

    @NotNull
    protected abstract BlockEntity createBlockEntity(World world, BlockVec pos);

    public static EntityBlock simple(BlockEntityType<?> type) {
        return new EntityBlock() {
            @NotNull
            @Override
            protected BlockEntity createBlockEntity(World world, BlockVec pos) {
                return type.create(world, pos);
            }
        };
    }

    public static EntityBlock simple(BlockEntityType<?> type, Properties properties) {
        return new EntityBlock(properties) {
            @NotNull
            @Override
            protected BlockEntity createBlockEntity(World world, BlockVec pos) {
                return type.create(world, pos);
            }
        };
    }
}
