package dev.ultreon.quantum.client.management;

import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.common.base.Supplier;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShaderProviderManager implements Manager<ShaderProvider> {
    private final Map<NamespaceID, ShaderProvider> shaders = new LinkedHashMap<>();
    private final LinkedHashMap<NamespaceID, Supplier<? extends ShaderProvider>> shaderProviderFactories = new LinkedHashMap<>();

    @Override
    public ShaderProvider register(@NotNull NamespaceID id, @NotNull ShaderProvider shaderProvider) {
        this.shaders.put(id, shaderProvider);
        return shaderProvider;
    }
    
    public <T extends ShaderProvider> Supplier<T> register(@NotNull NamespaceID id, @NotNull Supplier<T> factory) {
        Supplier<T> memoize = create(id, factory);
        this.shaderProviderFactories.put(id, memoize);
        return memoize;
    }

    @SafeVarargs
    private <T extends ShaderProvider> Supplier<T> create(NamespaceID id, Supplier<T> create, T... typeGetter) {
        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) typeGetter.getClass().getComponentType();
        return () -> {
            if (this.shaders.containsKey(id)) {
                return clazz.cast(this.shaders.get(id));
            }

            return QuantumClient.invokeAndWait(() -> {
                T provider = create.get();
                this.shaders.put(id, provider);
                return provider;
            });
        };
    }

    @Override
    public @Nullable ShaderProvider get(NamespaceID id) {
        ShaderProvider shaderProvider = this.shaders.get(id);

        if (shaderProvider == null) {
            throw new GdxRuntimeException("Shader provider not found: " + id);
        }

        return shaderProvider;
    }

    @Override
    public void reload(ReloadContext context) {
        for (ShaderProvider shaderProvider : List.copyOf(this.shaders.values())) {
            context.submit(shaderProvider::dispose);
        }

        this.shaders.clear();

        this.shaderProviderFactories.forEach((id, factory) -> {
            ShaderProvider provider = factory.get();
            this.shaders.put(id, provider);
            context.submit(provider::dispose);
        });
    }
}
