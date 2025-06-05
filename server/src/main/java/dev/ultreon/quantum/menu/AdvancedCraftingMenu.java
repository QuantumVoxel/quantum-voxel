package dev.ultreon.quantum.menu;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.container.Container;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A class that holds a bunch of item slots.
 *
 * @see ItemSlot
 * @see ItemStack
 * @see MenuType
 */
public class AdvancedCraftingMenu extends ContainerMenu {
    private @Nullable TextObject customTitle = null;

    /**
     * Constructs a new CraftingMenu.
     *
     * @param world     the world where the menu is opened.
     * @param entity    the entity that opened the menu.
     * @param pos       the position where the menu is opened; may be null.
     * @param container the container associated with the menu; may be null.
     */
    public AdvancedCraftingMenu(@NotNull WorldAccess world, @NotNull Entity entity, @Nullable BlockVec pos, @Nullable Container<?> container) {
        this(MenuTypes.ADVANCED_CRAFTING, world, entity, pos, 36, container);
    }

    /**
     * Constructs a new CraftingMenu.
     *
     * @param type      the type of the menu must not be null.
     * @param world     the world where the menu is opened, must not be null.
     * @param entity    the entity that opened the menu must not be null.
     * @param pos       the position where the menu is opened, may be null.
     * @param size      the size of the menu cannot be negative.
     * @param container the container associated with the menu, may be null.
     */
    public AdvancedCraftingMenu(@NotNull MenuType<?> type, @NotNull WorldAccess world, @NotNull Entity entity, @Nullable BlockVec pos, int size, @Nullable Container<?> container) {
        super(type, world, entity, pos, size, container);

        this.build();
    }

    /**
     * Constructs a new CraftingMenu.
     *
     * @param craftingMenuMenuType the type of the crafting menu must not be null.
     * @param world the world where the menu is opened, must not be null.
     * @param entity the entity that opened the menu must not be null.
     * @param pos the position where the menu is opened, may be null.
     */
    public AdvancedCraftingMenu(MenuType<AdvancedCraftingMenu> craftingMenuMenuType, World world, Entity entity, @Nullable BlockVec pos) {
        this(craftingMenuMenuType, world, entity, pos, 36, null);
    }

    @Override
    public void build() {
        super.build();

        inventoryMenu(0, 6, 6);
    }

    @Override
    public List<ItemSlot> getInputs() {
        return List.of();
    }

    @Override
    public List<ItemSlot> getOutputs() {
        return List.of();
    }

    /**
     * Retrieves the title of the menu.
     *
     * @return the title
     */
    @Override
    public TextObject getTitle() {
        NamespaceID id = this.getType().getId();

        if (this.customTitle == null)
            return TextObject.translation(id.getDomain() + ".container." + id.getPath().replace("/", ".") + ".title");
        return this.customTitle;
    }

    @Override
    public void setCustomTitle(@Nullable TextObject customTitle) {
        this.customTitle = customTitle;
    }

    @Override
    public @NotNull Iterator<ItemStack> iterator() {
        return Arrays.stream(this.slots).map(ItemSlot::getItem).iterator();
    }
}
