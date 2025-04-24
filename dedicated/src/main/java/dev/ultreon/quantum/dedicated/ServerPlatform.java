package dev.ultreon.quantum.dedicated;

import dev.ultreon.quantum.*;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServerPlatform extends GamePlatform {
    private final Map<String, FabricMod> mods = new IdentityHashMap<>();

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
    public long[] getUuidElements(UUID value) {
        return new long[]{value.getMostSignificantBits(), value.getLeastSignificantBits()};
    }

    @Override
    public UUID constructUuid(long msb, long lsb) {
        return new UUID(msb, lsb);
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
        return FabricLoader.getInstance().getModContainer(id).map(container -> (Mod) this.mods.computeIfAbsent(id, v -> new FabricMod(container))).or(() -> super.getMod(id));
    }

    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id) || super.isModLoaded(id);
    }

    @Override
    public Collection<? extends Mod> getMods() {
        var list = new ArrayList<Mod>();
        list.addAll(FabricLoader.getInstance().getAllMods().stream().map(container -> this.mods.computeIfAbsent(container.getMetadata().getId(), v -> new FabricMod(container))).collect(Collectors.toList()));
        list.addAll(super.getMods());
        return list;
    }

    @Override
    public boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
