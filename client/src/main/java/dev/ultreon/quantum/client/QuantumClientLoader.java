package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.CommonRegistries;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.LoadingContext;
import dev.ultreon.quantum.client.api.events.ClientLifecycleEvents;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.Fonts;
import dev.ultreon.quantum.client.gui.Hud;
import dev.ultreon.quantum.client.gui.Overlays;
import dev.ultreon.quantum.client.gui.debug.*;
import dev.ultreon.quantum.client.gui.overlay.ManualCrashOverlay;
import dev.ultreon.quantum.client.gui.overlay.OverlayManager;
import dev.ultreon.quantum.client.gui.screens.container.CrateScreen;
import dev.ultreon.quantum.client.gui.screens.container.InventoryScreen;
import dev.ultreon.quantum.client.gui.screens.settings.SettingsScreen;
import dev.ultreon.quantum.client.input.DesktopInput;
import dev.ultreon.quantum.client.input.GameInput;
import dev.ultreon.quantum.client.input.TouchscreenInput;
import dev.ultreon.quantum.client.item.ItemRenderer;
import dev.ultreon.quantum.client.model.model.Json5ModelLoader;
import dev.ultreon.quantum.client.particle.ClientParticleRegistry;
import dev.ultreon.quantum.client.particle.ParticleControllerRenderers;
import dev.ultreon.quantum.client.particle.ParticleControllers;
import dev.ultreon.quantum.client.particle.ParticleEmitters;
import dev.ultreon.quantum.client.registry.LanguageRegistry;
import dev.ultreon.quantum.client.registry.MenuRegistry;
import dev.ultreon.quantum.client.render.ShaderPrograms;
import dev.ultreon.quantum.client.render.shader.Shaders;
import dev.ultreon.quantum.client.resources.ResourceNotFoundException;
import dev.ultreon.quantum.client.text.LanguageManager;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.js.JsLoader;
import dev.ultreon.quantum.menu.MenuTypes;
import dev.ultreon.quantum.python.PyLoader;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.event.RegistryEvents;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Task;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.gen.biome.Biomes;

import java.util.*;

import static com.badlogic.gdx.math.MathUtils.ceil;

class QuantumClientLoader implements Runnable {
    private static void registerDebugPages() {
        ClientRegistries.DEBUG_PAGE.register(QuantumClient.id("simple"), new SimpleDebugPage());
        ClientRegistries.DEBUG_PAGE.register(QuantumClient.id("generic"), new GenericDebugPage());
        ClientRegistries.DEBUG_PAGE.register(QuantumClient.id("rendering"), new RenderingDebugPage());
        ClientRegistries.DEBUG_PAGE.register(QuantumClient.id("profiler"), new ProfilerDebugPage());
        ClientRegistries.DEBUG_PAGE.register(QuantumClient.id("chunk_info"), new ChunkInfoDebugPage());
        ClientRegistries.DEBUG_PAGE.register(QuantumClient.id("inspector"), new InspectorDebugPage());
    }

    @Override
    public void run() {
        QuantumClient client = QuantumClient.get();
        load(client);
    }

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

        client.loadingOverlay.setProgress(0.075F);

        Gdx.app.setApplicationLogger(new GdxSlf4jLogger());

        client.configDir = QuantumClient.createDir("config/");

        QuantumClient.createDir("screenshots/");
        QuantumClient.createDir("game-crashes/");
        QuantumClient.createDir("logs/");

        int scale = ClientConfig.guiScale;
        if (scale == 0) {
            client.setAutomaticScale(true);
        }
        client.setGuiScale(scale);

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        client.loadingOverlay.setProgress(0.15F);

        QuantumClient.LOGGER.info("Importing resources");
        client.getResourceManager().importModResources();

        client.loadingOverlay.setProgress(0.35F);

        QuantumClient.LOGGER.info("Generating bitmap fonts");
        var resource = client.getResourceManager().getResource(QuantumClient.id("texts/unicode.txt"));
        if (resource == null) {
            throw new ApplicationCrash(new CrashLog("Where are my symbols", new ResourceNotFoundException(QuantumClient.id("texts/unicode.txt"))));
        }

        client.crashOverlay = new ManualCrashOverlay(client);

        client.loadingOverlay.setProgress(0.7F);

        //----------------------
        // Setting up rendering
        //----------------------
        QuantumClient.LOGGER.info("Initializing rendering stuffs");
        QuantumClient.invokeAndWait(() -> {
            client.input = client.deferDispose(createInput(client));
        });
        Gdx.input.setInputProcessor(client.input);

        QuantumClient.LOGGER.info("Setting up HUD");
        client.hud = QuantumClient.invokeAndWait(() -> new Hud(client));

        QuantumClient.LOGGER.info("Setting up Debug Renderer");
        client.debugGui = new DebugOverlay(client);

        client.loadingOverlay.setProgress(0.83F);

        //--------------------------
        // Registering game content
        //--------------------------
        QuantumClient.LOGGER.info("Loading languages");
        loadLanguages(client);

        QuantumClient.LOGGER.info("Registering stuff");

        LoadingContext.withinContext(new LoadingContext(CommonConstants.NAMESPACE), () -> {
            Registries.nopInit();
            RegistryEvents.REGISTRY_CREATION.factory().onRegistryCreation();
        });

