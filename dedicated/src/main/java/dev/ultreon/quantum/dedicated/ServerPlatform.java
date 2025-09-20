package dev.ultreon.quantum.dedicated;

import dev.ultreon.quantum.*;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.platform.PlatformFeature;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.Env;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class ServerPlatform extends GamePlatform {
    private final Map<String, FabricMod> mods = new IdentityHashMap<>();
    private Cleaner cleaner = Cleaner.create();

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
        return Arrays.asList(); // Server doesn't support mouse
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
        if (FabricLoader.getInstance() == null) return GameMod.INSTANCE;
        return new FabricMod(FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow());
    }

    @Override
    public boolean isFeatureSupported(PlatformFeature platformFeature) {
        return false;
    }

    @Override
    public void load(ResourceManager resourceManager) {

    }

    @Override
    public <T> List<T> newConcurrentList() {
        return new CopyOnWriteArrayList<>();
    }

    @Override
    public <T> Set<T> newConcurrentSet() {
        return new CopyOnWriteArraySet<>();
    }

    @Override
    public void onClean(Object o, Runnable onClean) {
        cleaner.register(o, onClean);
    }

    @Override
    public <K, V> Map<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public Logger getLogger(String name) {
        return (level, message, t) -> {
            if (level == null) return;
            Logger logger = LoggerFactory.getLogger(name);

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
        if (FabricLoader.getInstance() == null) return Optional.empty();
        return FabricLoader.getInstance().getModContainer(id).map(modContainer -> {
            FabricMod value = new FabricMod(modContainer);
            this.mods.put(id, value);
            return value;
        });

    }

    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    @Override
    public Collection<? extends Mod> getMods() {
        List<FabricMod> result = new ArrayList<>();
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            FabricMod fabricMod = this.mods.computeIfAbsent(container.getMetadata().getId(), v -> new FabricMod(container));
            result.add(fabricMod);
        }
        return result;
    }

    @Override
    public void initMods() {
        CommonConstants.LOGGER.info("Initializing mods...");

        if (FabricLoader.getInstance() == null) return;

        FabricLoader.getInstance().invokeEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);
        FabricLoader.getInstance().invokeEntrypoints("server", DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeServer);
    }

    @Override
    public boolean isDevEnvironment() {
        if (FabricLoader.getInstance() == null) return false;
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Env getEnv() {
        return Env.SERVER;
    }

}
