package dev.ultreon.quantum.dedicated;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.platform.PlatformFeature;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.scripting.ScriptLoader;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.xeox.api.IMod;
import dev.ultreon.xeox.api.IPath;
import dev.ultreon.xeox.api.IXeoxLoader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServerPlatform extends GamePlatform {
    private final Map<String, XeoxMod> mods = new IdentityHashMap<>();
    private final ScriptLoader scriptLoader = new ScriptLoader();

    @Override
    public WebSocket newWebSocket(String location, Consumer<Throwable> onError, WebSocket.InitializeListener initializeListener, WebSocket.ConnectedListener connectedListener) {
        throw new IllegalArgumentException("Can't create a websocket as a server!");
    }

    @Override
    public @Nullable MouseDevice getMouseDevice() {
        return null;
    }

    @Override
    public boolean isMouseCaptured() {
        return false;
    }

    @Override
    public void setMouseCaptured(boolean captured) {
        // Server doesn't support mouse
    }

    @Override
    public Collection<Device> getGameDevices() {
        return List.of(); // Server doesn't support mouse
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SERVER;
    }

    @Override
    public boolean isAngleGLES() {
        return false;
    }

    @Override
    public boolean isGLES() {
        return false;
    }

    @Override
    public boolean isWebGL() {
        return false;
    }

    @Override
    public boolean hasBackPanelRemoved() {
        return false;
    }

    @Override
    public boolean isWeb() {
        return false;
    }

    @Override
    public int cpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public void locateContentResources(ResourceManager resourceManager) {

    }

    @Override
    public void handleCrash(ApplicationCrash crash) {
        crash.printCrash();
        crash.getCrashLog().defaultSave();
    }

    @Override
    public long[] getUuidElements(UUID value) {
        return new long[]{value.getMostSignificantBits(), value.getLeastSignificantBits()};
    }

    @Override
    public UUID constructUuid(long msb, long lsb) {
        return new UUID(msb, lsb);
    }

    @Override
    public Mod getGameMod() {
        if (IXeoxLoader.get() == null) return GameMod.INSTANCE;
        return new XeoxMod(IXeoxLoader.get().getMod(CommonConstants.NAMESPACE));
    }

    @Override
    public boolean isFeatureSupported(PlatformFeature platformFeature) {
        return false;
    }

    @Override
    public void load(ResourceManager resourceManager) {
        scriptLoader.reload(resourceManager);
    }

    @Override
    public <T> List<T> createSyncList() {
        return new CopyOnWriteArrayList<>();
    }

    @Override
    public Logger getLogger(String name) {
        return (level, message, t) -> {
            if (level == null) return;
            org.slf4j.Logger logger = LoggerFactory.getLogger(name);

            if (t == null) {
                switch (level) {
                    case TRACE:
                        logger.trace(message);
                        return;
                    case DEBUG:
                        logger.debug(message);
                        return;
                    case INFO:
                        logger.info(message);
                        return;
                    case WARN:
                        logger.warn(message);
                        return;
                    case ERROR:
                        logger.error(message);
                        return;
                }
                return;
            }
            switch (level) {
                case TRACE:
                    logger.trace(message, t);
                    return;
                case DEBUG:
                    logger.debug(message, t);
                    return;
                case INFO:
                    logger.info(message, t);
                    return;
                case WARN:
                    logger.warn(message, t);
                    return;
                case ERROR:
                    logger.error(message, t);
            }
        };
    }


    @Override
    public Optional<Mod> getMod(String id) {
        if (this.mods.containsKey(id)) {
            return Optional.of(this.mods.get(id));
        }
        if (IXeoxLoader.get() == null) return Optional.empty();
        IMod mod = IXeoxLoader.get().getMod(id);
        if (mod != null) {
            XeoxMod value = new XeoxMod(mod);
            this.mods.put(id, value);
            return Optional.of(value);
        }
        return Optional.empty();

    }

    @Override
    public boolean isModLoaded(String id) {
        return IXeoxLoader.get().isModLoaded(id);
    }

    @Override
    public Collection<? extends Mod> getMods() {
        var list = new ArrayList<Mod>();
        list.addAll(IXeoxLoader.get().getMods().stream().map(container -> this.mods.computeIfAbsent(container.modId(), v -> new XeoxMod(container))).collect(Collectors.toList()));
        return list;
    }

    @Override
    public void initMods() {
        CommonConstants.LOGGER.info("Initializing mods...");

        if (IXeoxLoader.get() == null) return;

        IXeoxLoader.get().invokeEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);
        IXeoxLoader.get().invokeEntrypoints("server", DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeServer);
    }

    @Override
    public boolean isDevEnvironment() {
        if (IXeoxLoader.get() == null) return false;
        return IXeoxLoader.get().isDevEnvironment();
    }

    @Override
    public void locateServerResources(QuantumServer server) {
        try {
            URL resource = server.getClass().getResource("/.quantum-server-resources");
            if (resource == null) {
                throw new GdxRuntimeException("Quantum Voxel resources unavailable!");
            }
            String path = resource.toString();

            if (path.startsWith("jar:")) {
                path = path.substring("jar:".length());
            }

            path = path.substring(0, path.lastIndexOf('/'));

            if (path.endsWith("!")) {
                path = path.substring(0, path.length() - 1);
            }

            server.getResourceManager().importPackage(new FileHandle(new File(new URI(path))));
        } catch (Exception e) {
            IPath rootPath = IXeoxLoader.get().getMod(CommonConstants.NAMESPACE).filesystem().path("/");
            try {
                server.getResourceManager().importPackage(new XeoxFileHandle(rootPath));
            } catch (IOException ex) {
                throw new GdxRuntimeException("Failed to import resources!", ex);
            }
        }
    }
}
