package dev.ultreon.quantum.recipe;

import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.Menu;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryMap;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.PagedList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("GDXJavaUnsafeIterator")
public class RecipeRegistry<T extends Recipe> implements RegistryMap<NamespaceID, T> {
    public static final String CATEGORY = "recipe";
    final ObjectMap<NamespaceID, T> registry = new ObjectMap<>();
    private boolean frozen = false;

    @Override
    public T get(NamespaceID obj) {
        return this.registry.get(obj);
    }

    @Override
    public void register(NamespaceID key, T val) {
        if (this.frozen) throw new IllegalStateException("Registry is frozen");

        this.registry.put(key, val);
    }

    @Override
    public ObjectMap.Values<T> values() {
        return this.registry.values();
    }

    @Override
    public RegistryKey<T> nameById(int rawID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectMap.Keys<NamespaceID> keys() {
        return this.registry.keys();
    }

    @Override
    public ObjectMap.Entries<NamespaceID, T> entries() throws IllegalAccessException {
        return this.registry.entries();
    }

    @Override
    public int size() {
        return this.registry.size;
    }

    public PagedList<T> getRecipes(int pageSize, @Nullable ContainerMenu inventory) {
        ObjectMap.Values<T> values = this.registry.values();
        PagedList<T> list = new PagedList<>(pageSize);
        if (inventory != null) {
            for (T recipe : values) {
                if (recipe != null && recipe.canCraft(inventory)) list.add(recipe);
            }
        }

        return list;
    }

    public NamespaceID getKey(T recipe) {
        return this.registry.findKey(recipe, true);
    }

    public T removeRecipe(NamespaceID id) {
        if (this.frozen) throw new IllegalStateException("Registry is frozen");

        return this.registry.remove(id);
    }

    public void freeze() {
        this.frozen = true;
    }

    public List<T> findRecipe(Menu menu) {
        return Arrays.stream(this.registry.values().toArray().items).filter(t -> t.canCraft(menu)).collect(Collectors.toList());
    }
}
