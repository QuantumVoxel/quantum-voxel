package dev.ultreon.quantum.recipe;

import dev.ultreon.quantum.collection.OrderedMap;
import dev.ultreon.quantum.menu.Inventory;
import dev.ultreon.quantum.registry.AbstractRegistry;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.util.PagedList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeRegistry<T extends Recipe> extends AbstractRegistry<Identifier, T> {
    public static final String CATEGORY = "recipe";
    private final OrderedMap<Identifier, T> keyMap = new OrderedMap<>();
    private final OrderedMap<T, Identifier> valueMap = new OrderedMap<>();
    private final Class<T> type;
    private boolean frozen = false;

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public RecipeRegistry(T... typeGetter) {
        this.type = (Class<T>) typeGetter.getClass().getComponentType();
    }

    @Override
    public T get(Identifier obj) {
        return this.keyMap.get(obj);
    }

    @Override
    public void register(Identifier key, T val) {
        if (this.frozen) throw new IllegalStateException("Registry is frozen");

        this.keyMap.put(key, val);
        this.valueMap.put(val, key);
    }

    @Override
    public List<T> values() {
        return this.keyMap.valueList();
    }

    @Override
    public List<Identifier> keys() {
        return this.keyMap.keyList();
    }

    @Override
    public Set<Map.Entry<Identifier, T>> entries() throws IllegalAccessException {
        return this.keyMap.entrySet();
    }

    public PagedList<T> getRecipes(int pageSize, @Nullable Inventory inventory) {
        List<T> values = this.keyMap.valueList();
        if (inventory != null) {
            values = values.stream().filter(t -> t.canCraft(inventory)).collect(Collectors.toList());
        }
        return new PagedList<>(pageSize, values);
    }

    public Identifier getKey(T recipe) {
        return this.valueMap.get(recipe);
    }

    public T removeRecipe(Identifier id) {
        if (this.frozen) throw new IllegalStateException("Registry is frozen");

        T recipe = this.keyMap.remove(id);
        this.valueMap.remove(recipe);
        return recipe;
    }

    public void freeze() {
        this.frozen = true;
    }

    public Class<T> getType() {
        return type;
    }
}
