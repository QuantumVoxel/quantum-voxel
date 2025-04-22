package dev.ultreon.quantum.registry;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.libs.commons.v0.Logger;
import dev.ultreon.quantum.registry.event.RegistryEvents;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.tags.NamedTag;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("GDXJavaUnsafeIterator")
public abstract class Registry<T> extends AbstractRegistryMap<RegistryKey<T>, T> implements RawIdMap<T> {
    private static Logger dumpLogger = (level, msg, t) -> {};
    private static boolean frozen;
    private final ObjectMap<RegistryKey<T>, T> registry = new ObjectMap<>();
    private final IntMap<T> idMap = new IntMap<>();
    private final Class<T> type;
    private final NamespaceID id;
    private final boolean overrideAllowed;
    private final boolean syncDisabled;
    protected final RegistryKey<Registry<T>> key;

    private final Map<NamespaceID, NamedTag<T>> tags = new HashMap<>();
    private int curId;

    protected Registry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {        this.key = key;
        this.id = builder.id;
        this.type = builder.type;
        this.overrideAllowed = builder.allowOverride;
        this.syncDisabled = builder.doNotSync;

        RegistryEvents.REGISTRY_DUMP.subscribe(this::dumpRegistry);
    }

    protected Registry(Builder<T> builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.overrideAllowed = builder.allowOverride;
        this.syncDisabled = builder.doNotSync;
        this.key = RegistryKey.registry(this);

        RegistryEvents.REGISTRY_DUMP.subscribe(this::dumpRegistry);
    }

    private RegistryKey<T> subspace(NamespaceID namespaceID) {
        return RegistryKey.of(this.key, namespaceID);
    }

    public static void freeze() {
        Registry.frozen = true;
    }

    public static void unfreeze() {
        Registry.frozen = false;
    }

    public static Logger getDumpLogger() {
        return dumpLogger;
    }

    public static void setDumpLogger(Logger dumpLogger) {
        Registry.dumpLogger = dumpLogger;
    }

    public NamespaceID id() {
        return this.id;
    }

    public RegistryKey<Registry<T>> key() {
        return this.key;
    }

    /**
     * Returns the id id of the given registered instance.
     *
     * @param obj the registered instance.
     * @return the id id of it.
     */
    @Nullable
    public NamespaceID getId(T obj) {
        @Nullable RegistryKey<T> registryKey = this.registry.findKey(obj, true);
        if (registryKey == null) return null;
        return registryKey.id();
    }

    /**
     * Returns the registry key of the given registered instance.
     *
     * @param obj the registered instance.
     * @return the registry key of it.
     */
    public RegistryKey<T> getKey(T obj) {
        return this.registry.findKey(obj, true);
    }

    /**
     * Returns the registered instance from the given {@link NamespaceID}
     *
     * @param key the id id.
     * @return a registered instance of the type {@link T}.
     * @throws ClassCastException if the type is invalid.
     */
    public T get(@Nullable NamespaceID key) {
        return this.registry.get(RegistryKey.of(this.key, key));
    }

    public boolean contains(NamespaceID rl) {
        return this.registry.containsKey(RegistryKey.of(this.key, rl));
    }

    public void dumpRegistry() {
        Registry.getDumpLogger().log("Registry dump: " + this.type.getSimpleName());
        for (ObjectMap.Entry<RegistryKey<T>, T> entry : this.entries()) {
            T object = entry.value;
            NamespaceID rl = entry.key.id();

            Registry.getDumpLogger().log("  (" + rl + ") -> " + object);
        }
    }

    /**
     * Register an object.
     *
     * @param rl  the resource location.
     * @param val the register item value.
     */
    public void register(NamespaceID rl, T val) {
        if (!this.type.isAssignableFrom(val.getClass()))
            throw new IllegalArgumentException("Not allowed type detected, got " + val.getClass() + " expected assignable to " + this.type);


        RegistryKey<T> key = new RegistryKey<>(this.key, rl);
        if (this.registry.containsKey(key) && !this.overrideAllowed)
            throw new IllegalArgumentException("Already registered: " + rl);

        int setId = curId++;
        this.idMap.put(setId, val);
        this.registry.put(key, val);
    }

    public boolean isOverrideAllowed() {
        return this.overrideAllowed;
    }

    public boolean isSyncDisabled() {
        return this.syncDisabled;
    }

    public ObjectMap.Values<T> values() {
        return this.registry.values();
    }

    public ObjectMap.Keys<RegistryKey<T>> keys() {
        return this.registry.keys();
    }

    public ObjectMap.Entries<RegistryKey<T>, T> entries() {
        return registry.entries();
    }

    @Override
    public int size() {
        return this.registry.size;
    }

    @Override
    public boolean isEmpty() {
        return this.registry.isEmpty();
    }

    public Class<T> getType() {
        return this.type;
    }

    @Override
    public int getRawId(T object) {
        int theKey = this.idMap.findKey(object, true, -1);
        if (theKey == -1) throw new NoSuchElementException("No such element: " + object);
        return theKey;
    }

    @Override
    public @Nullable T byId(int id) {
        return this.idMap.get(id);
    }

    public void register(RegistryKey<T> id, T element) {
        if (!this.type.isAssignableFrom(element.getClass()))
            throw new IllegalArgumentException("Not allowed type detected, got " + element.getClass() + " expected assignable to " + this.type);

        if (this.registry.containsKey(id) && !this.overrideAllowed)
            throw new IllegalArgumentException("Already registered: " + id);

        this.registry.put(id, element);
        this.idMap.put(curId++, element);
    }

    public T get(RegistryKey<T> key) {
        T value = this.registry.get(key);
        if (value == null) throw new NoSuchElementException("No such element: " + key);
        return value;
    }

    public void reload(ReloadContext context) {
        for (NamedTag<T> tag : this.tags.values())
            tag.reload(context);
    }

    public Optional<NamedTag<T>> getTag(NamespaceID namespaceID) {
        NamedTag<T> tag = this.tags.get(namespaceID);
        if (tag == null) return Optional.empty();

        return Optional.of(tag);
    }

    public NamedTag<T> createTag(NamespaceID namespaceID) {
        NamedTag<T> tag = new NamedTag<>(namespaceID, this);
        this.tags.put(namespaceID, tag);

        return tag;
    }

    public static abstract class Builder<T> {
        private final Class<T> type;
        private final NamespaceID id;
        private boolean allowOverride = false;
        private boolean doNotSync = false;

        @SafeVarargs
        @SuppressWarnings("unchecked")
        public Builder(NamespaceID id, T... typeGetter) {
            this.type = (Class<T>) typeGetter.getClass().getComponentType();
            this.id = id;
        }

        public Builder<T> allowOverride() {
            this.allowOverride = true;
            return this;
        }

        public abstract Registry<T> build();

        public Builder<T> doNotSync() {
            this.doNotSync = true;
            return this;
        }
    }
}
