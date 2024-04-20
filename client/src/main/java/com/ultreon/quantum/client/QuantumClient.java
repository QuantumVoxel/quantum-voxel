package com.ultreon.quantum.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.RestrictedApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ultreon.quantum.*;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.client.api.events.ClientLifecycleEvents;
import com.ultreon.quantum.client.api.events.ClientTickEvents;
import com.ultreon.quantum.client.api.events.RenderEvents;
import com.ultreon.quantum.client.api.events.gui.ScreenEvents;
import com.ultreon.quantum.client.atlas.TextureAtlas;
import com.ultreon.quantum.client.audio.ClientSound;
import com.ultreon.quantum.client.config.Config;
import com.ultreon.quantum.client.config.GameSettings;
import com.ultreon.quantum.client.font.Font;
import com.ultreon.quantum.client.gui.*;
import com.ultreon.quantum.client.gui.debug.*;
import com.ultreon.quantum.client.gui.overlay.LoadingOverlay;
import com.ultreon.quantum.client.gui.overlay.ManualCrashOverlay;
import com.ultreon.quantum.client.gui.overlay.OverlayManager;
import com.ultreon.quantum.client.gui.screens.*;
import com.ultreon.quantum.client.gui.screens.container.CrateScreen;
import com.ultreon.quantum.client.gui.screens.container.InventoryScreen;
import com.ultreon.quantum.client.imgui.ImGuiOverlay;
import com.ultreon.quantum.client.init.Fonts;
import com.ultreon.quantum.client.init.Overlays;
import com.ultreon.quantum.client.init.ShaderPrograms;
import com.ultreon.quantum.client.init.Shaders;
import com.ultreon.quantum.client.input.DesktopInput;
import com.ultreon.quantum.client.input.GameCamera;
import com.ultreon.quantum.client.input.GameInput;
import com.ultreon.quantum.client.input.PlayerInput;
import com.ultreon.quantum.client.item.ItemRenderer;
import com.ultreon.quantum.client.management.*;
import com.ultreon.quantum.client.model.block.BakedModelRegistry;
import com.ultreon.quantum.client.model.block.BlockModel;
import com.ultreon.quantum.client.model.block.BlockModelRegistry;
import com.ultreon.quantum.client.model.model.Json5ModelLoader;
import com.ultreon.quantum.client.multiplayer.MultiplayerData;
import com.ultreon.quantum.client.network.LoginClientPacketHandlerImpl;
import com.ultreon.quantum.client.network.system.ClientTcpConnection;
import com.ultreon.quantum.client.player.LocalPlayer;
import com.ultreon.quantum.client.player.SkinManager;
import com.ultreon.quantum.client.registry.*;
import com.ultreon.quantum.client.render.pipeline.*;
import com.ultreon.quantum.client.render.shader.GameShaderProvider;
import com.ultreon.quantum.network.MemoryConnectionContext;
import com.ultreon.quantum.network.MemoryNetworker;
import com.ultreon.quantum.network.client.ClientPacketHandler;
import com.ultreon.quantum.network.server.ServerPacketHandler;
import com.ultreon.quantum.resources.ReloadContext;
import com.ultreon.quantum.client.resources.ResourceLoader;
import com.ultreon.quantum.client.resources.ResourceNotFoundException;
import com.ultreon.quantum.client.rpc.GameActivity;
import com.ultreon.quantum.client.rpc.RpcHandler;
import com.ultreon.quantum.client.sound.ClientSoundRegistry;
import com.ultreon.quantum.client.text.Language;
import com.ultreon.quantum.client.text.LanguageManager;
import com.ultreon.quantum.client.texture.TextureManager;
import com.ultreon.quantum.client.util.DeferredDisposable;
import com.ultreon.quantum.client.util.GG;
import com.ultreon.quantum.client.util.Resizer;
import com.ultreon.quantum.client.world.ClientWorld;
import com.ultreon.quantum.client.world.WorldRenderer;
import com.ultreon.quantum.crash.ApplicationCrash;
import com.ultreon.quantum.crash.CrashCategory;
import com.ultreon.quantum.crash.CrashLog;
import com.ultreon.quantum.debug.DebugFlags;
import com.ultreon.quantum.debug.Debugger;
import com.ultreon.quantum.debug.inspect.InspectionNode;
import com.ultreon.quantum.debug.inspect.InspectionRoot;
import com.ultreon.quantum.debug.profiler.Profiler;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.item.Item;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.item.tool.ToolItem;
import com.ultreon.quantum.menu.MenuTypes;
import com.ultreon.quantum.network.system.IConnection;
import com.ultreon.quantum.network.packets.c2s.C2SLoginPacket;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.registry.Registry;
import com.ultreon.quantum.registry.event.RegistryEvents;
import com.ultreon.quantum.resources.ResourceManager;
import com.ultreon.quantum.server.QuantumServer;
import com.ultreon.quantum.sound.event.SoundEvents;
import com.ultreon.quantum.text.LanguageBootstrap;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.util.*;
import com.ultreon.quantum.world.*;
import com.ultreon.quantum.world.gen.biome.Biomes;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.libs.datetime.v0.Duration;
import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import net.fabricmc.loader.api.FabricLoader;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.earlygrey.shapedrawer.ShapeDrawer;

import javax.annotation.WillClose;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.*;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.math.MathUtils.ceil;

