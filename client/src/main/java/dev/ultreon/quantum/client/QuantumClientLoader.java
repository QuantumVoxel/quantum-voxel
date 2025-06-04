package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.CommonRegistries;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.LoadingContext;
import dev.ultreon.quantum.client.api.events.ClientLifecycleEvents;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.Fonts;
import dev.ultreon.quantum.client.gui.Hud;
import dev.ultreon.quantum.client.gui.Overlays;
import dev.ultreon.quantum.client.gui.debug.*;
import dev.ultreon.quantum.client.gui.overlay.ManualCrashOverlay;
import dev.ultreon.quantum.client.gui.overlay.OverlayManager;
import dev.ultreon.quantum.client.gui.screens.BuilderInventoryScreen;
import dev.ultreon.quantum.client.gui.screens.TitleScreen;
import dev.ultreon.quantum.client.gui.screens.container.AdvancedCraftingScreen;
import dev.ultreon.quantum.client.gui.screens.container.BlastFurnaceScreen;
import dev.ultreon.quantum.client.gui.screens.container.CrateScreen;
import dev.ultreon.quantum.client.gui.screens.container.InventoryScreen;
import dev.ultreon.quantum.client.input.KeyAndMouseInput;
import dev.ultreon.quantum.client.input.TouchInput;
//import dev.ultreon.quantum.client.input.controller.ControllerInput;
//import dev.ultreon.quantum.client.input.controller.gui.VirtualKeyboard;
import dev.ultreon.quantum.client.item.ItemRenderer;
import dev.ultreon.quantum.client.model.model.JsonModelLoader;
import dev.ultreon.quantum.client.particle.ClientParticleRegistry;
import dev.ultreon.quantum.client.particle.ParticleControllerRenderers;
import dev.ultreon.quantum.client.particle.ParticleControllers;
import dev.ultreon.quantum.client.particle.ParticleEmitters;
import dev.ultreon.quantum.client.registry.LanguageRegistry;
import dev.ultreon.quantum.client.registry.MenuRegistry;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.render.ShaderPrograms;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.client.text.LanguageManager;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.menu.MenuTypes;
import dev.ultreon.quantum.registry.SimpleRegistry;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.event.RegistryEvents;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Task;

import java.util.*;

import static com.badlogic.gdx.math.MathUtils.ceil;

/**
 * A class that loads the QuantumClient.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */ 
class QuantumClientLoader implements Runnable {
    /**
     * Registers the debug pages.
     */
    private static void registerDebugPages() {
        ClientRegistries.DEBUG_PAGE.register(NamespaceID.of("simple"), new SimpleDebugPage());
        ClientRegistries.DEBUG_PAGE.register(NamespaceID.of("generic"), new GenericDebugPage());
        ClientRegistries.DEBUG_PAGE.register(NamespaceID.of("timings"), new TimingsDebugPage());
        ClientRegistries.DEBUG_PAGE.register(NamespaceID.of("value_tracker"), new ValueTrackerPage());
        ClientRegistries.DEBUG_PAGE.register(NamespaceID.of("rendering"), new RenderingDebugPage());
        if (!GamePlatform.get().isWeb()) ClientRegistries.DEBUG_PAGE.register(NamespaceID.of("profiler"), new ProfilerDebugPage());
        ClientRegistries.DEBUG_PAGE.register(NamespaceID.of("chunk_info"), new ChunkInfoDebugPage());
    }

    /**
     * Runs the QuantumClientLoader.
     */
    @Override
    public void run() {
        try {
            QuantumClient client = QuantumClient.get();
            load(client);
        } catch (Throwable e) {
            QuantumClient.LOGGER.error("Failed to load QuantumClient", e);
            QuantumClient.LOGGER.error("QuantumClient will be shut down");
            QuantumClient.crash(e);
        }
    }

