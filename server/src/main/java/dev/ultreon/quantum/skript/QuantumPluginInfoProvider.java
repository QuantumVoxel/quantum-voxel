//package dev.ultreon.quantum.skript;
//
//import dev.ultreon.baseskript.Plugin;
//import dev.ultreon.baseskript.PluginInfoProvider;
//
//import java.io.File;
//import java.io.InputStream;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.nio.file.Paths;
//
//public class QuantumPluginInfoProvider implements PluginInfoProvider {
//    @Override
//    public boolean isValid(Plugin plugin) {
//        return plugin instanceof QuantumSkriptPlugin;
//    }
//
//    @Override
//    public File getDataFolder(Plugin plugin) {
//        return new File("config/" + plugin.getName());
//    }
//
//    @Override
//    public URL getPluginLocation(Plugin plugin) {
//        return plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
//    }
//
//    @Override
//    public InputStream getResource(Plugin plugin, String name) {
//        return plugin.getClass().getClassLoader().getResourceAsStream("/" + name);
//    }
//
//    @Override
//    public File getFile(Plugin plugin) {
//        try {
//            return Paths.get(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
//        } catch (URISyntaxException e) {
//            return null;
//        }
//    }
//}
