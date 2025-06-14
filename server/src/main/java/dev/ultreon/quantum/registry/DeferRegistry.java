package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.registry.event.RegistryEvents;
import dev.ultreon.quantum.util.NamespaceID;
import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class DeferRegistry<T> {
    @NotNull
    private final String modId;
    @NotNull
    private final Registry<T> registry;
    private final ArrayList<HashMap.Entry<NamespaceID, Supplier<T>>> objects = new ArrayList<>();

    protected DeferRegistry(@NotNull String modId, @NotNull Registry<T> registry) {        this.modId = modId;
        this.registry = registry;
    }

    public static <T> DeferRegistry<T> of(String namespace, Registry<T> registry) {
        return new DeferRegistry<>(namespace, registry);
    }

    public <C extends T> DeferredElement<C> defer(@NotNull String name, @NotNull Supplier<@NotNull C> supplier) {
        var id = new NamespaceID(this.modId, name);

        this.objects.add(new HashMap.SimpleEntry<>(id, supplier::get));

        return new DeferredElement<>(this.registry, supplier, id);
    }

    public <C extends T> DeferredElement<C> defer(String s, Closure<C> closure) {
        return defer(s, closure::call);
    }

    public void register() {
        RegistryEvents.AUTO_REGISTER.subscribe((modId, registry) -> {
            if (!registry.getType().equals(this.registry.getType()) || !this.modId.equals(modId)) {
                return;
            }

            for (HashMap.Entry<NamespaceID, Supplier<T>> entry : this.objects) {
                T object = entry.getValue().get();
                NamespaceID id = entry.getKey();

                if (!registry.getType().isAssignableFrom(object.getClass())) {
                    throw new IllegalArgumentException("Got invalid type in deferred register: " + object.getClass() + " expected assignable to " + registry.getType());
                }

                this.registry.register(id, object);
            }
        });
    }
}
