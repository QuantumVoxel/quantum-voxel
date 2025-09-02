package dev.ultreon.quantum.skript;

import dev.ultreon.baseskript.Plugin;
import dev.ultreon.baseskript.PluginClassesProvider;
import dev.ultreon.baseskript.PluginInfoProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class QuantumPluginClassesProvider implements PluginClassesProvider {
    @Override
    public boolean isValid(Plugin plugin) {
        return plugin instanceof QuantumSkriptPlugin;
    }

    @Override
    public Class<?>[] getClasses(Plugin plugin, String basePackage, String... subPackages) {
        return ((QuantumSkriptPlugin) plugin).getClasses(basePackage, subPackages);
    }
}
