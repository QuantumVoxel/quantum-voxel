package dev.ultreon.quantum.world.loot;

import com.google.common.collect.ImmutableList;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.world.rng.RNG;
import org.apache.commons.lang3.IntegerRange;

import java.util.List;
import java.util.Objects;

public class RandomLoot implements LootGenerator {
    private final List<LootEntry> entries;

    public RandomLoot(LootEntry... entries) {
        this.entries = List.of(entries);
    }

    @Override
    public Iterable<ItemStack> generate(RNG random) {
        var items = new ImmutableList.Builder<ItemStack>();

        for (var entry : this.entries) {
            int count = entry.randomCount(random);
            items.add(new ItemStack(entry.item(), count, entry.data()));
        }

        return items.build();
    }

    public List<LootEntry> getEntries() {
        return this.entries;
    }

    public interface LootEntry {
        int randomCount(RNG random);

        Item item();

        MapType data();
    }

    public static final class CountLootEntry implements LootEntry {
        private final IntegerRange range;
        private final Item item;
        private final MapType data;

            public CountLootEntry(IntegerRange range, Item item, MapType data) {
                this.range = range;
                this.item = item;

                this.data = data == null ? new MapType() : data;
            }

            public CountLootEntry(IntegerRange range, Item rock) {
                this(range, rock, new MapType());
            }

            @Override
            public int randomCount(RNG random) {
                return random.randint(this.range.getMinimum(), this.range.getMaximum());
            }

        public IntegerRange range() {
            return range;
        }

        @Override
        public Item item() {
            return item;
        }

        @Override
        public MapType data() {
            return data;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (CountLootEntry) obj;
            return Objects.equals(this.range, that.range) &&
                   Objects.equals(this.item, that.item) &&
                   Objects.equals(this.data, that.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(range, item, data);
        }

        @Override
        public String toString() {
            return "CountLootEntry[" +
                   "range=" + range + ", " +
                   "item=" + item + ", " +
                   "data=" + data + ']';
        }

        }

    public static final class ChanceLootEntry implements LootEntry {
        private final float chance;
        private final Item item;
        private final MapType data;

            public ChanceLootEntry(float chance, Item item, MapType data) {
                this.chance = chance;
                this.item = item;

                this.data = data == null ? new MapType() : data;
            }

            public ChanceLootEntry(float chance, Item rock) {
                this(chance, rock, new MapType());
            }

            @Override
            public int randomCount(RNG random) {
                return random.chance(this.chance) ? 1 : 0;
            }

        public float chance() {
            return chance;
        }

        @Override
        public Item item() {
            return item;
        }

        @Override
        public MapType data() {
            return data;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ChanceLootEntry) obj;
            return Float.floatToIntBits(this.chance) == Float.floatToIntBits(that.chance) &&
                   Objects.equals(this.item, that.item) &&
                   Objects.equals(this.data, that.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chance, item, data);
        }

        @Override
        public String toString() {
            return "ChanceLootEntry[" +
                   "chance=" + chance + ", " +
                   "item=" + item + ", " +
                   "data=" + data + ']';
        }

        }
}