        LoadingContext.withinContext(new LoadingContext(CommonConstants.NAMESPACE), () -> {
            CommonRegistries.register();

            // Client registry
            Fonts.register();
            Overlays.init();
            Biomes.init();

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
                for (Registry<?> registry : Registry.getRegistries()) {
                    RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(id, registry);
                }
            });
        }

        for (var pyMod : PyLoader.getInstance().getMods()) {
            LoadingContext.withinContext(new LoadingContext(pyMod.id), () -> {
                for (Registry<?> registry : Registry.getRegistries()) {
                    RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(pyMod.id, registry);
                }
            });
        }

        for (var jsMod : JsLoader.getInstance().getMods()) {
            LoadingContext.withinContext(new LoadingContext(jsMod.name), () -> {
                for (Registry<?> registry : Registry.getRegistries()) {
                    RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(jsMod.name, registry);
                }
            });
        }

        Registry.freeze();

        for (Biome biome : Registries.BIOME.values()) {
            biome.buildLayers();
        }

        QuantumClient.LOGGER.info("Registering models");
        registerMenuScreens();
        RenderingRegistration.registerRendering(client);

        client.j5ModelLoader = new Json5ModelLoader(client.getResourceManager());

        QuantumClient.LOGGER.info("Reloading resources");
        client.reloadResources();

        client.loadingOverlay.setProgress(0.95F);

        //*
        //* Post-initialize game content
        //* Such as model baking and texture stitching
        //*
        QuantumClient.LOGGER.info("Stitching textures");
        stitchTextures();

        client.loadingOverlay.setProgress(0.98F);

        client.itemRenderer = QuantumClient.invokeAndWait(() -> new ItemRenderer(client));
        client.itemRenderer.registerModels(client.j5ModelLoader);
        client.itemRenderer.loadModels(client);

        if (client.deferredWidth != null && client.deferredHeight != null) {
            client.camera.viewportWidth = client.deferredWidth;
            client.camera.viewportHeight = client.deferredHeight;
            client.camera.update();
        }

        client.windowTex = client.getTextureManager().getTexture(QuantumClient.id("textures/gui/window.png"));

        client.loadingOverlay.setProgress(0.99F);

        ClientLifecycleEvents.CLIENT_STARTED.factory().onGameLoaded(client);

        ClientParticleRegistry.registerAll();
//        ClientParticleRegistry.loadAll(client.batches);

        client.loading = false;

        //*************//
        // Final stuff //
        //*************//
        QuantumClient.LOGGER.info("Opening title screen");

        if (client.imGui) {
            GamePlatform.get().setupImGui();
        }

        client.onReloadConfig();

        client.booted = true;

        client.loadingOverlay.setProgress(1.0F);

        client.bootTime = Duration.ofMilliseconds(System.currentTimeMillis() - QuantumClient.BOOT_TIMESTAMP);
        QuantumClient.LOGGER.info("Game booted in {}.", client.bootTime.toSimpleString());

        QuantumClient.invokeAndWait(new Task<>(QuantumClient.id("main/show_title_screen"), () -> {
            if (client.devWorld) {
                client.startDevWorld();
            }
        }));
        client.loadingOverlay = null;

        OverlayManager.resize(ceil(client.getWidth() / client.getGuiScale()), ceil(client.getHeight() / client.getGuiScale()));
    }

    private GameInput createInput(QuantumClient quantumClient) {
        if (GamePlatform.get().isMobile()) {
            return new TouchscreenInput(quantumClient, quantumClient.camera);
        }
        return new DesktopInput(quantumClient, quantumClient.camera);
    }

    private void loadLanguages(QuantumClient client) {
        var internal = QuantumClient.resource(new NamespaceID("languages.json5"));
        Json5Element parse = CommonConstants.JSON5.parse(internal.reader());
        Json5Object asJson5Object = parse.getAsJson5Object();

        Json5Array languagesJ5 = asJson5Object.get("Languages").getAsJson5Array();

        if (languagesJ5.isEmpty()) {
            registerLanguage(QuantumClient.id("en_us"), client);
            return;
        }

        List<String> languages = new ArrayList<>();

        for (var language : languagesJ5) {
            if (language == null) continue;
            if (!language.isJson5Primitive()) continue;
            if (language.getAsJson5Primitive().isString()) {
                languages.add(language.getAsString());
            }
        }

        for (var language : languages) {
            registerLanguage(QuantumClient.id(language), client);
        }

        LanguageRegistry.doRegistration(id -> registerLanguage(id, client));
    }

    private void registerLanguage(NamespaceID id, QuantumClient quantumClient) {
        var s = id.getPath().split("_", 2);
        var locale = s.length == 1 ? Locale.of(s[0]) : Locale.of(s[0], s[1]);
        LanguageManager.INSTANCE.register(locale, id);
        LanguageManager.INSTANCE.load(locale, id, quantumClient.getResourceManager());
    }

    private void registerMenuScreens() {
        MenuRegistry.registerScreen(MenuTypes.INVENTORY, InventoryScreen::new);
        MenuRegistry.registerScreen(MenuTypes.CRATE, CrateScreen::new);
    }

    private void stitchTextures() {
    }
}
