package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.util.NamespaceID;
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
        NamespaceID id = new NamespaceID(this.modId, name);

        this.objects.add(new HashMap.SimpleEntry<>(id, supplier::get));

        return new DeferredElement<>(this.registry, supplier, id);
    }

    public void register() {
        EventSystem.addListenerDefault(AutoRegisterEvent.class, event -> {
            String modId = event.getModId();
            Registry<?> registry = event.getRegistry();

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
