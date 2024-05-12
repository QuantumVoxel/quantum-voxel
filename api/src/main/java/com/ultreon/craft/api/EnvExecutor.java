package dev.ultreon.quantum.api;

import dev.ultreon.quantum.util.Env;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Optional;
import java.util.function.Supplier;

public class EnvExecutor {
    public static void runInEnv(Env env, Supplier<Runnable> runnable) {
        if (FabricLoader.getInstance().getEnvironmentType() == env) {
            runnable.get().run();
        }
    }

    public static <T> Optional<T> getInEnv(Env env, Supplier<Supplier<T>> supplier) {
        return FabricLoader.getInstance().getEnvironmentType() == env ? Optional.of(supplier.get().get()) : Optional.empty();
    }

    public static <T> T getInEnv(Supplier<Supplier<T>> clientSupplier, Supplier<Supplier<T>> serverSupplier) {
        return FabricLoader.getInstance().getEnvironmentType() == Env.CLIENT ? clientSupplier.get().get() : serverSupplier.get().get();
    }

    public static void runInEnv(Supplier<Runnable> clientRunnable, Supplier<Runnable> serverRunnable) {
        if (FabricLoader.getInstance().getEnvironmentType() == Env.CLIENT) clientRunnable.get().run();
        else serverRunnable.get().run();
    }
}
