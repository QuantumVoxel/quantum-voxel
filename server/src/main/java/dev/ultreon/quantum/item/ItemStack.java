package dev.ultreon.quantum.item;

import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A class that holds items with a certain amount and with data.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @see Item
 */
public class ItemStack {
    @Deprecated
    public static final ItemStack EMPTY = empty();
    private static ItemStack empty;
    private Item item;
    @NotNull
    private MapType data;
    private int count;

    /**
     * @param item the item type to hold.
     */
    public ItemStack(Item item) {
        this(item, 1);
    }

    /**
     *
     */
    public ItemStack() {
        this(Items.AIR, 0);
    }

    /**
     * @param item  the item type to hold.
     * @param count the stack amount.
     */
    public ItemStack(Item item, int count) {
        this(item, count, new MapType());
    }

    /**
     * @param item  the item type to hold.
     * @param count the stack amount.
     * @param data  the data tag.
     */
    public ItemStack(Item item, int count, @NotNull MapType data) {
        this.item = item;
        this.count = count;
        this.data = data;
        this.checkCount(); // Note: used method so mods can use @Redirect to remove stack limits.
    }

    public static ItemStack load(MapType data) {
        @Nullable NamespaceID id = NamespaceID.tryParse(data.getString("item"));
        if (id == null) return new ItemStack();

        Item item = Registries.ITEM.get(id);
        if (item == null || item == Items.AIR) return new ItemStack();

        int count = data.getInt("count", 0);
        if (count <= 0) return new ItemStack();

        MapType tag = data.getMap("Tag", new MapType());
        return new ItemStack(item, count, tag);
    }

    public static ItemStack deserialize(JsonValue asJsonObject) {
        Item item = Registries.ITEM.get(NamespaceID.parse(asJsonObject.get("item").asString()));
        if (item == null) return new ItemStack();
        JsonValue countJson = asJsonObject.get("count");
        if (countJson == null) return new ItemStack(item);
        int count = countJson.asInt();
        if (count <= 0) return new ItemStack();

        return new ItemStack(item, count);
    }

    public MapType save() {
        MapType data = new MapType();
        data.putString("item", this.item.getId().toString());
        data.putInt("count", this.count);
        data.put("Tag", this.data);
        return data;
    }

    private void checkCount() {
        int maxStackSize = this.item.getMaxStackSize();
        if (this.count < 0) this.count = 0;
        if (this.count > maxStackSize) this.count = maxStackSize;
    }

    public static ItemStack empty() {
        if (empty != null) return empty;
        return empty = new ItemStack() {
            @Override
            public void setCount(int count) {

            }
        };
    }

    /**
     * @return get the item the stack is holding.
     */
    public Item getItem() {
        return this.item;
    }

    /**
     * @return the item stack's data tag.
     */
    public @NotNull MapType getData() {
        return this.data;
    }

    public void setData(@NotNull MapType data) {
        this.data = data;
    }

    /**
     * @return the item stack count.
     */
    public int getCount() {
        return this.count;
    }

    /**
     * @param count the item stack count to set.
     */
    public void setCount(int count) {
        this.count = count;
        if (this.count == 0) {
            this.item = Items.AIR;
        }
    }

    /**
     * Shrinks the item stack by an amount.
     *
     * @param amount the amount to shrink by.
     * @return the amount that was remains.
     */
    public int shrink(int amount) {
        if (this.count == 0) {
            return amount;
        }

        if (this.count - amount <= 0) {
            var remainder = amount - this.count;
            this.count = 0;
            this.item = Items.AIR;
            return remainder;
        } else {
            this.count -= amount;
            return 0;
        }
    }

    /**
     * Grows the item stack by an amount.
     *
     * @param amount the amount to grow by.
     * @return the amount of item that has overflown.
     */
    public int grow(int amount) {
        if (this.count == this.getItem().getMaxStackSize())
            return amount;

        if (this.count + amount >= this.getItem().getMaxStackSize()) {
            var overflown = this.count + amount - this.getItem().getMaxStackSize();
            this.count = this.getItem().getMaxStackSize();
            return overflown;
        } else {
            this.count += amount;
            return 0;
        }
    }

    /**
     * @return true if this item stack is empty.
     */
    public boolean isEmpty() {
        return this.item == Items.AIR || this.count < 1;
    }

    /**
     * @return a copy of this item stack.
     */
    public ItemStack copy() {
        return new ItemStack(this.item, this.count, this.data.copy());
    }

    /**
     * Gets the description of the item.
     *
     * @return the description
     */
    public List<TextObject> getDescription() {
        return this.item.getDescription(this);
    }

    /**
     * Determines if this ItemStack is similar to another ItemStack.
     * Checks the item and the data tag.
     *
     * @param other the ItemStack to compare with
     * @return true if the ItemStacks are similar, false otherwise
     */
    public boolean sameItemSameData(ItemStack other) {
        return this.item == other.item && this.data.equals(other.data);
    }

    /**
     * Checks if the current item is the same as the given item.
     *
     * @param other the item to compare with
     * @return true if the items are the same, false otherwise
     */
    public boolean isSameItem(ItemStack other) {
        return this.item == other.item;
    }

    /**
     * Split the item with a specified amount.
     *
     * @param amount the amount to split.
     * @return the part that got split.
     */
    public ItemStack split(int amount) {
        if (amount <= 0) return ItemStack.empty(); // Return an empty stack if the amount is invalid

        if (amount >= this.count) {
            ItemStack copy = this.copy();
            this.count = 0;
            return copy;
        } else {
            this.count -= amount;
            return new ItemStack(this.item, amount, this.data.copy());
        }
    }

    /**
     * Split the item stack in half.
     *
     * @return the other half of the item stack.
     */
    public ItemStack split() {
        if (this.count <= 1) return ItemStack.empty();
        return this.split(this.count / 2);
    }

    /**
     * Transfers a specified amount of the stack to another item stack.
     *
     * @param target the item stack to receive the items.
     * @param amount the amount to transfer.
     * @return the amount remaining.
     */
    public int transferTo(ItemStack target, int amount) {
        if (target.isEmpty()) {
            target.item = this.item;
            target.data = this.data.copy();
            target.count = amount;
            this.shrink(amount);
            return 0;
        }

        int remainder = target.grow(amount);
        if (remainder == 0) {
            this.shrink(amount);
            return 0;
        }

        int transferred = amount - remainder;
        this.shrink(transferred);
        return remainder;
    }

    /**
     * Transfers one item to another item stack.
     *
     * @param target the item stack to receive the item.
     * @return the amount remaining.
     */
    public boolean transferTo(ItemStack target) {
        if (target.getItem() != this.item) return false;
        if (target.grow(1) == 1) return false;

        this.shrink(1);
        return true;
    }

    public String toString() {
        return this.item.getId() + " x" + this.count;
    }

    public ItemStack merge(ItemStack with) {
        if (!this.sameItemSameData(with)) return with;

        if (this.count + with.count > this.getItem().getMaxStackSize()) {
            with.count = this.getItem().getMaxStackSize() - this.count;
            this.count = this.getItem().getMaxStackSize();
            return with;
        }

        this.count += with.count;
        return with;
    }

    public float getAttackDamage() {
        return item.getAttackDamage(this);
    }

    public String getFullDescription() {
        String join = String.join("\n", getDescription().stream().map(TextObject::getText).collect(Collectors.toList()));
        return join + "\n\n" + getExtendedDescription();
    }

    private String getExtendedDescription() {
        return this.item.getExtendedDescription(this);
    }
}
