package dev.ultreon.quantum.android;

import android.content.Intent;
import android.hardware.SensorEvent;
import android.os.Looper;
import android.view.InputDevice;
import android.view.MotionEvent;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Version;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.android.log.AndroidLogger;
import dev.ultreon.quantum.client.ClientPlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.platform.GraphicsEngine;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.dedicated.JavaWebSocket;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.platform.PlatformFeature;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.Result;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class AndroidPlatform extends ClientPlatform {
    public static final int IMPORT_MOD_CODE = 0x00000001;
    private final Map<String, Mod> mods = new IdentityHashMap<>();
    private final AndroidLauncher launcher;
    final IntMap<InputDevice> devices = new IntMap<>();
    private final IntSet mice = new IntSet();
    private final IntSet keyboards = new IntSet();
    private final IntMap<MouseDevice> motions = new IntMap<MouseDevice>();
    private AndroidMouseDevice mouseDevice;
    private final Map<Integer, Device> gameDevices = new HashMap<>();
    private LibGDXGraphicsEngine graphicsEngine;

    AndroidPlatform(AndroidLauncher launcher) {
        super();
        this.launcher = launcher;

        this.mods.put(CommonConstants.NAMESPACE, new BuiltinAndroidMod(CommonConstants.NAMESPACE, "Quantum Voxel", BuildConfig.VERSION_NAME, "The game you are now playing", Arrays.asList("Ultreon Studios")));
        this.mods.put("gdx", new BuiltinAndroidMod("gdx", "libGDX", Version.VERSION, "The game framework used to make Quantum Voxel", Arrays.asList("libGDX")));
        this.mods.put("xeox", new BuiltinAndroidMod("xeox", "Xeox Loader", "0.1.0", "The modloader for Quantum Voxel on Android", Arrays.asList("Ultreon Studios")));
    }

    void launch(AndroidLauncher launcher, ApplicationListener app, AndroidApplicationConfiguration config) {
        this.graphicsEngine = new LibGDXGraphicsEngine(this, app);
        this.graphicsEngine.start(() -> {
            launcher.initialize(app, config);
            return launcher;
        });
    }

    @Override
    public Collection<? extends Mod> getMods() {
        return new ArrayList<Mod>(this.mods.values());
    }

    @Override
    public WebSocket newWebSocket(String location, Consumer<Throwable> onError, WebSocket.InitializeListener initializeListener, WebSocket.ConnectedListener connectedListener) {
        return new JavaWebSocket(location, onError, initializeListener, connectedListener);
    }

    @Override
    public Optional<Mod> getMod(String id) {
        return Optional.ofNullable(this.mods.get(id));
    }

    @Override
    public boolean isModLoaded(String id) {
        return this.mods.containsKey(id);
    }

    @Override
    public <T> void invokeEntrypoint(String name, Class<T> initClass, Consumer<T> init) {
        // TODO: Implement
    }

    @Override
    public boolean isMobile() {
        return true;
    }

    @Override
    public Result<Boolean> openImportDialog() {
        launcher.runOnUiThread(() -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("application/zip");
//            startActivityForResult(
//                    this.launcher,
//                    Intent.createChooser(chooseFile, "Choose mod file"),
//                    IMPORT_MOD_CODE,
//                    null
//            );

            // No activities for you!
        });
        return Result.ok(false);
    }

    @Override
    public void prepare() {
        super.prepare();

        Looper.prepare();
    }

    @Override
    public void locateResources() {
        QuantumClient.get().resourceManager.importDeferredPackage(QuantumClient.class);
    }

    @Override
    public void locateServerResources(QuantumServer server) {
        server.getResourceManager().importDeferredPackage(QuantumServer.class);
    }

    @Override
    public Logger getLogger(String name) {
        return new AndroidLogger(name);
    }

    @Override
    public boolean detectDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    public GameWindow createWindow() {
        return new AndroidWindow();
    }

    @Override
    public boolean isDevEnvironment() {
        return detectDebug();
    }

    public void handleSensorChange(SensorEvent event) {
        // TODO: Implement if needed
    }

    public void setRotationLock(boolean b) {
        launcher.setRotationLock(b);
    }

    public void sendNotification(String tag, String quantum, String quantumRequiresAttention) {
        launcher.sendNotification(tag, quantum, quantumRequiresAttention);
    }

    public void addMouseDevice(int deviceId, InputDevice inputDevice) {
        this.devices.put(deviceId, inputDevice);

        if (inputDevice.supportsSource(InputDevice.SOURCE_MOUSE)) {
            this.mice.add(deviceId);

            InputDevice.MotionRange xMotion = inputDevice.getMotionRange(MotionEvent.AXIS_X);
            InputDevice.MotionRange yMotion = inputDevice.getMotionRange(MotionEvent.AXIS_Y);

            if (xMotion != null && yMotion != null) {
                this.mouseDevice = new AndroidMouseDevice(xMotion, yMotion, deviceId);
                this.gameDevices.put(deviceId, this.mouseDevice);
            }
        }
    }

    public void addKeyboardDevice(int deviceId, InputDevice inputDevice) {
        this.devices.put(deviceId, inputDevice);
    }

    public void removeMouseDevice(int deviceId) {
        this.devices.remove(deviceId);
    }

    public boolean isMouseDevice(int deviceId) {
        return this.mice.contains(deviceId);
    }

    public boolean isKeyboardDevice(int deviceId) {
        return this.keyboards.contains(deviceId);
    }

    @Override
    public AndroidMouseDevice getMouseDevice() {
        return mouseDevice;
    }

    @Override
    public boolean isMouseCaptured() {
        return launcher.isMouseCaptured();
    }

    @Override
    public void setMouseCaptured(boolean captured) {
        launcher.setMouseCaptured(captured);
    }

    public void removeDevice(int deviceId) {
        this.devices.remove(deviceId);
        this.keyboards.remove(deviceId);
        this.mice.remove(deviceId);
        this.gameDevices.remove(deviceId);
    }

    @Override
    public Collection<Device> getGameDevices() {
        return gameDevices.values();
    }

    @Override
    public void setCursorPosition(int x, int y) {
        launcher.setCursorPosition(x, y);
    }

    @Override
    public DeviceType getDeviceType() {
        return launcher.getDeviceType();
    }

    @Override
    public boolean isAngleGLES() {
        return true;
    }

    @Override
    public boolean isGLES() {
        return true;
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
        resourceManager.importDeferredPackage(AndroidLauncher.class);
    }

    @Override
    public void handleCrash(ApplicationCrash crash) {
        crash.printCrash();
        System.exit(1);
    }

    @Override
    public long[] getUuidElements(UUID value) {
        return new long[]{
                value.getMostSignificantBits(),
                value.getLeastSignificantBits()
        };
    }

    @Override
    public UUID constructUuid(long msb, long lsb) {
        return new UUID(msb, lsb);
    }

    @Override
    public boolean isLowPowerDevice() {
        return true;
    }

    @Override
    public boolean isFeatureSupported(PlatformFeature platformFeature) {
        switch (platformFeature) {
            case ClassLoading:
            case JsBytecode:
                return false;
            case JsInterop:
                return true;
        }

        return false;
    }

    @Override
    public void load(ResourceManager resourceManager) {

    }

    @Override
    public boolean cancelControllerVibration() {
        super.cancelControllerVibration();

        Controller current = Controllers.getCurrent();
        if (current == null) return false;
        current.cancelVibration();
        return true;
    }

    @Override
    public boolean startControllerVibration(int duration, float strength) {
        super.startControllerVibration(duration, strength);

        Controller current = Controllers.getCurrent();
        if (current == null) return false;
        current.startVibration(duration, Mth.clamp(strength, 0.0F, 1.0F));
        return true;
    }

    @Override
    public <T> List<T> createSyncList() {
        return new CopyOnWriteArrayList<>();
    }

    @Override
    public GraphicsEngine getGraphicsEngine() {
        return graphicsEngine;
    }
}
