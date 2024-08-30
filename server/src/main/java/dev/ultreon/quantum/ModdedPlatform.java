package dev.ultreon.quantum;

import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.js.JsLoader;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.python.PyLoader;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.Env;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public abstract class ModdedPlatform extends GamePlatform {
    private boolean resourcesLoaded;
    private boolean modResourcesLoaded;

    private final Map<String, FabricMod> mods = new IdentityHashMap<>();
    private String[] args = new String[0];

    @Override
    public void locateResources() {
        try {
            URL resource = QuantumServer.class.getResource("/.quantum-server-resources");
            if (resource == null) throw new GdxRuntimeException("Quantum Voxel resources unavailable!");
            String string = resource.toString();

            if (string.startsWith("jar:")) {
                string = string.substring("jar:".length());
            }

            string = string.substring(0, string.lastIndexOf('/'));

            if (string.endsWith("!")) {
                string = string.substring(0, string.length() - 1);
            }

            getResourceManager().importPackage(new File(new URI(string)).toPath());
            resourcesLoaded = true;
        } catch (Exception e) {
            for (Path rootPath : FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow().getRootPaths()) {
                try {
                    getResourceManager().importPackage(rootPath);
                } catch (IOException ex) {
                    crash(ex);
                }
            }
        }
    }

    public abstract ResourceManager getResourceManager();

    @Override
    public void locateModResources() {
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.getOrigin().getKind() != ModOrigin.Kind.PATH) continue;

            for (Path rootPath : mod.getRootPaths()) {
                // Try to import a resource package for the given mod path.
                try {
                    getResourceManager().importPackage(rootPath);
                } catch (IOException e) {
                    CommonConstants.LOGGER.warn("Importing resources failed for path: " + rootPath.toFile(), e);
                }
            }
        }

        this.modResourcesLoaded = true;
    }

    private void crash(Throwable throwable) {
        QuantumServer quantumServer = QuantumServer.get();
        if (quantumServer == null) {
            CrashLog crashLog = new CrashLog("An unexpected error occurred", new Exception());
            CrashCategory category = new CrashCategory("Crashed early", throwable);
            category.add("Resources loaded", this.resourcesLoaded);
            category.add("Mod resources loaded", this.modResourcesLoaded);
            crashLog.addCategory(category);
            throw new ApplicationCrash(crashLog);
        }
        quantumServer.crash(throwable);
    }

    @Override
    public Optional<Mod> getMod(String id) {
        return FabricLoader.getInstance().getModContainer(id).map(container -> (Mod) this.mods.computeIfAbsent(id, v -> new FabricMod(container))).or(() -> super.getMod(id));
    }

    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id) || super.isModLoaded(id);
    }

    @Override
    public Collection<? extends Mod> getMods() {
        var list = new ArrayList<Mod>();
        list.addAll(FabricLoader.getInstance().getAllMods().stream().map(container -> this.mods.computeIfAbsent(container.getMetadata().getId(), v -> new FabricMod(container))).toList());
        list.addAll(JsLoader.getInstance().getMods());
        list.addAll(PyLoader.getInstance().getMods());
        list.addAll(super.getMods());
        return list;
    }

    @Override
    public boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T> void invokeEntrypoint(String name, Class<T> initClass, Consumer<T> init) {
        FabricLoader.getInstance().invokeEntrypoints(name, initClass, init);
    }

    @Override
    public Env getEnv() {
        return switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT -> Env.CLIENT;
            case SERVER -> Env.SERVER;
        };
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public boolean isDesktop() {
        return true;
    }

    @Override
    public boolean isMacOSX() {
        return OS.isMac();
    }

    @Override
    public boolean isWindows() {
        return OS.isWindows();
    }

    @Override
    public boolean isLinux() {
        return OS.isLinux();
    }

    @Override
    public abstract DeviceType getDeviceType();

    @Override
    public void launch(String[] argv) {
        this.args = argv;
    }

    public String[] getArgs() {
        return args.clone();
    }

    @Override
    public void requestAttention() {
        boolean taskbarSupported = Taskbar.isTaskbarSupported();

        if (!taskbarSupported) return;

        Taskbar taskbar = Taskbar.getTaskbar();
        boolean supported = taskbar.isSupported(Taskbar.Feature.USER_ATTENTION);
        if (supported) taskbar.requestUserAttention(true, false);
    }

    @Override
    public Logger getLogger(String name) {
        return new Slf4jLogger(LoggerFactory.getLogger(name));
    }
}
