package dev.ultreon.xeox.impl.games.quantum;

import dev.ultreon.xeox.api.Environment;
import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.impl.EntryPoint;
import dev.ultreon.xeox.impl.IPermissionProvider;
import dev.ultreon.xeox.impl.ModClassLoader;
import dev.ultreon.xeox.impl.ModLoaderException;
import dev.ultreon.xeox.impl.games.IGameProvider;

import java.util.Arrays;
import java.util.List;

public class QuantumGameProvider implements IGameProvider {
    private Environment environment;

    @Override
    public void initialize(List<IFileSystem> gameFileSystems) {
        for (IFileSystem gameFileSystem : gameFileSystems) {
            if (gameFileSystem.path("dev/ultreon/quantum/desktop/XeoxDesktopLauncher.class").exists()) {
                environment = Environment.CLIENT;
                return;
            } else if (gameFileSystem.path("dev/ultreon/quantum/dedicated/XeoxLauncher.class").exists()) {
                environment = Environment.SERVER;
                return;
            }
        }

        throw new ModLoaderException("No game found in provided file systems!");
    }

    @Override
    public void setupClassLoader(ModClassLoader classLoader) {
        classLoader.blockMixin();
    }

    @Override
    public IPermissionProvider getPermissionProvider() {
        return new QuantumPermissionProvider();
    }

    @Override
    public String mainClass(List<IFileSystem> gameFileSystems) {
        if (environment == null) {
            throw new ModLoaderException("Environment not initialized!");
        }

        return switch (environment) {
            case CLIENT -> "dev.ultreon.quantum.desktop.XeoxDesktopLauncher";
            case SERVER -> "dev.ultreon.quantum.dedicated.XeoxLauncher";
        };
    }

    @Override
    public String namespace() {
        return "quantum";
    }

    @Override
    public String name() {
        return "Quantum Voxel";
    }

    @Override
    public Environment getEnvironment() {
        if (environment == null) {
            throw new ModLoaderException("Environment not initialized!");
        }

        return environment;
    }

    @Override
    public String version() {
        return "0.2.0-alpha.1";
    }

    @Override
    public List<String> mixinConfigs() {
        return Arrays.asList();
    }

    @Override
    public List<EntryPoint> entrypoints() {
        return Arrays.asList();
    }
}