    /**
     * Loads the QuantumClient.
     * 
     * @param client The QuantumClient.
     */
    @SuppressWarnings("UnstableApiUsage")
    void load(QuantumClient client) {
        var argList = Arrays.asList(client.argv);
        client.isDevMode = argList.contains("--dev") && GamePlatform.get().isDevEnvironment();

        if (GamePlatform.get().isDevEnvironment()) client.gameEnv = GameEnvironment.DEVELOPMENT;
        else if (Objects.equals(System.getProperty("quantum.environment", "normal"), "packaged"))
            client.gameEnv = GameEnvironment.PACKAGED;
        else client.gameEnv = GameEnvironment.NORMAL;

        if (client.isDevMode) QuantumClient.LOGGER.info("Developer mode is enabled");

//        Thread.setDefaultUncaughtExceptionHandler(QuantumClient::uncaughtException);

        progress(client, 0.075F);

        Gdx.app.setApplicationLogger(new GdxSlf4jLogger());

        client.configDir = QuantumClient.createDir("config/");

        QuantumClient.createDir("screenshots/");
        QuantumClient.createDir("game-crashes/");
        QuantumClient.createDir("logs/");

        int scale = ClientConfiguration.guiScale.getValue();
        if (scale == 0) {
            client.setAutomaticScale(true);
        }
        client.setGuiScale(scale);

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        progress(client, 0.15F);

        float progress = 0.35F;
        progress(client, progress);

        QuantumClient.LOGGER.info("Generating bitmap fonts");

        client.crashOverlay = new ManualCrashOverlay(client);

        progress(client, 0.7F);

        //----------------------
        // Setting up rendering
        //----------------------
        QuantumClient.LOGGER.info("Initializing rendering stuffs");
        QuantumClient.invokeAndWait(() -> {
            client.keyAndMouseInput = new KeyAndMouseInput(client, client.camera);
//            client.controllerInput = new ControllerInput(client, client.camera);
            client.touchInput = new TouchInput(client, client.camera);

//            client.virtualKeyboard = new VirtualKeyboard();
        });
        Gdx.input.setInputProcessor(GamePlatform.get().isMobile() ? client.touchInput : client.keyAndMouseInput);

        QuantumClient.LOGGER.info("Setting up HUD");
        client.hud = QuantumClient.invokeAndWait(() -> new Hud(client));

        QuantumClient.LOGGER.info("Setting up Debug Renderer");
        client.debugGui = new DebugOverlay(client);

        progress(client, 0.83F);

        //--------------------------
        // Registering game content
        //--------------------------
        QuantumClient.LOGGER.info("Loading languages");
        loadLanguages(client);

        QuantumClient.LOGGER.info("Registering stuff");


        LoadingContext.withinContext(new LoadingContext(CommonConstants.NAMESPACE), () -> {
            RenderPass.nopInit();
            Registries.nopInit();
//            RegistryEvents.REGISTRY_CREATION.factory().onRegistryCreation();
        });

        LoadingContext.withinContext(new LoadingContext(CommonConstants.NAMESPACE), () -> {
            CommonRegistries.register();

            // Client registry
            Overlays.init();

            // Stuff that needs to be initialized on the render thread
            QuantumClient.invokeAndWait(() -> {
                Shaders.init();
                ShaderPrograms.init();
                ParticleEmitters.init();
                ParticleControllerRenderers.init();
                ParticleControllers.init();
            });

            // Register debug pages
            QuantumClientLoader.registerDebugPages();
        });

        for (var mod : GamePlatform.get().getMods()) {
            final String id = mod.getName();
            LoadingContext.withinContext(new LoadingContext(id), () -> {
                for (Registry<?> registry : SimpleRegistry.getRegistries()) {
                    RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(id, registry);
                }
            });
        }

        Registry.freeze();

        QuantumClient.LOGGER.info("Registering models");
        registerMenuScreens();
        RenderingRegistration.registerRendering(client);

        client.j5ModelLoader = new JsonModelLoader(client.getResourceManager());

        QuantumClient.LOGGER.info("Reloading resources");
        client.reloadResources();

        QuantumClient.LOGGER.info("Loading fonts");
        Fonts.register();

        progress(client, 0.95F);

        //*
        //* Post-initialize game content
        //* Such as model baking and texture stitching
        //*
        QuantumClient.LOGGER.info("Stitching textures");
        stitchTextures();

        progress(client, 0.98F);

        client.itemRenderer = QuantumClient.invokeAndWait(() -> new ItemRenderer(client));

        if (client.deferredWidth != null && client.deferredHeight != null) {
            client.camera.viewportWidth = client.getWidth();
            client.camera.viewportHeight = client.getHeight();
            client.camera.update();
        }

        client.windowTex = client.getTextureManager().getTexture(NamespaceID.of("textures/gui/window.png"));

        progress(client, 0.99F);

        ClientLifecycleEvents.CLIENT_STARTED.factory().onGameLoaded(client);

        ClientParticleRegistry.registerAll();
//        ClientParticleRegistry.loadAll(client.batches);

        client.loading = false;

        //*************//
        // Final stuff //
        //*************//
        QuantumClient.LOGGER.info("Opening title screen");

        client.onReloadConfig();

        client.booted = true;

        progress(client, 1.0F);

        client.bootTime = Duration.ofMilliseconds(System.currentTimeMillis() - QuantumClient.BOOT_TIMESTAMP);
        QuantumClient.LOGGER.info("Game booted in {}.", client.bootTime.toSimpleString());

        QuantumClient.invokeAndWait(new Task<>(NamespaceID.of("main/show_title_screen"), () -> {
            if (client.devWorld) {
                client.startDevWorld();
            } else {
                client.showScreen(new DevPreviewScreen(client.getUser() != null ? new TitleScreen() : new UsernameScreen()));
                client.loadingOverlay = null;
            }
        }));

        OverlayManager.resize(ceil(client.getWidth() / client.getGuiScale()), ceil(client.getHeight() / client.getGuiScale()));
    }