@SuppressWarnings("UnusedReturnValue")
public class QuantumClient extends PollingExecutorService implements DeferredDisposable {
    @SuppressWarnings("removal")
    @Deprecated(since = "0.1.0", forRemoval = true)
    public static final String NAMESPACE = QuantumServer.NAMESPACE;
    public static final Logger LOGGER = LoggerFactory.getLogger("QuantumClient");
    public static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setPrettyPrinting().create();
    public static final int[] SIZES = new int[]{16, 24, 32, 40, 48, 64, 72, 80, 96, 108, 128, 160, 192, 256, 1024};
    public static final float FROM_ZOOM = 2.0f;
    public static final float TO_ZOOM = 1.3f;
    private static final float DURATION = 6000f;
    private static final int MINIMUM_WIDTH = 800;
    private static final int MINIMUM_HEIGHT = 600;
    @SuppressWarnings("GDXJavaStaticResource")
    public static final Profiler PROFILER = new Profiler();
    private static ArgParser arguments;
    private static boolean crashing;
    private final Cursor normalCursor;
    private final Cursor clickCursor;
    private final RenderPipeline pipeline;
    public final Renderer renderer;
    public final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    public IConnection<ClientPacketHandler, ServerPacketHandler> connection;
    public ServerData serverData;
    public ExecutorService chunkLoadingExecutor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 3, 1), r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("ChunkLoadingExecutor");
        return thread;
    });
    private final boolean devWorld;
    private boolean imGui = false;
    public InspectionRoot<QuantumClient> inspection;
    public Config newConfig;
    public boolean hideHud = false;

    private Duration bootTime;
    private GarbageCollector garbageCollector;
    private GameEnvironment gameEnv;
    private final Sound logoRevealSound;
    private final Texture ultreonBgTex;
    private final Texture ultreonLogoTex;
    private final Texture libGDXLogoTex;
    private final Resizer resizer;
    private boolean showUltreonSplash = false;
    private boolean showLibGDXSplash = !FabricLoader.getInstance().isDevelopmentEnvironment();
    private long ultreonSplashTime;
    private long libGDXSplashTime;
    public FileHandle configDir;
    public Metadata metadata = Metadata.load();

    private static final String FATAL_ERROR_MSG = "Fatal error occurred when handling crash:";
    public boolean forceUnicode = false;
    public ItemRenderer itemRenderer;
    public NotifyManager notifications = new NotifyManager(this);
    @SuppressWarnings("FieldMayBeFinal")
    private boolean booted;
    public final Font font;
    public final Font newFont;
    @UnknownNullability
    public BitmapFont unifont;
    public GameInput input;
    @Nullable
    public ClientWorld world;
    @Nullable
    public WorldRenderer worldRenderer;
    @UnknownNullability
    @SuppressWarnings("GDXJavaStaticResource")
    private static QuantumClient instance;
    @Nullable
    public LocalPlayer player;
    @NotNull
    public final SpriteBatch spriteBatch;
    public final ModelBatch modelBatch;
    public final GameCamera camera;
    public final PlayerInput playerInput = new PlayerInput(this);
    private boolean isDevMode;
    @Nullable
    public Screen screen;
    @Deprecated
    public GameSettings settings = new GameSettings();
    public final ShapeDrawer shapes;
    private final TextureManager textureManager;
    private final CubemapManager cubemapManager;
    private final ResourceManager resourceManager;
    public final EntityModelRegistry entityModelManager;
    public final EntityRendererRegistry entityRendererManager;
    private float guiScale = this.calcMaxGuiScale();

    public Hud hud;
    public HitResult hitResult;
    private Vec3i breaking;
    private BlockProperties breakingBlock;

    // Public Flags
    public boolean renderWorld = false;

    // Startup time
    public static final long BOOT_TIMESTAMP = System.currentTimeMillis();

    // Texture Atlases
    @UnknownNullability
    public TextureAtlas blocksTextureAtlas;
    public TextureAtlas emmisiveTextureAtlas;
    @UnknownNullability
    public TextureAtlas itemTextureAtlas;
    private BakedModelRegistry bakedBlockModels;

    // Advanced Shadows
    private final List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("QVoxelClientScheduler");
        return thread;
    });
    @Nullable
    private Integer deferredWidth;
    @Nullable
    private Integer deferredHeight;
    private Texture windowTex;
    public DebugOverlay debugGui;
    private boolean closingWorld;
    private int oldSelected;
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private final List<Shutdownable> shutdownables = new CopyOnWriteArrayList<>();
    private final List<AutoCloseable> closeables = new CopyOnWriteArrayList<>();
    private boolean loading = true;
    private final Thread mainThread;
    public HitResult cursor;
    private LoadingOverlay loadingOverlay;
    private final String[] argv;
    private final ClientSoundRegistry soundRegistry = new ClientSoundRegistry();
    public IntegratedServer integratedServer;
    private final User user;
    private int currentTps;
    private float tickTime = 0f;
    public float partialTick = 0f;
    public float frameTime = 0f;
    private int ticksPassed = 0;

    double time = System.currentTimeMillis();
    private GameActivity activity = null;
    private GameActivity oldActivity = null;
    private Vec2i oldMode;
    private boolean isInThirdPerson;
    private boolean triggerScreenshot;
    private boolean captureScreenshot;
    public int screenshotScale = 4;
    private final GameRenderer gameRenderer;
    private FrameBuffer fbo;
    private int width;
    private int height;
    private MultiplayerData multiplayerData;
    ManualCrashOverlay crashOverlay;
    private boolean wasClicking;
    private final Queue<Runnable> serverTickQueue = new ArrayDeque<>();
    private boolean startDevLoading = true;
    private final Environment defaultEnv = new Environment();
    private boolean autoScale;
    private boolean disposed;
    private final GameWindow window;

    @ApiStatus.Experimental
    private static Callback<CrashLog> crashHook;
    private final List<CrashLog> crashes = new CopyOnWriteArrayList<>();
    private long screenshotFlashTime;
    private final Color tmpColor = new Color();
    private final SkinManager skinManager = new SkinManager();
    private CompletableFuture<Screenshot> screenshotFuture;
    private final MaterialManager materialManager;
    private final ShaderProviderManager shaderProviderManager;
    private final ShaderProgramManager shaderProgramManager;
    private final TextureAtlasManager textureAtlasManager;
    private final FontManager fontManager = new FontManager();
    private final Queue<Disposable> disposalQueue = new ArrayDeque<>();

    QuantumClient(String[] argv) {
        super(QuantumClient.PROFILER);

        // Disable shader pedantic mode
        ShaderProgram.pedantic = false;

        // Add a shutdown hook to gracefully shut down the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (this.integratedServer != null) {
                    this.integratedServer.shutdown();
                }
            } catch (Exception e) {
                QuantumClient.LOGGER.error("Failed to shutdown server", e);
            }

            QuantumClient.LOGGER.info("Shutting down game!");
            QuantumClient.instance = null;
        }));

        // Log the booting of the game
        QuantumClient.LOGGER.info("Booting game!");
        QuantumClient.instance = this;

        // Initialize the unifont and font
        this.unifont = deferDispose(new BitmapFont(Gdx.files.internal("assets/quantum/font/unifont/unifont.fnt"), true));
        this.font = new Font(new BitmapFont(Gdx.files.internal("assets/quantum/font/quantium.fnt"), true));
        this.newFont = new Font(new BitmapFont(Gdx.files.internal("assets/quantum/font/quantium.fnt"), true));

        // Initialize the game window
        this.window = new GameWindow();

        // Initialize the inspection root
        this.inspection = deferDispose(new InspectionRoot<>(this));

        // Initialize the resource manager, texture manager, and resource loader
        this.resourceManager = new ResourceManager("assets");

        // Initialize shader provider and shader program manager. These should be initialized after the resource manager.
        this.shaderProviderManager = new ShaderProviderManager();
        this.shaderProgramManager = new ShaderProgramManager();

        // Locate resources by finding the ".ucraft-resources" file using Class.getResource() and using the parent file.
        try {
            URL resource = QuantumClient.class.getResource("/.ucraft-resources");
            if (resource == null) {
                throw new GdxRuntimeException("Quantum Voxel resources unavailable!");
            }
            String string = resource.toString();

            if (string.startsWith("jar:")) {
                string = string.substring("jar:".length());
            }

            string = string.substring(0, string.lastIndexOf('/'));

            if (string.endsWith("!")) {
                string = string.substring(0, string.length() - 1);
            }

            this.resourceManager.importPackage(new File(new URI(string)).toPath());
        } catch (Exception e) {
            for (Path rootPath : FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow().getRootPaths()) {
                try {
                    this.resourceManager.importPackage(rootPath);
                } catch (IOException ex) {
                    crash(ex);
                }
            }
        }

        // Set the language bootstrap
        LanguageBootstrap.bootstrap.set(Language::translate);

        this.textureManager = new TextureManager(this.resourceManager);
        this.cubemapManager = new CubemapManager(this.resourceManager);
        this.materialManager = new MaterialManager(this.resourceManager, this.textureManager, this.cubemapManager);
        this.textureAtlasManager = new TextureAtlasManager(this);
        ResourceLoader.init(this);

        // Load the configuration
        ModLoadingContext.withinContext(FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow(), () -> {
            this.newConfig = new Config();
            this.newConfig.event.subscribe(this::onReloadConfig);
            this.newConfig.load();
        });

        // Register auto fillers for debugging
        DebugRegistration.registerAutoFillers();

        this.argv = argv;

        // Set the flag for the development world
        this.devWorld = QuantumClient.arguments.getFlags().contains("devWorld");

        // Create a new user
        this.user = new User("Player" + MathUtils.random(100, 999));

        this.mainThread = Thread.currentThread();

        // Initialize ImGui if necessary
        this.imGui = !SharedLibraryLoader.isMac && !SharedLibraryLoader.isAndroid && !SharedLibraryLoader.isARM && !SharedLibraryLoader.isIos;
        if (this.imGui)
            ImGuiOverlay.preInitImGui();

        // Initialize the model loader
        G3dModelLoader modelLoader = new G3dModelLoader(new JsonReader());
        this.entityModelManager = new EntityModelRegistry(modelLoader, this);
        this.entityRendererManager = new EntityRendererRegistry(this.entityModelManager);

        // Initialize the game camera
        this.camera = new GameCamera(67, this.getWidth(), this.getHeight());
        this.camera.near = 0.1f;
        this.camera.far = 2;

        // Initialize the render pipeline
        this.pipeline = deferDispose(new RenderPipeline(new MainRenderNode(), this.camera)
//                .node(new SkyboxNode())
                .node(new CollectNode())
                .node(new WorldDiffuseNode())
                .node(new SkyboxNode())
                .node(new MainRenderNode()));

        // Create a white pixel for the shape drawer
        Pixmap pixmap = deferDispose(new Pixmap(1, 1, Pixmap.Format.RGBA8888));
        pixmap.setColor(1F, 1F, 1F, 1F);
        pixmap.drawPixel(0, 0);
        TextureRegion white = new TextureRegion(new Texture(pixmap));

        // Initialize the sprite batch, shape drawer, and renderer
        this.spriteBatch = deferDispose(new SpriteBatch());
        this.shapes = new ShapeDrawer(this.spriteBatch, white);
        this.renderer = new Renderer(this.shapes);

        // Initialize DepthShader configuration
        DepthShader.Config shaderConfig = new DepthShader.Config();
        shaderConfig.defaultCullFace = GL_BACK;
        shaderConfig.defaultDepthFunc = GL_DEPTH_FUNC;

        // Initialize ModelBatch with GameShaderProvider
        this.modelBatch = deferDispose(new ModelBatch(new GameShaderProvider(shaderConfig)));

        // Initialize GameRenderer
        this.gameRenderer = new GameRenderer(this, this.modelBatch, this.pipeline);

        // Set up modifications
        this.setupMods();

        // Load textures
        this.ultreonBgTex = new Texture("assets/quantum/textures/gui/loading_overlay_bg.png");
        this.ultreonLogoTex = new Texture("assets/quantum/logo.png");
        this.libGDXLogoTex = new Texture("assets/quantum/libgdx_logo.png");
        this.logoRevealSound = Gdx.audio.newSound(Gdx.files.internal("assets/quantum/sounds/logo_reveal.mp3"));

        // Initialize Resizer
        this.resizer = new Resizer(this.ultreonLogoTex.getWidth(), this.ultreonLogoTex.getHeight());

        // Create cursor textures
        this.normalCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("assets/quantum/textures/cursors/normal.png")), 0, 0);
        this.clickCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("assets/quantum/textures/cursors/click.png")), 0, 0);

        // Set current language
        LanguageManager.setCurrentLanguage(new Locale("en", "us"));

        // Set normal cursor
        Gdx.graphics.setCursor(this.normalCursor);

        // Create inspection nodes for libGdx and graphics
        if (DebugFlags.INSPECTION_ENABLED.enabled()) {
            InspectionNode<Application> libGdxNode = this.inspection.createNode("libGdx", value -> Gdx.app);
            InspectionNode<Graphics> graphicsNode = libGdxNode.createNode("graphics", Application::getGraphics);

            // Create inspection nodes for graphics properties
            graphicsNode.create("backBufferScale", Graphics::getBackBufferScale);
            graphicsNode.create("backBufferWidth", Graphics::getBackBufferWidth);
            graphicsNode.create("backBufferHeight", Graphics::getBackBufferHeight);
            graphicsNode.create("width", Graphics::getWidth);
            graphicsNode.create("height", Graphics::getHeight);
            graphicsNode.createNode("bufferFormat", Graphics::getBufferFormat);
            graphicsNode.create("density", Graphics::getDensity);
            graphicsNode.create("deltaTime", Graphics::getDeltaTime);
            graphicsNode.createNode("displayMode", Graphics::getDisplayMode);
            graphicsNode.createNode("primaryMonitor", Graphics::getPrimaryMonitor);
            graphicsNode.create("glVersion", Graphics::getGLVersion);
            graphicsNode.create("framesPerSecond", Graphics::getFramesPerSecond);
            graphicsNode.create("frameId", Graphics::getFrameId);
            graphicsNode.create("type", Graphics::getType);

            // Create inspection nodes for libGdx properties
            libGdxNode.create("version", Application::getVersion);
            libGdxNode.create("javaHeap", Application::getJavaHeap);
        }
    }

    /**
     * Game crash hook, which will be called when a crash occurs.
     * <p>
     * <p style="font-size: 16px"><b>ONLY USE THIS IF YOU KNOW WHAT YOU ARE DOING</b></p>
     * <p>WHEN THIS IS NON-NULL CRASHES WILL BE CAPTURED AND WILL STOP THE GAME FROM HANDLING THEM.</p>
     * <p>So, make sure to actually handle the crash when using this.</p>
     */
    @RestrictedApi(
            explanation = "Only use this if you know what you are doing",
            link = "https://github.com/Ultreon/ultracraft/wiki/Crash-Hooks#important",
            allowlistAnnotations = UnsafeApi.class
    )
    @UnsafeApi
    public static void setCrashHook(Callback<CrashLog> crashHook) {
        QuantumClient.crashHook = crashHook;
    }

    private void onReloadConfig() {
        if (Config.fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        String[] split = Config.language.path().split("_");
        if (split.length == 2) {
            LanguageManager.setCurrentLanguage(new Locale(split[0], split[1]));
        } else {
            QuantumClient.LOGGER.error("Invalid language: {}", Config.language);
            LanguageManager.setCurrentLanguage(new Locale("en", "us"));
            Config.language = QuantumClient.id("en_us");
            this.newConfig.save();
        }

        if (Config.guiScale != 0) {
            this.setAutomaticScale(false);
            this.setGuiScale(Config.guiScale);
        } else {
            this.setAutomaticScale(true);
        }

        this.camera.fov = Config.fov;

        if (Config.hideRPC) {
            RpcHandler.disable();
        } else {
            RpcHandler.enable();
            if (this.activity != null) {
                this.setActivity(this.activity);
                RpcHandler.setActivity(this.activity);
            }
        }

        if (!Config.vibration) {
            GameInput.cancelVibration();
        }

        QuantumClient.invoke(() -> {
            boolean enableVsync = Config.enableVsync;
            Gdx.graphics.setVSync(enableVsync);

            int fpsLimit = Config.fpsLimit;
            if (fpsLimit >= 240) QuantumClient.setFpsLimit(240);
            else QuantumClient.setFpsLimit(fpsLimit < 10 ? 60 : fpsLimit);

            this.renderer.resetGrid();
        });
    }

    public static void setFpsLimit(int limit) {
        Gdx.graphics.setForegroundFPS(limit);
    }

    /**
     * Gets a file handle in the game directory.
     *
     * @param path the path in the game directory.
     * @return the file handle.
     * @see #getGameDir()
     */
    public static FileHandle data(String path) {
        return Gdx.files.absolute(QuantumClient.getGameDir().toAbsolutePath().toString()).child(path);
    }

    /**
     * Makes a screenshot of the game.
     */
    public CompletableFuture<Screenshot> screenshot() {
        this.triggerScreenshot = true;
        this.screenshotFuture = new CompletableFuture<>();
        return this.screenshotFuture;
    }

    /**
     * Launches the game.
     * <p style="color:red;"><b>Note: This method should not be called.</b></p>
     *
     * @param argv the arguments to pass to the game
     */
    @ApiStatus.Internal
    public static void main(String[] argv) {
        try {
            QuantumClient.arguments = new ArgParser(argv);

            QuantumClient.launch(argv);
        } catch (Exception | OutOfMemoryError e) {
            CrashHandler.handleCrash(new CrashLog("Launch failed", e).createCrash().getCrashLog());
        }
    }

    /**
     * <h2 style="color:red;"><b>Note: This method should not be called.</b></h2>
     * Launches the game.
     * This method gets invoked dynamically by the FabricMC game provider.
     *
     * @param argv the arguments to pass to the game
     */
    @SuppressWarnings("unused")
    @kotlin.OptIn(markerClass = InternalApi.class)
    private static void launch(String[] argv) {
        QuantumClient.logDebug();

        FlatMacLightLaf.setup();

        // Before initializing LibGDX or creating a window:
        try (var ignored = GLFW.glfwSetErrorCallback((error, description) -> QuantumClient.LOGGER.error("GLFW Error: {}", description))) {
            try {
                new Lwjgl3Application(new GameLibGDXWrapper(argv), QuantumClient.createConfig());
            } catch (ApplicationCrash e) {
                CrashLog crashLog = e.getCrashLog();
                QuantumClient.crash(crashLog);
            } catch (Exception e) {
                QuantumClient.crash(e);
            }
        }
    }

    @NotNull
    private static Lwjgl3ApplicationConfiguration createConfig() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(false);
        config.setForegroundFPS(120);
        config.setIdleFPS(10);
        config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 0);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 4, 1);
        config.setInitialVisible(false);
        config.setTitle("Quantum Voxel");
        config.setWindowIcon(QuantumClient.getIcons());
        config.setWindowedMode(1280, 720);
        config.setWindowListener(new WindowAdapter());
        return config;
    }

    private static void logDebug() {
        if (QuantumClient.isPackaged()) QuantumClient.LOGGER.warn("Running in the JPackage environment.");
        QuantumClient.LOGGER.debug("Java Version: {}", System.getProperty("java.version"));
        QuantumClient.LOGGER.debug("Java Vendor: {}", System.getProperty("java.vendor"));
        QuantumClient.LOGGER.debug("Operating System: {} {} ({})", new Object[]{System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch")});
    }

    private static String[] getIcons() {
        String[] icons = new String[QuantumClient.SIZES.length];
        for (int i = 0, sizesLength = QuantumClient.SIZES.length; i < sizesLength; i++) {
            var size = QuantumClient.SIZES[i];
            icons[i] = "icons/icon_" + size + ".png";
        }

        return icons;
    }

    /**
     * Check whether the application is packaged using JPackage.
     *
     * @return true if in the JPackage environment, false otherwise.
     */
    public static boolean isPackaged() {
        return QuantumClient.arguments.getFlags().contains("packaged");
    }

    /**
     * Gets the game directory.
     *
     * @return the game directory.
     */
    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void load() {
        var argList = Arrays.asList(this.argv);
        this.isDevMode = argList.contains("--dev") && FabricLoader.getInstance().isDevelopmentEnvironment();

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) this.gameEnv = GameEnvironment.DEVELOPMENT;
        else if (Objects.equals(System.getProperty("quantum.environment", "normal"), "packaged"))
            this.gameEnv = GameEnvironment.PACKAGED;
        else this.gameEnv = GameEnvironment.NORMAL;

        if (this.isDevMode) QuantumClient.LOGGER.info("Developer mode is enabled");

//        Thread.setDefaultUncaughtExceptionHandler(QuantumClient::uncaughtException);

        this.loadingOverlay.setProgress(0.075F);

        Gdx.app.setApplicationLogger(new LibGDXLogger());

        this.configDir = QuantumClient.createDir("config/");
        this.garbageCollector = new GarbageCollector();

        QuantumClient.createDir("screenshots/");
        QuantumClient.createDir("game-crashes/");
        QuantumClient.createDir("logs/");

        int scale = Config.guiScale;
        if (scale == 0) {
            this.setAutomaticScale(true);
        }
        this.setGuiScale(scale);

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        this.loadingOverlay.setProgress(0.15F);

        QuantumClient.LOGGER.info("Importing resources");
        this.resourceManager.importModResources();

        this.loadingOverlay.setProgress(0.35F);

        QuantumClient.LOGGER.info("Generating bitmap fonts");
        var resource = this.resourceManager.getResource(QuantumClient.id("texts/unicode.txt"));
        if (resource == null) {
            throw new ApplicationCrash(new CrashLog("Where are my symbols", new ResourceNotFoundException(QuantumClient.id("texts/unicode.txt"))));
        }

        this.crashOverlay = new ManualCrashOverlay(this);

        this.loadingOverlay.setProgress(0.7F);

        //----------------------
        // Setting up rendering
        //----------------------
        QuantumClient.LOGGER.info("Initializing rendering stuffs");
        QuantumClient.invokeAndWait(() -> {
            this.input = deferDispose(this.createInput());
        });
        Gdx.input.setInputProcessor(this.input);

        QuantumClient.LOGGER.info("Setting up HUD");
        this.hud = QuantumClient.invokeAndWait(() -> new Hud(this));

        QuantumClient.LOGGER.info("Setting up Debug Renderer");
        this.debugGui = new DebugOverlay(this);

        this.loadingOverlay.setProgress(0.83F);

        //--------------------------
        // Registering game content
        //--------------------------
        QuantumClient.LOGGER.info("Loading languages");
        this.loadLanguages();

        QuantumClient.LOGGER.info("Registering stuff");

        LoadingContext.withinContext(new LoadingContext(CommonConstants.NAMESPACE), () -> {
            Registries.nopInit();
            RegistryEvents.REGISTRY_CREATION.factory().onRegistryCreation();
        });

        LoadingContext.withinContext(new LoadingContext(CommonConstants.NAMESPACE), () -> {
            CommonRegistries.registerGameStuff();

            // Client registry
            Fonts.nopInit();
            Overlays.nopInit();
            QuantumClient.invokeAndWait(() -> {
                Shaders.nopInit();
                ShaderPrograms.nopInit();
            });

            QuantumClient.registerDebugPages();

            Biomes.nopInit();
        });

        for (var mod : FabricLoader.getInstance().getAllMods()) {
            final String id = mod.getMetadata().getId();
            LoadingContext.withinContext(new LoadingContext(id), () -> {
                for (Registry<?> registry : Registry.getRegistries()) {
                    RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(id, registry);
                }
            });
        }

        Registry.freeze();

        for (Biome biome : Registries.BIOME.values()) {
            biome.buildLayers();
        }

        QuantumClient.LOGGER.info("Registering models");
        this.registerMenuScreens();
        RenderingRegistration.registerRendering(this);

        QuantumClient.LOGGER.info("Reloading resources");
        this.reloadResources();

        this.loadingOverlay.setProgress(0.95F);

        //*
        //* Post-initialize game content
        //* Such as model baking and texture stitching
        //*
        QuantumClient.LOGGER.info("Stitching textures");
        this.stitchTextures();

        this.loadingOverlay.setProgress(0.98F);

        Json5ModelLoader j5ModelLoader = new Json5ModelLoader(this.resourceManager);

        BlockModelRegistry.load(j5ModelLoader);
        QuantumClient.LOGGER.info("Initializing sounds");
        this.soundRegistry.registerSounds();

        QuantumClient.LOGGER.info("Baking models");
        BlockModelRegistry.bakeJsonModels(this);
        this.bakedBlockModels = BlockModelRegistry.bake(this.blocksTextureAtlas);

        this.itemRenderer = QuantumClient.invokeAndWait(() -> new ItemRenderer(this));
        this.itemRenderer.registerModels(j5ModelLoader);
        this.itemRenderer.loadModels(this);

        if (this.deferredWidth != null && this.deferredHeight != null) {
            this.camera.viewportWidth = this.deferredWidth;
            this.camera.viewportHeight = this.deferredHeight;
            this.camera.update();
        }

        this.windowTex = this.textureManager.getTexture(QuantumClient.id("textures/gui/window.png"));

        this.loadingOverlay.setProgress(0.99F);

        ClientLifecycleEvents.CLIENT_STARTED.factory().onGameLoaded(this);

        this.loading = false;

        //*************//
        // Final stuff //
        //*************//
        QuantumClient.LOGGER.info("Opening title screen");

        if (this.imGui) {
            ImGuiOverlay.setupImGui();
        }

        this.booted = true;

        this.loadingOverlay.setProgress(1.0F);

        this.bootTime = Duration.ofMilliseconds(System.currentTimeMillis() - QuantumClient.BOOT_TIMESTAMP);
        QuantumClient.LOGGER.info("Game booted in {}.", this.bootTime.toSimpleString());

        QuantumClient.invokeAndWait(new Task<>(QuantumClient.id("main/show_title_screen"), () -> {
            if (this.devWorld) {
                this.startDevWorld();
            }
        }));
        this.loadingOverlay = null;
    }

    public void setAutomaticScale(boolean b) {
        this.autoScale = b;
        this.guiScale = this.calcMaxGuiScale();
        this.resize(this.width, this.height);
    }

    private static void registerDebugPages() {
        ClientRegistries.DEBUG_PAGE.register(QuantumClient.id("simple"), new SimpleDebugPage());
        ClientRegistries.DEBUG_PAGE.register(QuantumClient.id("generic"), new GenericDebugPage());
        ClientRegistries.DEBUG_PAGE.register(QuantumClient.id("profiler"), new ProfilerDebugPage());
        ClientRegistries.DEBUG_PAGE.register(QuantumClient.id("inspector"), new InspectorDebugPage());
    }

    private void startDevWorld() {
        WorldStorage storage = new WorldStorage("worlds/dev");
        try {
            if (Gdx.files.local("worlds/dev").exists())
                storage.delete();

            storage.createWorld();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }

        this.startWorld(storage);
    }

    private void registerMenuScreens() {
        MenuRegistry.registerScreen(MenuTypes.INVENTORY, InventoryScreen::new);
        MenuRegistry.registerScreen(MenuTypes.CRATE, CrateScreen::new);
    }

    /**
     * Set up mods by invoking entry points using {@link FabricLoader#invokeEntrypoints}.
     * This should be done at the start of the game.
     * <p>
     * See {@link ModInit} and {@link ClientModInit}
     * Thi also initializes and loads configurations from entry points.
     */
    private void setupMods() {
        // Set mod icon overrides.
        ModIconOverrideRegistry.set("quantum", QuantumClient.id("icon.png"));
        ModIconOverrideRegistry.set("gdx", new Identifier("gdx", "icon.png"));

        // Invoke entry points for initialization.
        FabricLoader loader = FabricLoader.getInstance();
        loader.invokeEntrypoints(ModInit.ENTRYPOINT_KEY, ModInit.class, ModInit::onInitialize);
        loader.invokeEntrypoints(ClientModInit.ENTRYPOINT_KEY, ClientModInit.class, ClientModInit::onInitializeClient);

        CommonLoader.initConfigEntrypoints(loader);
    }

    /**
     * Executes the specified {@link Callable} function on the client thread and waits until it completes.
     * This method is designed to be invoked from a different thread to ensure that the function is executed on the
     * client thread.
     *
     * @param func the Callable task to be executed
     * @param <T>  the type of result returned by the Callable task
     * @return the result returned by the Callable task
     */
    @CanIgnoreReturnValue
    public static <T> T invokeAndWait(@NotNull Callable<T> func) {
        return QuantumClient.instance.submit(func).join();
    }

    /**
     * Executes the specified {@link Runnable} function on the client thread and waits until it completes.
     * This method is designed to be invoked from a different thread to ensure that the function is executed on the
     * client thread.
     *
     * @param func the {@link Runnable} function to be executed on the QuantumClient thread
     */
    public static void invokeAndWait(Runnable func) {
        QuantumClient.instance.submit(func).join();
    }

    /**
     * Invokes the given runnable asynchronously and returns a {@link CompletableFuture} that completes with Void.
     *
     * @param func the runnable to be invoked
     * @return a CompletableFuture that completes with Void once the runnable has been invoked
     */
    @CanIgnoreReturnValue
    public static @NotNull CompletableFuture<Void> invoke(Runnable func) {
        return QuantumClient.instance.submit(func);
    }

    /**
     * Invokes the given callable function asynchronously and returns a {@link CompletableFuture}.
     *
     * @param func the callable function to be invoked
     * @param <T>  the type parameter of the callable function's return value
     * @return a CompletableFuture representing the pending result of the callable function
     */
    @CanIgnoreReturnValue
    public static <T> @NotNull CompletableFuture<T> invoke(Callable<T> func) {
        return QuantumClient.instance.submit(func);
    }

    /**
     * Returns a new instance of FileHandle for the specified resource identifier.
     *
     * @param id The identifier of the resource.
     * @return A new instance of FileHandle for the specified resource.
     */
    @NewInstance
    public static @NotNull FileHandle resource(Identifier id) {
        return Gdx.files.internal("assets/" + id.namespace() + "/" + id.path());
    }

    /**
     * Checks whether the current thread is the main thread.
     *
     * @return true if the current thread is the main thread, false otherwise.
     */
    public static boolean isOnMainThread() {
        return Thread.currentThread().getId() == QuantumClient.instance.mainThread.getId();
    }

    /**
     * Gets an Quantum Voxel identifier from a path, but as a string.
     *
     * @param path the path.
     * @return the identifier as string.
     * @see #id(String)
     */
    public static String strId(String path) {
        return QuantumClient.id(path).toString();
    }

    /**
     * Delays disposing of a disposable.
     * This method disposes the given disposable when the game is shutdown.
     *
     * @param disposable the disposable to delay disposal.
     * @param <T>        the type of the disposable.
     * @return the same disposable.
     */
    @Override
    public <T extends Disposable> T deferDispose(T disposable) {
        if (disposable == null) return null;
        if (QuantumClient.instance.disposables.contains(disposable)) return disposable;

        if (QuantumClient.instance.disposed) {
            QuantumClient.LOGGER.warn("QuantumClient already disposed, immediately disposing {}", disposable.getClass().getName());
            disposable.dispose();
            return disposable;
        }

        QuantumClient.instance.disposables.add(disposable);
        return disposable;
    }

    /**
     * Delays shutting down of a shutdownable.
     * This method shuts down the given shutdownable when the game is shutdown
     *
     * @param shutdownable the shutdownable to delay shutdown.
     * @param <T>          the type of the shutdownable.
     * @return the same shutdownable
     */
    public <T extends Shutdownable> T deferShutdown(T shutdownable) {
        QuantumClient.instance.shutdownables.add(shutdownable);
        return shutdownable;
    }

    /**
     * Delays closing of a "closeable".
     * This method closes the given closeable when the game is shutdown.
     *
     * @param closeable the closeable to delay closing.
     * @param <T>       the type of the closeable.
     * @return the same closeable.
     */
    public <T extends AutoCloseable> T deferClose(@WillClose T closeable) {
        QuantumClient.instance.closeables.add(closeable);
        return closeable;
    }

    /**
     * Gets the boot time of the game.
     *
     * @return the boot time
     */
    public Duration getBootTime() {
        return this.bootTime;
    }

    /**
     * Delays crashing the game.
     *
     * @param crashLog the crash log.
     */
    public void delayCrash(CrashLog crashLog) {
        final var finalCrash = new CrashLog("An error occurred", crashLog, new RuntimeException("Delayed crash"));
        Gdx.app.postRunnable(() -> QuantumClient.crash(finalCrash));
    }

    /**
     * Self-explanatory, gets the Quantum Voxel client instance.
     *
     * @return the Quantum Voxel client instance.
     */
    public static QuantumClient get() {
        return QuantumClient.instance;
    }

    /**
     * Gets the Quantum Voxel identifier for the given path.
     *
     * @param path the path to the resource.
     * @return the identifier for the given path.
     */
    public static Identifier id(String path) {
        return new Identifier(CommonConstants.NAMESPACE, path);
    }

    /**
     * GG bro!
     */
    public static GG ggBro() {
        return new GG();
    }

    private void loadLanguages() {
        var internal = QuantumClient.resource(new Identifier("languages.json5"));
        Json5Element parse = CommonConstants.JSON5.parse(internal.reader());
        Json5Object asJson5Object = parse.getAsJson5Object();

        Json5Array languagesJ5 = asJson5Object.get("Languages").getAsJson5Array();

        if (languagesJ5.isEmpty()) {
            this.registerLanguage(QuantumClient.id("en_us"));
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
            this.registerLanguage(QuantumClient.id(language));
        }

        LanguageRegistry.doRegistration(this::registerLanguage);
    }

    private void registerLanguage(Identifier id) {
        var s = id.path().split("_", 2);
        var locale = s.length == 1 ? new Locale(s[0]) : new Locale(s[0], s[1]);
        LanguageManager.INSTANCE.register(locale, id);
        LanguageManager.INSTANCE.load(locale, id, this.resourceManager);
    }

    private void stitchTextures() {
    }

    private GameInput createInput() {
        return new DesktopInput(this, this.camera);
    }

    private static FileHandle createDir(String dirName) {
        var directory = QuantumClient.data(dirName);
        if (!directory.exists()) {
            directory.mkdirs();
        } else if (!directory.isDirectory()) {
            directory.delete();
            directory.mkdirs();
        }
        return directory;
    }

    /**
     * Pauses the game by showing the pause screen.
     * If the current screen is not null and the world is not null, it will show the pause screen.
     */
    public void pause() {
        if (this.screen == null && this.world != null) {
            this.showScreen(new PauseScreen());
        }
    }

    /**
     * Resumes the game by hiding the pause screen.
     * If the current screen is a PauseScreen and the world is not null,
     * the screen is set to null to resume the game.
     */
    public void resume() {
        if (this.screen instanceof PauseScreen && this.world != null) {
            this.showScreen(null);
        }
    }

    /**
     * Shows the given screen.
     *
     * @param next the screen to open, or null to close the current screen
     * @return true if the screen was opened, false if opening was canceled.
     */
    @CanIgnoreReturnValue
    public boolean showScreen(@Nullable Screen next) {
        if (!isOnMainThread()) {
            @Nullable Screen finalNext = next;
            return invokeAndWait(() -> this.showScreen(finalNext));
        }

        var cur = this.screen;
        if (next == null && this.world == null)
            next = new TitleScreen();

        if (next == null)
            return cur == null || this.closeScreen(cur);

        // Call open event.
        var openResult = ScreenEvents.OPEN.factory().onOpenScreen(next);
        if (openResult.isCanceled())
            return false;

        if (openResult.isInterrupted())
            next = openResult.getValue();

        if (cur != null && this.closeScreen(next, cur))
            return false; // Close was canceled
        if (next == null)
            return false; // The next screen is null, canceling.

        this.screen = next;
        this.screen.init(this.getScaledWidth(), this.getScaledHeight());
        DesktopInput.setCursorCaught(false);

        return true;
    }

    private boolean closeScreen(@Nullable Screen next, Screen cur) {
        var closeResult = ScreenEvents.CLOSE.factory().onCloseScreen(cur);
        if (closeResult.isCanceled()) return true;

        if (!cur.onClose(next)) return true;
        cur.onClosed();
        return false;
    }

    private boolean closeScreen(Screen cur) {
        if (this.closeScreen(null, cur)) return false;
        this.screen = null;
        DesktopInput.setCursorCaught(true);

        return true;
    }

    /**
     * Interpolates a value between two values.
     * From <a href="https://www.java2s.com">Java2s</a>
     */
    public static double interpolate(double a, double b, double d) {
        return a + (b - a) * d;
    }

    public void delayDispose(Disposable disposable) {
        this.disposalQueue.add(disposable);
    }

    /**
     * Renders the game.
     * <p>NOTE: This method should not be called.
     * This is invoked by libGDX.</p>
     */
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        Disposable disposable;
        while ((disposable = this.disposalQueue.poll()) != null) {
            try {
                cleanUp(disposable);
            } catch (Exception e) {
                QuantumClient.crash(new Throwable("Failed to dispose " + disposable + " during render", e));
            }
        }

        try {
            QuantumClient.PROFILER.update();

            if (this.debugGui != null && this.isShowDebugHud() && !this.loading) {
                this.debugGui.updateProfiler();
            }

            QuantumClient.PROFILER.section("render", () -> this.doRender(deltaTime));
            this.renderer.actuallyEnd();
        } catch (OutOfMemoryError e) {
            System.gc(); // try to free up some memory before handling out of memory.
            try {
                if (this.integratedServer != null) {
                    this.integratedServer.shutdownNow();
                    this.integratedServer = null;
                }

                if (this.worldRenderer != null) {
                    this.worldRenderer.dispose();
                }
                System.gc();

                this.showScreen(new OutOfMemoryScreen());
            } catch (OutOfMemoryError | Exception t) {
                QuantumClient.crash(t);
            }
        } catch (Exception t) {
            QuantumClient.crash(t);
        }

        Gdx.gl.glDisable(GL_CULL_FACE);
    }

    private void doRender(float deltaTime) {
        this.width = Gdx.graphics.getWidth();
        this.height = Gdx.graphics.getHeight();

        if (this.width == 0 || this.height == 0) return;
        if (this.triggerScreenshot) this.prepareScreenshot();

        QuantumClient.PROFILER.section("renderGame", () -> this.renderGame(renderer, deltaTime));

        if (this.captureScreenshot) this.handleScreenshot();

        if (this.screenshotFlashTime > System.currentTimeMillis() - 200) {
            this.renderer.begin();
            this.shapes.filledRectangle(0, 0, this.width, this.height, this.tmpColor.set(1, 1, 1, 1 - (System.currentTimeMillis() - this.screenshotFlashTime) / 200f));
            this.renderer.end();
        }

        if (this.isCustomBorderShown()) this.drawCustomBorder(renderer);

        if (this.imGui) {
            ImGuiOverlay.renderImGui(this);
        }
    }

    private void prepareScreenshot() {
        this.screenshotScale = 1;

        if (Config.enable4xScreenshot && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
            this.screenshotScale = 4;
        }

        this.width = this.width * this.screenshotScale;
        this.height = this.height * this.screenshotScale;

        this.captureScreenshot = true;
        this.triggerScreenshot = false;

        this.fbo = new FrameBuffer(Pixmap.Format.RGB888, this.width, this.height, true);
        this.fbo.begin();

        ScreenUtils.clear(0, 0, 0, 1, true);
    }

    private void drawCustomBorder(Renderer renderer) {
        this.renderer.begin();
        renderer.pushMatrix();
        renderer.scale(2, 2);
        this.renderWindow(renderer, this.getWidth() / 2, this.getHeight() / 2);
        renderer.popMatrix();
        this.renderer.end();
    }

    private void handleScreenshot() {
        this.captureScreenshot = false;

        this.saveScreenshot();
        this.fbo.end();
        this.fbo.dispose();

        this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private boolean renderGame(Renderer renderer, float deltaTime) {
        if (Gdx.graphics.getFrameId() == 2) {
            QuantumClient.firstRender();
            Gdx.graphics.setTitle("Quantum Voxel %s".formatted(QuantumClient.getGameVersion()));
        }

        this.updateActivity();

        this.poll();

        QuantumClient.PROFILER.section("client-tick", this::tryClientTick);

        boolean renderSplash = this.showUltreonSplash || this.showLibGDXSplash;
        if (this.showLibGDXSplash) {
            this.renderLibGDXSplash(renderer);
        }

        if (this.showUltreonSplash) {
            this.renderUltreonSplash(renderer);
        }
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && this.startDevLoading) {
            this.startLoading();
        }
        if (renderSplash) {
            return true;
        }

        final LoadingOverlay loading = this.loadingOverlay;
        if (loading != null) {
            this.renderLoadingOverlay(renderer, deltaTime, loading);
        }

        if (loading != null) {
            return true;
        }

        this.renderMain(renderer, deltaTime);
        return false;
    }

    private void renderMain(Renderer renderer, float deltaTime) {
        Player player = this.player;
        if (player == null) {
            this.hitResult = null;
        } else {
            QuantumClient.PROFILER.section("playerRayCast", () -> this.hitResult = player.rayCast());
        }
        GameInput input = this.input;
        if (input != null) {
            QuantumClient.PROFILER.section("input", input::update);
        }

        Screen screen = this.screen;
        if (screen != null && DesktopInput.isPressingAnyButton() && !this.wasClicking) {
            Gdx.graphics.setCursor(this.clickCursor);
            this.wasClicking = true;
        } else if (screen != null && !DesktopInput.isPressingAnyButton() && this.wasClicking) {
            Gdx.graphics.setCursor(this.normalCursor);
            this.wasClicking = false;
        }

        RenderEvents.PRE_RENDER_GAME.factory().onRenderGame(gameRenderer, renderer, deltaTime);
        this.gameRenderer.render(renderer, deltaTime);
        RenderEvents.POST_RENDER_GAME.factory().onRenderGame(gameRenderer, renderer, deltaTime);
    }

    private void renderLoadingOverlay(Renderer renderer, float deltaTime, LoadingOverlay loading) {
        QuantumClient.PROFILER.section("loading", () -> {
            renderer.begin();
            renderer.pushMatrix();
            renderer.translate(this.getDrawOffset().x, this.getDrawOffset().y);
            renderer.scale(this.getGuiScale(), this.getGuiScale());
            loading.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
            renderer.popMatrix();
            renderer.end();
        });
    }

    private void startLoading() {
        this.startDevLoading = false;

        QuantumClient.setCrashHook(this.crashes::add);

        this.loadingOverlay = new LoadingOverlay();

        CompletableFuture.runAsync(this::load).exceptionally(throwable -> {
            // Clear the crash handling
            QuantumClient.crashHook = null;

            this.crashes.add(new CrashLog("Failed to load", throwable));

            // Show the crash screen
            QuantumClient.invoke(() -> {
                this.screen = new CrashScreen(this.crashes);
                this.screen.init(this.getScaledWidth(), this.getScaledHeight());
                this.loadingOverlay = null;
            }).exceptionally(throwable1 -> {
                crash(throwable1);
                return null;
            });
            return null;
        }).thenRun(() -> {
            // Clear the crash handling
            QuantumClient.crashHook = null;

            QuantumClient.invoke(() -> {
                this.screen = this.crashes.isEmpty() ? new DevScreen() : new CrashScreen(this.crashes);
                this.screen.init(this.getScaledWidth(), this.getScaledHeight());
                this.loadingOverlay = null;
            }).exceptionally(throwable -> {
                crash(throwable);
                return null;
            });
        });
    }

    private void renderLibGDXSplash(Renderer renderer) {
        QuantumClient.PROFILER.section("libGdxSplash", () -> {
            if (this.libGDXSplashTime == 0L) {
                this.libGDXSplashTime = System.currentTimeMillis();
            }

            ScreenUtils.clear(1, 1, 1, 1, true);

            this.renderer.begin();
            int size = Math.min(this.getWidth(), this.getHeight()) / 2;
            renderer.blit(this.libGDXLogoTex, (float) this.getWidth() / 2 - (float) size / 2, (float) this.getHeight() / 2 - (float) size / 2, size, size);
            this.renderer.end();

            if (System.currentTimeMillis() - this.libGDXSplashTime > 4000f) {
                this.showLibGDXSplash = false;
                this.showUltreonSplash = true;
            }
        });
    }

    private void renderUltreonSplash(Renderer renderer) {
        QuantumClient.PROFILER.section("ultreonSplash", () -> {
            if (this.ultreonSplashTime == 0L) {
                this.ultreonSplashTime = System.currentTimeMillis();

                this.logoRevealSound.play(0.5f);
            }

            ScreenUtils.clear(0, 0, 0, 1, true);

            final long timeDiff = System.currentTimeMillis() - this.ultreonSplashTime;
            float zoom = (float) QuantumClient.interpolate(QuantumClient.FROM_ZOOM, QuantumClient.TO_ZOOM, Mth.clamp(timeDiff / QuantumClient.DURATION, 0f, 1f));
            Vec2f thumbnail = this.resizer.thumbnail(this.getWidth() * zoom, this.getHeight() * zoom);

            float drawWidth = thumbnail.x;
            float drawHeight = thumbnail.y;

            float drawX = (this.getWidth() - drawWidth) / 2;
            float drawY = (this.getHeight() - drawHeight) / 2;

            this.renderer.begin();
            renderer.blit(this.ultreonBgTex, 0, 0, this.getWidth(), this.getHeight(), 0, 0, 1024, 1024, 1024, 1024);
            renderer.blit(this.ultreonLogoTex, (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, 1920, 1080, 1920, 1080);
            this.renderer.end();

            if (System.currentTimeMillis() - this.ultreonSplashTime > QuantumClient.DURATION) {
                showUltreonSplash = false;
                this.startLoading();
            }
        });
    }

    /**
     * Retrieves the game version of the UltraCraft mod.
     *
     * @return The game version as a {@code String}.
     */
    public static String getGameVersion() {
        return FabricLoader.getInstance().getModContainer("quantum").orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    private void tryClientTick() {
        var canTick = false;

        double time2 = System.currentTimeMillis();
        var passed = time2 - this.time;
        this.frameTime += (float) passed;
        this.tickTime += (float) passed;

        this.time = time2;

        float tickCap = 1000f / QuantumServer.TPS;
        while (this.frameTime >= tickCap) {
            this.frameTime -= tickCap;
            this.partialTick = this.frameTime / tickCap;

            canTick = true;
        }

        if (canTick) {
            this.ticksPassed++;
            try {
                this.clientTick();
            } catch (ApplicationCrash e) {
                QuantumClient.crash(e.getCrashLog());
            } catch (Exception t) {
                var crashLog = new CrashLog("Game being ticked.", t);
                QuantumClient.crash(crashLog);
            }
        }

        if (this.tickTime >= 1000.0d) {
            this.currentTps = this.ticksPassed;
            this.ticksPassed = 0;
            this.tickTime = 0;
        }
    }

    private void updateActivity() {
        if (this.activity != this.oldActivity) {
            this.oldActivity = this.activity;
            if (this.activity == null) {
                Gdx.graphics.setTitle("Quantum Voxel " + QuantumClient.getGameVersion());
            } else {
                var name = this.activity.getDisplayName();
                Gdx.graphics.setTitle("Quantum Voxel " + QuantumClient.getGameVersion() + " - " + name);
            }

            RpcHandler.setActivity(this.activity);
        }
    }

    private void saveScreenshot() {
        if (this.spriteBatch.isDrawing()) this.spriteBatch.flush();
        if (this.modelBatch.getCamera() != null) this.modelBatch.flush();

        Screenshot grabbed = Screenshot.grab(this.width, this.height);
        FileHandle save = grabbed.save("screenshots/%s.png".formatted(DateTimeFormatter.ofPattern("MM.dd.yyyy-HH.mm.ss").format(LocalDateTime.now())));

        this.screenshotFlashTime = System.currentTimeMillis();

        this.playSound(SoundEvents.SCREENSHOT, 0.5f);

        this.notifications.add("Screenshot taken.", save.name(), "screenshots");

        this.screenshotFuture.complete(grabbed);
    }

    private static void firstRender() {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
        Lwjgl3Window window = graphics.getWindow();
        window.setVisible(true);
    }

    private void renderWindow(Renderer renderer, int width, int height) {
        renderer.draw9PatchTexture(this.windowTex, 0, 0, width, height, 0, 0, 18, 22, 256, 256);
    }

    public static void crash(Throwable throwable) {
        QuantumClient.LOGGER.error("Game crash triggered:", throwable);
        var crashLog = new CrashLog("An unexpected error occurred", throwable);
        QuantumClient.crash(crashLog);
    }

    public static void crash(CrashLog crashLog) {
        try {
            Callback<CrashLog> handler = QuantumClient.crashHook;
            if (handler != null) {
                try {
                    handler.call(crashLog);
                    return;
                } catch (Exception e) {
                    QuantumClient.LOGGER.error("Crash hook failed", e);

                }
            }

            QuantumClient.instance.fillGameInfo(crashLog);
            var crash = crashLog.createCrash();
            QuantumClient.crash(crash);
        } catch (Exception | OutOfMemoryError t) {
            QuantumClient.LOGGER.error(QuantumClient.FATAL_ERROR_MSG, t);
            System.exit(1);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @ApiStatus.Internal
    public void fillGameInfo(CrashLog crashLog) {
        if (this.world != null) {
            this.world.fillCrashInfo(crashLog);
        }

        var client = new CrashCategory("Game Details");
        client.add("Time until crash", Duration.ofMilliseconds(System.currentTimeMillis() - QuantumClient.BOOT_TIMESTAMP).toSimpleString()); // Could be the game only crashes after a long time.
        client.add("Game booted", this.booted); // Could be that the game isn't booted yet.
        crashLog.addCategory(client);
    }

    private static void crash(ApplicationCrash crash) {
        if (crashing) {
            LOGGER.error("Double crash detected, ignoring.");
            return;
        }
        crashing = true;
        try {
            var crashLog = crash.getCrashLog();
            CrashHandler.handleCrash(crashLog);
            GameLibGDXWrapper.displayCrash(crash);
        } catch (Exception | OutOfMemoryError t) {
            QuantumClient.LOGGER.error(QuantumClient.FATAL_ERROR_MSG, t);
            System.exit(1);
        }
    }

    private static void cleanUp(@Nullable Disposable disposable) {
        if (disposable == null) return;

        try {
            disposable.dispose();
        } catch (Exception throwable) {
            Debugger.log("Failed to dispose " + disposable.getClass().getName(), throwable);
        }
    }

    private static void cleanUp(@Nullable Shutdownable disposable) {
        if (disposable == null) return;

        try {
            disposable.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception throwable) {
            Debugger.log("Failed to shut down " + disposable.getClass().getName(), throwable);
        }
    }

    private static void cleanUp(@Nullable ExecutorService disposable) {
        if (disposable == null) return;

        try {
            disposable.shutdownNow();
        } catch (Exception throwable) {
            Debugger.log("Failed to shut down " + disposable.getClass().getName(), throwable);
        }
    }

    private static void cleanUp(@Nullable AutoCloseable disposable) {
        if (disposable == null) return;

        try {
            disposable.close();
        } catch (Exception throwable) {
            Debugger.log("Failed to close " + disposable.getClass().getName(), throwable);
        }
    }

    /**
     * Executes the necessary operations during a client tick.
     */
    @ApiStatus.Internal
    public void clientTick() {
        // Check if the pre-game tick event is canceled
        if (ClientTickEvents.PRE_GAME_TICK.factory().onGameTick(this).isCanceled()) return;

        // Update cursor position based on player's look vector
        final LocalPlayer player = this.player;
        if (player != null && this.world != null) {
            this.cursor = this.world.rayCast(new Ray(player.getPosition(this.partialTick).add(0, player.getEyeHeight(), 0), player.getLookVector()));
        }

        // Update connection tick
        IConnection<ClientPacketHandler, ServerPacketHandler> connection = this.connection;
        if (connection != null) {
            connection.tick();
            // Update client connection tick
            this.connection.tick();
        }

        // Execute player tick if not canceled
        if (player != null && !ClientTickEvents.PRE_PLAYER_TICK.factory().onPlayerTick(player).isCanceled()) {
            player.tick();
            ClientTickEvents.POST_PLAYER_TICK.factory().onPlayerTick(player);
        }

        // Execute world tick if not canceled
        if (this.world != null && !ClientTickEvents.PRE_WORLD_TICK.factory().onWorldTick(this.world).isCanceled()) {
            this.world.tick();
            ClientTickEvents.POST_WORLD_TICK.factory().onWorldTick(this.world);
        }

        // Handle block breaking if relevant
        BlockPos breaking = this.breaking != null ? new BlockPos(this.breaking) : null;
        if (this.world != null && breaking != null) {
            HitResult hitResult = this.hitResult;

            if (hitResult != null) {
                this.handleBlockBreaking(breaking, hitResult);
            }
        }

        // Update camera based on player position
        if (player != null) {
            this.camera.update(player);
        }

        // Execute post-game tick event
        ClientTickEvents.POST_GAME_TICK.factory().onGameTick(this);
    }

    private void handleBlockBreaking(BlockPos breaking, HitResult hitResult) {
        World world = this.world;
        if (world == null) return;
        if (!hitResult.getPos().equals(breaking.vec()) || !hitResult.getBlockMeta().equals(this.breakingBlock) || this.player == null) {
            this.resetBreaking(hitResult);
        } else {
            float efficiency = 1.0F;
            ItemStack stack = this.player.getSelectedItem();
            Item item = stack.getItem();
            if (item instanceof ToolItem toolItem && this.breakingBlock.getEffectiveTool() == ((ToolItem) item).getToolType()) {
                efficiency = toolItem.getEfficiency();
            }

            if (world.continueBreaking(breaking, 1.0F / (Math.max(this.breakingBlock.getHardness() * QuantumServer.TPS / efficiency, 0) + 1), this.player) != BreakResult.CONTINUE) {
                this.stopBreaking();
            } else {
                if (this.oldSelected != this.player.selected) {
                    this.resetBreaking();
                }
                this.oldSelected = this.player.selected;
            }
        }
    }

    private void resetBreaking(HitResult hitResult) {
        LocalPlayer player = this.player;

        if (this.world == null) return;
        if (this.breaking == null) return;
        if (player == null) return;

        this.world.stopBreaking(new BlockPos(this.breaking), player);
        BlockProperties block = hitResult.getBlockMeta();

        if (block == null || block.isAir()) {
            this.breaking = null;
            this.breakingBlock = null;
        } else {
            this.breaking = hitResult.getPos();
            this.breakingBlock = block;
            this.world.startBreaking(new BlockPos(hitResult.getPos()), player);
        }
    }

    /**
     * Resize the display to the specified width and height.
     * If not on the main thread, the resize is deferred to the main thread.
     *
     * @param width  The new width of the display
     * @param height The new height of the display
     */
    public void resize(int width, int height) {
        if (!QuantumClient.isOnMainThread()) {
            QuantumClient.invokeAndWait(() -> this.resize(width, height));
            return;
        }

        // Set the projection matrix for the spriteBatch
        this.spriteBatch.getProjectionMatrix().setToOrtho(0, width, height, 0, 0, 1000000);

        // Update the deferred width and height values
        this.deferredWidth = width;
        this.deferredHeight = height;

        // Resize the renderer
        this.renderer.resize(width, height);

        // Auto-scale the GUI if enabled
        if (this.autoScale) {
            this.guiScale = this.calcMaxGuiScale();
        }

        // Update the camera if present
        if (this.camera != null) {
            this.camera.viewportWidth = width;
            this.camera.viewportHeight = height;
            this.camera.update();
        }

        // Resize the item renderer
        if (this.itemRenderer != null) {
            this.itemRenderer.resize(width, height);
        }

        // Resize the game renderer
        this.gameRenderer.resize(width, height);

        // Resize the current screen
        var cur = this.screen;
        if (cur != null) {
            cur.resize(ceil(width / this.getGuiScale()), ceil(height / this.getGuiScale()));
        }

        OverlayManager.resize(ceil(width / this.getGuiScale()), ceil(height / this.getGuiScale()));;
    }

    @Override
    public void dispose() {
        if (!QuantumClient.isOnMainThread()) {
            throw new IllegalThreadError("Should only dispose on LibGDX main thread");
        }

        synchronized (this) {
            this.disposed = true;

            try {
                while (!this.futures.isEmpty()) {
                    this.futures.removeIf(CompletableFuture::isDone);
                }

                GameInput.cancelVibration();

                QuantumServer.getWatchManager().stop();

                QuantumClient.cleanUp((ExecutorService) this.integratedServer);
                QuantumClient.cleanUp((Shutdownable) this.integratedServer);

                QuantumClient.cleanUp(this.scheduler);
                QuantumClient.cleanUp(this.unifont);
                QuantumClient.cleanUp(this.font);
                QuantumClient.cleanUp(this.fontManager);
                if (this.chunkLoadingExecutor != null) this.chunkLoadingExecutor.shutdownNow();

                QuantumClient.cleanUp(this.garbageCollector);
                QuantumClient.cleanUp(this.world);
                QuantumClient.cleanUp(this.profiler);

                if (this.imGui) {
                    ImGuiOverlay.dispose();
                }

                this.disposables.forEach(QuantumClient::cleanUp);
                this.shutdownables.forEach(QuantumClient::cleanUp);
                this.closeables.forEach(QuantumClient::cleanUp);

                // Dispose renderers
                QuantumClient.cleanUp(this.renderer);
                QuantumClient.cleanUp(this.gameRenderer);
                QuantumClient.cleanUp(this.pipeline);
                QuantumClient.cleanUp(this.modelBatch);
                QuantumClient.cleanUp(this.itemRenderer);
                QuantumClient.cleanUp(this.worldRenderer);
                QuantumClient.cleanUp(this.fbo);

                // Dispose textures
                QuantumClient.cleanUp(this.ultreonBgTex);
                QuantumClient.cleanUp(this.ultreonLogoTex);
                QuantumClient.cleanUp(this.libGDXLogoTex);
                QuantumClient.cleanUp(this.textureManager);

                // Dispose resources
                QuantumClient.cleanUp(this.resourceManager);
                QuantumClient.cleanUp(this.skinManager);

                // Dispose cursors
                QuantumClient.cleanUp(this.normalCursor);
                QuantumClient.cleanUp(this.clickCursor);

                // Dispose connections
                QuantumClient.cleanUp(this.connection);

                ClientLifecycleEvents.CLIENT_STOPPED.factory().onGameDisposed();
            } catch (Exception t) {
                QuantumClient.crash(t);
            }
        }
    }

    public boolean isDevMode() {
        return this.isDevMode;
    }

    public boolean isShowingImGui() {
        return ImGuiOverlay.isShown();
    }

    public void setShowingImGui(boolean value) {
        ImGuiOverlay.setShowingImGui(value);
    }

    public int getWidth() {
        return this.width - this.getDrawOffset().x * 2;
    }

    public int getHeight() {
        return this.height - this.getDrawOffset().y * 2;
    }

    public TextureManager getTextureManager() {
        return this.textureManager;
    }

    public void startWorld(WorldStorage storage) {
        this.showScreen(new WorldLoadScreen(storage));
    }

    public void startWorld(Path path) {
        this.showScreen(new WorldLoadScreen(new WorldStorage(path)));
    }

    public float getGuiScale() {
        return this.guiScale;
    }

    public int getScaledWidth() {
        return ceil(Gdx.graphics.getWidth() / this.getGuiScale());
    }

    public int getScaledHeight() {
        return ceil(Gdx.graphics.getHeight() / this.getGuiScale());
    }

    public void exitWorldToTitle() {
        this.exitWorldAndThen(() -> this.showScreen(new TitleScreen()));
    }

    public void exitWorldAndThen(Runnable afterExit) {
        this.closingWorld = true;
        this.renderWorld = false;

        final @Nullable WorldRenderer worldRenderer = this.worldRenderer;
        this.showScreen(new MessageScreen(TextObject.translation("quantum.screen.message.saving_world"))); // "Saving world..."

        CompletableFuture.runAsync(() -> {
            try {
                this.connection.close();
            } catch (IOException e) {
                QuantumClient.crash(e);
                return;
            }

            QuantumClient.cleanUp((Shutdownable) this.integratedServer);

            this.serverTickQueue.clear();

            try {
                QuantumClient.invoke(() -> {
                    QuantumClient.cleanUp(worldRenderer);
                    QuantumClient.cleanUp(this.world);
                    this.connection = null;
                    this.renderWorld = false;
                    this.worldRenderer = null;
                    this.world = null;
                    this.integratedServer = null;
                    this.player = null;

                    GameInput.cancelVibration();

                    afterExit.run();
                });
            } catch (Exception e) {
                QuantumClient.crash(e);
            }
        });
    }

    public boolean isClosingWorld() {
        return this.closingWorld;
    }

    public ScheduledFuture<Void> schedule(Task<?> task, long timeMillis) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                QuantumClient.LOGGER.warn("Error occurred in task {}:", task.id(), e);
            }
            return null;
        }, timeMillis, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<Void> schedule(Task<?> task, long time, TimeUnit unit) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                QuantumClient.LOGGER.warn("Error occurred in task {}:", task.id(), e);
            }
            return null;
        }, time, unit);
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public void playSound(ClientSound event) {
        event.getSound().play();
    }

    public boolean tryShutdown() {
        ClientLifecycleEvents.WINDOW_CLOSED.factory().onWindowClose();
        if (this.world != null) {
            this.exitWorldAndThen(() -> Gdx.app.exit());
            return false;
        }

        return true;
    }

    public boolean filesDropped(String[] files) {
        var currentScreen = this.screen;
        var handles = Arrays.stream(files).map(FileHandle::new).toList();

        if (currentScreen != null) {
            return currentScreen.filesDropped(handles);
        }

        return false;
    }

    public void addFuture(CompletableFuture<?> future) {
        this.futures.add(future);
    }

    public @NotNull BlockModel getBakedBlockModel(BlockProperties block) {
        return Objects.requireNonNull(this.bakedBlockModels.bakedModels().getOrDefault(block.getBlock(), List.of()))
                .stream()
                .filter(pair -> pair.getFirst().test(block))
                .findFirst()
                .map(pair -> (BlockModel) pair.getSecond())
                .orElseGet(() -> BlockModelRegistry.get(block));
    }

    /**
     * Reset the breaking action for the player.
     */
    public void resetBreaking() {
        // If there is no breaking action, return early
        if (this.breaking == null) {
            return;
        }

        // Retrieve the necessary variables
        HitResult hitResult = this.hitResult;
        Player player = this.player;

        // Check for null conditions and return if any are true
        if (hitResult == null || this.world == null) return;
        if (player == null) return;

        // Stop and start breaking at the hit position for the player
        this.world.stopBreaking(new BlockPos(hitResult.getPos()), player);
        this.world.startBreaking(new BlockPos(hitResult.getPos()), player);

        // Update the breaking position and block meta
        this.breaking = hitResult.getPos();
        this.breakingBlock = hitResult.getBlockMeta();
    }

    /**
     * Starts the process of breaking a block in the game world.
     * If the player is already breaking a block, it stops the current process and starts a new one.
     */
    public void startBreaking() {
        // Get the hit result and player
        HitResult hitResult = this.hitResult;
        LocalPlayer player = this.player;

        // If hit result or world is null, return
        if (hitResult == null || this.world == null) {
            return;
        }

        // If the block being hit is already broken, return
        if (this.world.getBreakProgress(new BlockPos(hitResult.getPos())) >= 0.0F) {
            return;
        }

        // If the player is null, return
        if (player == null) {
            return;
        }

        if (this.breaking != null) {
            this.world.continueBreaking(new BlockPos(this.breaking), 1.0F, player);
            return;
        }

        // Start breaking the block and update the breaking position and block metadata
        this.world.startBreaking(new BlockPos(hitResult.getPos()), player);
        this.breaking = hitResult.getPos();
        this.breakingBlock = hitResult.getBlockMeta();
    }

    public void stopBreaking() {
        HitResult hitResult = this.hitResult;
        LocalPlayer player = this.player;
        if (hitResult == null || this.world == null || player == null || this.breaking == null) return;

        this.world.stopBreaking(new BlockPos(hitResult.getPos()), player);
        this.breaking = null;
        this.breakingBlock = null;
    }

    public float getBreakProgress() {
        Vec3i breaking = this.breaking;
        World world = this.world;
        if (breaking == null || world == null) return -1;
        return world.getBreakProgress(new BlockPos(breaking));
    }

    private int calcMaxGuiScale() {
        var windowWidth = Gdx.graphics.getWidth();
        var windowHeight = Gdx.graphics.getHeight();

        if (windowWidth / QuantumClient.MINIMUM_WIDTH < windowHeight / QuantumClient.MINIMUM_HEIGHT) {
            return windowWidth / QuantumClient.MINIMUM_WIDTH;
        }

        if (windowHeight / QuantumClient.MINIMUM_HEIGHT < windowWidth / QuantumClient.MINIMUM_WIDTH) {
            return windowHeight / QuantumClient.MINIMUM_HEIGHT;
        }

        return Math.min(windowWidth / QuantumClient.MINIMUM_WIDTH, windowHeight / QuantumClient.MINIMUM_HEIGHT);
    }

    public boolean isPlaying() {
        return this.world != null && this.screen == null;
    }

    public static FileHandle getConfigDir() {
        return QuantumClient.instance.configDir;
    }

    public GridPoint2 getDrawOffset() {
        return this.isCustomBorderShown() ? new GridPoint2(18 * 2, 22 * 2) : new GridPoint2();
    }

    @ApiStatus.Experimental
    public boolean isCustomBorderShown() {
        return false;
    }

    public boolean isLoading() {
        return this.loading;
    }

    public static GameEnvironment getGameEnv() {
        if (QuantumClient.instance == null) return GameEnvironment.UNKNOWN;
        return QuantumClient.instance.gameEnv;
    }

    public IntegratedServer getSingleplayerServer() {
        return this.integratedServer;
    }

    public boolean isSinglePlayer() {
        return this.integratedServer != null && !this.integratedServer.isOpenToLan();
    }

    public void playSound(@NotNull SoundEvent soundEvent, float volume) {
        Preconditions.checkNotNull(soundEvent);
        Preconditions.checkArgument(volume >= 0.0F && volume <= 1.0F, "Volume must be between 0.0F and 1.0F");

        Sound sound = this.soundRegistry.getSound(soundEvent.getId());
        if (sound == null) {
            QuantumClient.LOGGER.warn("Unknown sound event: {}", soundEvent.getId());
            return;
        }
        sound.play(volume);
    }

    public void startIntegratedServer() {
        var mem = ClientTcpConnection.connectToLocalServer().unwrap();
        this.connection = mem;
        MemoryConnectionContext.set(mem);

        this.integratedServer.start();

        mem.setOtherSide(((MemoryNetworker) this.integratedServer.getNetworker()).getConnections().get(0));

        // Initialize (memory) connection.
        this.multiplayerData = new MultiplayerData(this);
        this.connection.initiate(new LoginClientPacketHandlerImpl(this.connection), new C2SLoginPacket(this.user.name()));
    }

    public void connectToServer(String host, int port) {
        this.connection = ClientTcpConnection.connectToServer(host, port).unwrap();

        // Initialize remote connection.
        this.multiplayerData = new MultiplayerData(this);

        this.connection.initiate(new LoginClientPacketHandlerImpl(this.connection), new C2SLoginPacket(this.user.name()));
    }

    /**
     * Get the current TPS (Ticks Per Second) value.
     *
     * @return the current TPS
     */
    public int getCurrentTps() {
        return this.currentTps;
    }

    public void setActivity(GameActivity activity) {
        this.activity = activity;
    }

    public void setFullScreen(boolean fullScreen) {
        if (Gdx.graphics.isFullscreen() != fullScreen) {
            if (fullScreen) {
                this.oldMode = new Vec2i(this.getWidth(), this.getHeight());
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(this.oldMode.x, this.oldMode.y);
            }
        }
    }

    public boolean isFullScreen() {
        return Gdx.graphics.isFullscreen();
    }

    /**
     * @return true if the player is in the third person, false otherwise.
     */
    public boolean isInThirdPerson() {
        return this.isInThirdPerson;
    }

    /**
     * @param thirdPerson true to set the player to be in the third person, false for first person.
     */
    public void setInThirdPerson(boolean thirdPerson) {
        this.isInThirdPerson = thirdPerson;
    }

    /**
     * Sets the GUI scale based on the provided scale value.
     * If autoScale is enabled, set the GUI scale to the maximum calculated scale.
     * If autoScale is disabled, sets the GUI scale to the provided scale value and resizes the GUI.
     *
     * @param guiScale The scale value to set the GUI to.
     */
    public void setGuiScale(float guiScale) {
        if (autoScale) {
            // If autoScale is enabled, set the GUI scale to the maximum calculated scale
            this.guiScale = this.calcMaxGuiScale();
            return;
        }

        // If autoScale is disabled, set the GUI scale to the provided scale value and resize the GUI
        this.guiScale = guiScale;
        this.resize(this.width, this.height);
    }

    public @Nullable MultiplayerData getMultiplayerData() {
        return this.multiplayerData;
    }

    public boolean isRenderingWorld() {
        return this.world != null && this.worldRenderer != null && this.renderWorld;
    }

    @Override
    public @NotNull String toString() {
        return "QuantumClient[" + this.user + "]";
    }

    public void runInTick(Runnable func) {
        this.serverTickQueue.add(func);
    }

    @ApiStatus.Internal
    public void pollServerTick() {
        Runnable task;
        while ((task = this.serverTickQueue.poll()) != null) {
            task.run();
        }
    }

    public User getUser() {
        return this.user;
    }

    public Environment getEnvironment() {
        if (this.worldRenderer != null) {
            return this.worldRenderer.getEnvironment();
        }
        return this.defaultEnv;
    }

    public RenderPipeline getPipeline() {
        return pipeline;
    }

    public GameWindow getWindow() {
        return window;
    }

    public SkinManager getSkinManager() {
        return skinManager;
    }

    public boolean isShowDebugHud() {
        return Config.enableDebugUtils;
    }

    public void setShowDebugHud(boolean showDebugHud) {
        Config.enableDebugUtils = showDebugHud;
        this.newConfig.save();
    }

    public void reloadResourcesAsync() {
        this.loadingOverlay = new LoadingOverlay();
        loading = true;
        CompletableFuture.runAsync(() -> {
            LOGGER.info("Reloading resources...");
            this.reloadResources();
            LOGGER.info("Resources reloaded.");
            this.loading = false;
            this.loadingOverlay = null;
        }).exceptionally(throwable -> {
            LOGGER.error("Failed to reload resources:", throwable);
            return null;
        });
    }

    private void reloadResources() {
        ReloadContext context = ReloadContext.create(this, this.resourceManager);
        this.resourceManager.reload();
        this.textureManager.reload(context);
        this.cubemapManager.reload(context);
        this.materialManager.reload(context);
        this.entityModelManager.reload(this.resourceManager, context);
        this.entityRendererManager.reload(this.resourceManager, context);
        this.textureAtlasManager.reload(context);
        if (this.itemRenderer != null)
            this.itemRenderer.reload();
        this.skinManager.reload();
        
        RenderingRegistration.registerRendering(this);
        
        if (this.worldRenderer != null) {
            this.worldRenderer.reload(context, materialManager);
        }

        while (!context.isDone()) {
            try {
                Duration.ofSeconds(0.1).sleep();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                crash(new IllegalThreadInterruptionError("Thread interrupted while reloading resources!", e));
            }
        }
    }

    public MaterialManager getMaterialManager() {
        return materialManager;
    }

    public ShaderProviderManager getShaderProviderManager() {
        return shaderProviderManager;
    }

    public ShaderProgramManager getShaderProgramManager() {
        return shaderProgramManager;
    }

    public void onDisconnect(String message) {
        this.showScreen(new DisconnectedScreen(message, !connection.isMemoryConnection()));
    }
}
