package dev.ultreon.xeox.impl.games;

import dev.ultreon.xeox.api.Environment;
import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.impl.EntryPoint;
import dev.ultreon.xeox.impl.IPermissionProvider;
import dev.ultreon.xeox.impl.ModClassLoader;

import java.util.List;

public interface IGameProvider {
    void initialize(List<IFileSystem> gameFileSystems);

    void setupClassLoader(ModClassLoader classLoader);

    IPermissionProvider getPermissionProvider();

    String mainClass(List<IFileSystem> gameFileSystems);

    String namespace();

    String name();

    Environment getEnvironment();

    String version();

    List<String> mixinConfigs();

    List<EntryPoint> entrypoints();

    default List<String> permissions() {
        return List.of("*");
    }

    default boolean disableMixinCheck() {
        return false;
    }
}