    private static void progress(QuantumClient client, float progress) {
        if (client.loadingOverlay == null) {
            CommonConstants.LOGGER.warn("Loading overlay is null");
            return;
        }
        client.loadingOverlay.setProgress(progress);
    }

    /**
     * Creates a new KeyAndMouseInput.
     * 
     * @param quantumClient The QuantumClient.
     * @return The KeyAndMouseInput.
     */
    private KeyAndMouseInput createInput(QuantumClient quantumClient) {
        return new KeyAndMouseInput(quantumClient, quantumClient.camera);
    }

    /**
     * Loads the languages.
     * 
     * @param client The QuantumClient.
     */
    private void loadLanguages(QuantumClient client) {
        var internal = QuantumClient.resource(new NamespaceID("languages.quant"));
        JsonValue asJsonObject = CommonConstants.JSON_READ.parse(internal.reader());

        String[] languagesJ5 = asJsonObject.get("Languages").asStringArray();

        if (languagesJ5.length == 0) {
            registerLanguage(NamespaceID.of("en_us"), client);
            return;
        }

        List<String> languages = new ArrayList<>();

        for (var language : languagesJ5) {
            if (language == null) continue;
            languages.add(language);
        }

        for (var language : languages) {
            registerLanguage(NamespaceID.of(language), client);
        }

        LanguageRegistry.doRegistration(id -> registerLanguage(id, client));
    }

    /**
     * Registers a language.
     * 
     * @param id The ID of the language.
     * @param quantumClient The QuantumClient.
     */
    private void registerLanguage(NamespaceID id, QuantumClient quantumClient) {
        var s = id.getPath().split("_", 2);
        var locale = s.length == 1 ? new Locale(s[0]) : new Locale(s[0], s[1]);
        LanguageManager.INSTANCE.register(locale, id);
        LanguageManager.INSTANCE.load(locale, id, quantumClient.getResourceManager());
    }

    /**
     * Registers the menu screens.
     */
    private void registerMenuScreens() {
        MenuRegistry.registerScreen(MenuTypes.INVENTORY, (menu, title) -> {
            Player holder = menu.getHolder();
            if (holder.isBuilder()) {
                return new BuilderInventoryScreen(menu);
            } else {
                return new InventoryScreen(menu, title);
            }
        });
        MenuRegistry.registerScreen(MenuTypes.ADVANCED_CRAFTING, AdvancedCraftingScreen::new);
        MenuRegistry.registerScreen(MenuTypes.CRATE, CrateScreen::new);
        MenuRegistry.registerScreen(MenuTypes.BLAST_FURNACE, BlastFurnaceScreen::new);
    }

    /**
     * Stitches the textures.
     */
    private void stitchTextures() {
    }
}
