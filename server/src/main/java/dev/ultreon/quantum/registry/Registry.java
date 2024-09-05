package dev.ultreon.quantum.registry;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import dev.ultreon.libs.commons.v0.Logger;
import dev.ultreon.quantum.collection.OrderedMap;
import dev.ultreon.quantum.registry.event.RegistryEvents;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.tags.NamedTag;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Registry<T> extends AbstractRegistryMap<RegistryKey<T>, T> implements RawIdMap<T> {
    private static Logger dumpLogger = (level, msg, t) -> {};
    private static boolean frozen;
    private final OrderedMap<RegistryKey<T>, T> keyMap = new OrderedMap<>();
    private final OrderedMap<T, RegistryKey<T>> valueMap = new OrderedMap<>();
    private final Class<T> type;
    private final NamespaceID id;
    private final boolean overrideAllowed;
    private final boolean syncDisabled;
    protected final RegistryKey<Registry<T>> key;
    private final Codec<RegistryKey<T>> keyCodec;

    private final Map<NamespaceID, NamedTag<T>> tags = new HashMap<>();

    protected Registry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {
        Preconditions.checkNotNull(key, "key");
        this.key = key;
        this.id = builder.id;
        this.type = builder.type;
        this.overrideAllowed = builder.allowOverride;
        this.syncDisabled = builder.doNotSync;
        keyCodec = NamespaceID.CODEC.xmap(this::subspace, RegistryKey::id);

        RegistryEvents.REGISTRY_DUMP.subscribe(this::dumpRegistry);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Registry(Builder<T> builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.overrideAllowed = builder.allowOverride;
        this.syncDisabled = builder.doNotSync;
        this.key = RegistryKey.registry(this);
        keyCodec = NamespaceID.CODEC.xmap(this::subspace, RegistryKey::id);

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
        RegistryKey<T> registryKey = this.valueMap.get(obj);
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
        return this.valueMap.get(obj);
    }

    /**
     * Returns the registered instance from the given {@link NamespaceID}
     *
     * @param key the id id.
     * @return a registered instance of the type {@link T}.
     * @throws ClassCastException if the type is invalid.
     */
    public T get(@Nullable NamespaceID key) {
        return this.keyMap.get(RegistryKey.of(this.key, key));
    }

    public boolean contains(NamespaceID rl) {
        return this.keyMap.containsKey(RegistryKey.of(this.key, rl));
    }

    public void dumpRegistry() {
        Registry.getDumpLogger().log("Registry dump: " + this.type.getSimpleName());
        for (Map.Entry<RegistryKey<T>, T> entry : this.entries()) {
            T object = entry.getValue();
            NamespaceID rl = entry.getKey().id();

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
        if (this.keyMap.containsKey(key) && !this.overrideAllowed)
            throw new IllegalArgumentException("Already registered: " + rl);

        this.keyMap.put(key, val);
        this.valueMap.put(val, key);
    }

    public boolean isOverrideAllowed() {
        return this.overrideAllowed;
    }

    public boolean isSyncDisabled() {
        return this.syncDisabled;
    }

    public List<T> values() {
        return Collections.unmodifiableList(this.keyMap.valueList());
    }

    public List<NamespaceID> ids() {
        return this.keyMap.keyList().stream().map(RegistryKey::id).collect(Collectors.toList());
    }

    public List<RegistryKey<T>> keys() {
        return Collections.unmodifiableList(this.keyMap.keyList());
    }

    public Set<Map.Entry<RegistryKey<T>, T>> entries() {
        // I do this because the IDE won't accept dynamic values and keys.
        ArrayList<T> values = new ArrayList<>(this.values());
        ArrayList<RegistryKey<T>> keys = new ArrayList<>(this.keys());

        if (keys.size() != values.size()) throw new IllegalStateException("Keys and values have different lengths.");

        Set<Map.Entry<RegistryKey<T>, T>> entrySet = new HashSet<>();

        for (int i = 0; i < keys.size(); i++) {
            entrySet.add(new AbstractMap.SimpleEntry<>(keys.get(i), values.get(i)));
        }

        return Collections.unmodifiableSet(entrySet);
    }

    public Class<T> getType() {
        return this.type;
    }

    public boolean isFrozen() {
        return Registry.frozen;
    }

    @Override
    public int getRawId(T object) {
        return this.keyMap.indexOfValue(object);
    }

    @Override
    public @Nullable T byId(int id) {
        if (id < 0 || id >= this.keyMap.size()) return null;
        return this.keyMap.valueList().get(id);
    }

    public void register(RegistryKey<T> id, T element) {
        if (!this.type.isAssignableFrom(element.getClass()))
            throw new IllegalArgumentException("Not allowed type detected, got " + element.getClass() + " expected assignable to " + this.type);

        if (this.keyMap.containsKey(id) && !this.overrideAllowed)
            throw new IllegalArgumentException("Already registered: " + id);

        this.keyMap.put(id, element);
        this.valueMap.put(element, id);
    }

    public T get(RegistryKey<T> key) {
        T value = this.keyMap.get(key);
        if (value == null) throw new NoSuchElementException("No such element: " + key);
        return value;
    }

    public Codec<RegistryKey<T>> keyCodec() {
        return this.keyCodec;
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
