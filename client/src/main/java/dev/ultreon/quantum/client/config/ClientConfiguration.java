package dev.ultreon.quantum.client.config;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.text.LanguageManager;
import dev.ultreon.quantum.config.api.ConfigCategory;
import dev.ultreon.quantum.config.api.Configuration;
import dev.ultreon.quantum.config.api.props.ConfigProperty;
import dev.ultreon.quantum.config.crafty.ConfigInfo;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.util.NamespaceID;

/**
 * This is the client configuration.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class ClientConfiguration {
    private static final Configuration config = new Configuration("quantum-client");
    private static final ConfigCategory videoCat = config.createCategory("video");

    public static final ConfigProperty<Integer> renderDistance = videoCat.create("renderDistance", 128, 4, 256);
    public static final ConfigProperty<Integer> entityRenderDistance = videoCat.create("renderDistance", 16, 4, 32);
    public static final ConfigProperty<Boolean> fog = videoCat.create("fog", true);
    public static final ConfigProperty<Boolean> enableVsync = videoCat.create("vsync", true);
    public static final ConfigProperty<Integer> fpsLimit = videoCat.create("fpsLimit", 60, 10, 240);
    public static final ConfigProperty<Integer> fov = videoCat.create("fov", 70, 40, 120);
    public static final ConfigProperty<Integer> guiScale = videoCat.create("guiScale", 0, 0, 6);
    public static final ConfigProperty<Boolean> fullscreen = videoCat.create("fullscreen", false);
    public static final ConfigProperty<Boolean> enableDebugUtils = videoCat.create("enable4xScreenshot", GamePlatform.get().isDevEnvironment());
    public static final ConfigProperty<Boolean> showSunAndMoon = videoCat.create("showSunAndMoon", true);

    private static final ConfigCategory guiCat = videoCat.createCategory("gui");
    public static final ConfigProperty<Boolean> enableHud = guiCat.create("enableHud", true);
    public static final ConfigProperty<Boolean> enableFpsHud = guiCat.create("enableFpsHud", false);
    public static final ConfigProperty<Boolean> enable4xScreenshot = guiCat.create("enable4xScreenshot", false);
    public static final ConfigProperty<Boolean> enableCrosshair = guiCat.create("enableCrosshair", true);
    public static final ConfigProperty<Boolean> showMemoryUsage = guiCat.create("showMemoryUsage", GamePlatform.get().isDevEnvironment() && GamePlatform.get().isWindows());

    private static final ConfigCategory genericCat = config.createCategory("generic");
    public static final ConfigProperty<NamespaceID> language = genericCat.create("language", new NamespaceID("en_us"), id -> LanguageManager.INSTANCE.getLanguageIDs().contains(id));

    private static final ConfigCategory personalCat = config.createCategory("personalisation");
    public static final ConfigProperty<Boolean> diagonalFontShadow = personalCat.create("diagonalFontShadow", false);
    public static final ConfigProperty<Boolean> enforceUnicode = personalCat.create("enforceUnicode", false);
    public static final ConfigProperty<Float> blurRadius = personalCat.create("blurRadius", 16f, 1f, 120f);
    public static final ConfigProperty<Float> vignetteStrength = personalCat.create("blurRadius", .5f, 0, 1);

    private static final ConfigCategory gameplayCat = config.createCategory("gameplay");

    public static final ConfigProperty<Boolean> showOnlyCraftable = gameplayCat.create("showOnlyCraftable", false);
    public static final ConfigProperty<Boolean> firstPersonPlayerModel = gameplayCat.create("firstPersonPlayerModel", false);
    public static final ConfigProperty<Boolean> thirdpersonHotbar = gameplayCat.create("thirdpersonHotbar", false);

    private static final ConfigCategory accessibilityCat = config.createCategory("accessibility");
    public static final ConfigProperty<Boolean> vibration = accessibilityCat.create("vibration", true);
    public static final ConfigProperty<Boolean> closePrompt = accessibilityCat.create("closePrompt", false);

    private static final ConfigCategory privacyCat = config.createCategory("privacy");

    public static final ConfigProperty<Boolean> hideUsername = privacyCat.create("hideUsername", false);
    public static final ConfigProperty<Boolean> hideSkin = privacyCat.create("hideSkin", false);
    public static final ConfigProperty<Boolean> hideActivity = privacyCat.create("hideActivity", false);
    public static final ConfigProperty<Boolean> hideServerFromActivity = privacyCat.create("hideActivity", true);

    private static final ConfigCategory audioCat = config.createCategory("audio");
    public static final ConfigProperty<Boolean> enableSound = audioCat.create("enableSound", true);
    public static final ConfigProperty<Float> soundVolume = audioCat.create("soundVolume", 50f, 0f, 100f);


    private static final ConfigCategory cameraCat = config.createCategory("camera");
    public static final ConfigProperty<Float> cameraSensitivity = cameraCat.create("sensitivity", 0.5f, 0f, 2f);

    private static final ConfigCategory performanceCat = config.createCategory("performance");
    public static final ConfigProperty<Boolean> blurEnabled = performanceCat.create("uiBlurEnabled", false);

    private static final ConfigCategory networkCat = config.createCategory("network");
    public static final ConfigProperty<Integer> networkKeepAliveTime = networkCat.create("keepAlive", 10000, 1000, 120000);
    public static final ConfigProperty<Integer> networkTimeout = networkCat.create("timeout", 30000, 1000, 120000);

    private static final ConfigCategory inputCat = config.createCategory("input");
    private static final ConfigCategory controllerInputCat = inputCat.createCategory("input");
    public static final ConfigProperty<Float> controllerDeadZone = controllerInputCat.create("keepAlive", 0.2f, 0f, 1f);
    public static final ConfigProperty<Boolean> enableVirtualKeyboard = controllerInputCat.create("virtualKeyboard", true);
    public static final ConfigProperty<Integer> lodThreshold = videoCat.create("lodThreshold", 4, 4, 16);

    public static void load() {
        config.load();
    }

    public static void save() {
        config.save();;
    }
}
