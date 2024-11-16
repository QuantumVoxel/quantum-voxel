package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.block.entity.ContainerBlockEntity;
import dev.ultreon.quantum.item.ItemStack;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

@Getter
public class BlockEntitySlot extends ItemSlot {
    private final int idx;
    @Getter
    private final ContainerBlockEntity<?> blockEntity;
    private final IntFunction<ItemStack> getter;
    private final BiConsumer<Integer, ItemStack> setter;
    private final IntConsumer updater;

    public BlockEntitySlot(int idx, ContainerBlockEntity<?> blockEntity, IntFunction<ItemStack> getter, BiConsumer<Integer, ItemStack> setter, IntConsumer updater) {
        super(idx, null, null, 0, 0);
        this.idx = idx;
        this.blockEntity = blockEntity;
        this.getter = getter;
        this.setter = setter;
        this.updater = updater;
    }

    @Override
    public ContainerMenu getContainer() {
        throw new UnsupportedOperationException("Container menus aren't supported by block entity slots");
    }

    @Override
    public ItemStack getItem() {
        return getter.apply(idx);
    }

    @Override
    public ItemStack setItem(ItemStack item, boolean emitEvent) {
        ItemStack apply = getter.apply(idx);
        setter.accept(idx, item);
        return apply;
    }

    @Override
    public void update() {
        this.updater.accept(idx);
    }
}
