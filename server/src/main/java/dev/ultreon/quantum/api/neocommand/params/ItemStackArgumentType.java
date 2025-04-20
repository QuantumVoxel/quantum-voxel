package dev.ultreon.quantum.api.neocommand.params;

import dev.ultreon.quantum.api.neocommand.*;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.ubo.types.MapType;

import java.util.List;

public class ItemStackArgumentType implements ArgumentType<ItemStack> {
    private final boolean allowAir;

    private ItemStackArgumentType() {
        this(false);
    }

    private ItemStackArgumentType(boolean allowAir) {
        this.allowAir = allowAir;
    }

    @Override
    public ItemStack parse(CommandReader ctx) throws CommandParseException {
        Selective selective = ctx.nextSelective();
        Selector<?>[] selectors = selective.selectors();
        MapType data = new MapType();
        int count = 1;
        for (Selector<?> selector : selectors) {
            if (selector.type() == Selector.Type.DATA) {
                data = (MapType) selector.value();
            } else if (selector.type() == Selector.Type.ID) {
                count = (int) selector.value();
            } else {
                throw new CommandParseException("Incorrect selector: " + selector.type().character, ctx.tell());
            }
        }

        if (count <= 0) {
            throw new CommandParseException("Invalid count", ctx.tell());
        }

        String name = selective.name();
        if (name.isEmpty()) {
            throw new CommandParseException("Invalid item name", ctx.tell());
        }

        NamespaceID namespaceID = NamespaceID.parse(name);
        Item item = Registries.ITEM.get(namespaceID);
        if (item == null) {
            throw new CommandParseException("Invalid item: " + name, ctx.tell());
        }

        if (item == Items.AIR && !allowAir) {
            throw new CommandParseException("Invalid item: " + name, ctx.tell());
        }

        return new ItemStack(item, count, data);
    }

    @Override
    public void complete(SuggestionProvider ctx) {

    }

    @Override
    public boolean matches(CommandReader arg) {
        try {
            parse(arg);
            return true;
        } catch (CommandParseException e) {
            return false;
        }
    }

    @Override
    public List<String> getExamples() {
        return List.of(
                "quantum:stone",
                "quantum:path/to/value",
                "stone",
                "path/to/value",
                "stone%1",
                "path/to/value%1",
                "stone?{key: \"value\"}",
                "path/to/value?{key: \"value\"}",
                "stone%1?{key: \"value\"}",
                "path/to/value%1?{key: \"value\"}",
                "stone?{key: \"value\"}%1",
                "path/to/value?{key: \"value\"}%1",
                "quantum:stone%1",
                "quantum:path/to/value%1",
                "quantum:stone?{key: \"value\"}",
                "quantum:path/to/value?{key: \"value\"}",
                "quantum:stone%1?{key: \"value\"}",
                "quantum:path/to/value%1?{key: \"value\"}",
                "quantum:stone?{key: \"value\"}%1",
                "quantum:path/to/value?{key: \"value\"}%1"
        );
    }

    public static Parameter<ItemStack> itemStacks(String name) { return new Parameter<>(name, new ItemStackArgumentType()); }

    public static Parameter<ItemStack> itemStacks(String name, boolean allowAir) { return new Parameter<>(name, new ItemStackArgumentType(allowAir)); }
}
