package dev.ultreon.quantum.recipe;

import dev.ultreon.quantum.collection.OrderedMap;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.Menu;
import dev.ultreon.quantum.registry.AbstractRegistryMap;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.PagedList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeRegistry<T extends Recipe> extends AbstractRegistryMap<NamespaceID, T> {
    public static final String CATEGORY = "recipe";
    final OrderedMap<NamespaceID, T> keyMap = new OrderedMap<>();
    private final OrderedMap<T, NamespaceID> valueMap = new OrderedMap<>();
    private boolean frozen = false;

    @Override
    public T get(NamespaceID obj) {
        return this.keyMap.get(obj);
    }

    @Override
    public void register(NamespaceID key, T val) {
        if (this.frozen) throw new IllegalStateException("Registry is frozen");

        this.keyMap.put(key, val);
        this.valueMap.put(val, key);
    }

    @Override
    public List<T> values() {
        return this.keyMap.valueList();
    }

    @Override
    public List<NamespaceID> keys() {
        return this.keyMap.keyList();
    }

    @Override
    public Set<Map.Entry<NamespaceID, T>> entries() throws IllegalAccessException {
        return this.keyMap.entrySet();
    }

    public PagedList<T> getRecipes(int pageSize, @Nullable ContainerMenu inventory) {
        List<T> values = this.keyMap.valueList();
        if (inventory != null) {
            values = values.stream().filter(t -> t.canCraft(inventory)).collect(Collectors.toList());
        }
        return new PagedList<>(pageSize, values);
    }

    public NamespaceID getKey(T recipe) {
        return this.valueMap.get(recipe);
    }

    public T removeRecipe(NamespaceID id) {
        if (this.frozen) throw new IllegalStateException("Registry is frozen");

        T recipe = this.keyMap.remove(id);
        this.valueMap.remove(recipe);
        return recipe;
    }

    public void freeze() {
        this.frozen = true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<T> findRecipe(Menu menu) {
        return this.keyMap.values().stream().filter(t -> t.canCraft(menu)).toList();
    }
}
