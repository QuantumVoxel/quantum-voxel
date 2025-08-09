package dev.ultreon.quantum.dedicated;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.platform.PlatformFeature;
import dev.ultreon.quantum.resources.ResourceManager;
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
import java.util.function.Consumer;

public class ServerPlatform extends GamePlatform {
    private final Map<String, XeoxMod> mods = new IdentityHashMap<>();

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
    public int cpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public void locateContentResources(ResourceManager resourceManager) {
        try {
            resourceManager.loadFromAssetsTxt(Gdx.files.internal("assets.txt"));
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to load assets.txt", e);
        }
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

    @SuppressWarnings({"DuplicateBranchesInSwitch", "ConstantValue"})
    @Override
    public boolean isFeatureSupported(PlatformFeature platformFeature) {
        return switch (platformFeature) {
            case JsInterop -> true;
            case ClassLoading -> true;
            case JsBytecode -> true;
        };
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
        IMod iMod = IXeoxLoader.get().getMod(id);
        if (iMod == null) {
            XeoxMod mod = this.mods.get(id);
            if (mod != null) {
                return Optional.of(mod);
            }
            return Optional.empty();
        }
        return Optional.of(new XeoxMod(iMod));
    }

    @Override
    public boolean isModLoaded(String id) {
        return IXeoxLoader.get().getMod(id) != null || this.mods.containsKey(id);
    }

    @Override
    public Collection<? extends Mod> getMods() {
        return new ArrayList<>(this.mods.values());
    }

    @Override
    public void initMods() {
        CommonConstants.LOGGER.info("Initializing mods...");

        IXeoxLoader.get().invokeEntrypoints("common", ModInitializer.class, ModInitializer::onInitialize);
        IXeoxLoader.get().invokeEntrypoints("server", DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeServer);
    }

    @Override
    public boolean isDevEnvironment() {
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
            IPath path = IXeoxLoader.get().getMod(CommonConstants.NAMESPACE).filesystem().path("/");
            try {
                server.getResourceManager().importPackage(new XeoxFileHandle(path));
            } catch (IOException ex) {
                CommonConstants.LOGGER.error("Failed to import resources!", ex);
                throw new GdxRuntimeException("Quantum Voxel resources unavailable!");
            }
        }
    }

    @Override
    public void locateModResources() {
        for (IMod mod : IXeoxLoader.get().getMods()) {
            IPath path = mod.filesystem().path("/");
            try {
                QuantumServer.get().getResourceManager().importPackage(new XeoxFileHandle(path));
            } catch (IOException ex) {
                CommonConstants.LOGGER.error("Failed to import resources!", ex);
            }
            this.mods.put(mod.modId(), new XeoxMod(mod));
        }
    }
}
