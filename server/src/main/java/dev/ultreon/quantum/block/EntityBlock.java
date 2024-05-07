package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

public abstract class EntityBlock extends Block {
    public EntityBlock() {
    }

    public EntityBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(World world, BlockPos pos, BlockProperties blockProperties) {
        super.onPlace(world, pos, blockProperties);

        world.setBlockEntity(pos, this.createBlockEntity(world, pos));
    }

    @NotNull
    protected abstract BlockEntity createBlockEntity(World world, BlockPos pos);

    public static EntityBlock simple(BlockEntityType<?> type) {
        return new EntityBlock() {
            @NotNull
            @Override
            protected BlockEntity createBlockEntity(World world, BlockPos pos) {
                return type.create(world, pos);
            }
        };
    }

    public static EntityBlock simple(BlockEntityType<?> type, Properties properties) {
        return new EntityBlock(properties) {
            @NotNull
            @Override
            protected BlockEntity createBlockEntity(World world, BlockPos pos) {
                return type.create(world, pos);
            }
        };
    }
}
