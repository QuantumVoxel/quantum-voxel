package dev.ultreon.quantum.client.management;

import dev.ultreon.quantum.client.resources.ContextAwareReloadable;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Manager<T> extends ContextAwareReloadable {
    T register(@NotNull NamespaceID id, @NotNull T object);

    @Nullable T get(NamespaceID id);

    void reload(ReloadContext context);

    @Override
    default void reload(ResourceManager resourceManager, ReloadContext context) {
        this.reload(context);
    }
}
