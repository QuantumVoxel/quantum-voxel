package dev.ultreon.quantum.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.RestrictedApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.libs.commons.v0.vector.Vec2f;
import dev.ultreon.libs.commons.v0.vector.Vec2i;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.api.events.ClientLifecycleEvents;
import dev.ultreon.quantum.client.api.events.ClientTickEvents;
import dev.ultreon.quantum.client.api.events.RenderEvents;
import dev.ultreon.quantum.client.api.events.gui.ScreenEvents;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.audio.ClientSound;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.config.ConfigScreenFactory;
import dev.ultreon.quantum.client.config.GameSettings;
import dev.ultreon.quantum.client.font.Font;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.debug.DebugOverlay;
import dev.ultreon.quantum.client.gui.overlay.LoadingOverlay;
import dev.ultreon.quantum.client.gui.overlay.ManualCrashOverlay;
import dev.ultreon.quantum.client.gui.overlay.OverlayManager;
import dev.ultreon.quantum.client.gui.screens.*;
import dev.ultreon.quantum.client.input.*;
import dev.ultreon.quantum.client.item.ItemRenderer;
import dev.ultreon.quantum.client.management.*;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BakedModelRegistry;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.model.model.Json5ModelLoader;
import dev.ultreon.quantum.client.multiplayer.MultiplayerData;
import dev.ultreon.quantum.client.network.LoginClientPacketHandlerImpl;
import dev.ultreon.quantum.client.network.system.ClientTcpConnection;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.player.SkinManager;
import dev.ultreon.quantum.client.registry.EntityModelRegistry;
import dev.ultreon.quantum.client.registry.EntityRendererRegistry;
import dev.ultreon.quantum.client.registry.ModIconOverrideRegistry;
import dev.ultreon.quantum.client.render.MeshManager;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.client.render.pipeline.*;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.client.rpc.GameActivity;
import dev.ultreon.quantum.client.shaders.provider.SceneShaders;
import dev.ultreon.quantum.client.sound.ClientSoundRegistry;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.client.text.LanguageManager;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.client.util.DeferredDisposable;
import dev.ultreon.quantum.client.util.GG;
import dev.ultreon.quantum.client.util.PlayerView;
import dev.ultreon.quantum.client.util.Resizer;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.debug.Debugger;
import dev.ultreon.quantum.debug.inspect.InspectionNode;
import dev.ultreon.quantum.debug.inspect.InspectionRoot;
import dev.ultreon.quantum.debug.profiler.Profiler;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.tool.ToolItem;
import dev.ultreon.quantum.network.MemoryConnectionContext;
import dev.ultreon.quantum.network.MemoryNetworker;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.C2SAttackPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SLoginPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.python.PyLoader;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.text.LanguageBootstrap;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.*;
import kotlin.OptIn;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
import space.earlygrey.shapedrawer.ShapeDrawer;

import javax.annotation.WillClose;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.math.MathUtils.ceil;
import static com.badlogic.gdx.utils.SharedLibraryLoader.isMac;

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
    private static final int MINIMUM_WIDTH = 480;
    private static final int MINIMUM_HEIGHT = 300;
    @SuppressWarnings("GDXJavaStaticResource")
    public static final Profiler PROFILER = new Profiler();
    public static final GridPoint2 MAXIMIZE_OFF = new GridPoint2(18, 0);
    private static final GridPoint2 ZERO = new GridPoint2();
    private static ArgParser arguments = new ArgParser();
    private static boolean crashing;
    private final Cursor normalCursor;
    private final Cursor clickCursor;
    private final RenderPipeline pipeline;
    public final Renderer renderer;
    public final IClipboard clipboard = getClipboard();
//    public Array<ParticleBatch<?>> batches = new Array<ParticleBatch<?>>(new ParticleBatch[]{
//            new BillboardParticleBatch(ParticleShader.AlignMode.Screen, true, 5000)
//    });
    private final AssetManager assetManager = new AssetManager(fileName -> new ResourceFileHandle(Identifier.parse(fileName)));
    private final ControlButton closeButton;
    private final ControlButton maximizeButton;
    private final ControlButton minimizeButton;
    public VideoPlayer backgroundVideo = VideoPlayerCreator.createVideoPlayer();
    public TouchPoint motionPointer = null;
    public Vector2 scrollPointer = new Vector2();
    public Json5ModelLoader j5ModelLoader;
    private boolean screenshotWorldOnly;
    public WorldStorage openedWorld;
    private final Map<String, ConfigScreenFactory> cfgScreenFactories = new HashMap<>();
    private final boolean windowVibrancyEnabled = false;
    private Cursor cursor0;
    private final Bounds gameBounds = new Bounds();
    private long lastPress;

    private IClipboard getClipboard() {
        if (GamePlatform.get().isMacOSX()) {
            return new NullClipboard();
        }

        return new GameClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
    }

    public final G3dModelLoader modelLoader;
    public IConnection<ClientPacketHandler, ServerPacketHandler> connection;
    public ServerData serverData;
    public ExecutorService chunkLoadingExecutor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 3, 1), r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("ChunkLoadingExecutor");
        return thread;
    });
    final boolean devWorld;
    boolean imGui = false;
    public InspectionRoot<QuantumClient> inspection;
    public ClientConfig newConfig;
    public boolean hideHud = false;

    Duration bootTime;
    GarbageCollector garbageCollector;
    GameEnvironment gameEnv;
    private final Sound logoRevealSound;
    private final Texture ultreonBgTex;
    private final Texture ultreonLogoTex;
    private final Texture libGDXLogoTex;
    private final Resizer resizer;
    private boolean showUltreonSplash = false;
    private boolean showLibGDXSplash = !GamePlatform.get().isDevEnvironment();
    private long ultreonSplashTime;
    private long libGDXSplashTime;
    public FileHandle configDir;
    public Metadata metadata = Metadata.load();

    private static final String FATAL_ERROR_MSG = "Fatal error occurred when handling crash:";
    public boolean forceUnicode = false;
    public ItemRenderer itemRenderer;
    public NotifyManager notifications = new NotifyManager(this);
    @SuppressWarnings("FieldMayBeFinal")
    boolean booted;
    public final Font font;
    public final Font newFont;
    @UnknownNullability
    public BitmapFont unifont;
    public GameInput input;
    @Nullable
    public ClientWorld world;
    private boolean skipScreenshot = false;
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
    boolean isDevMode;
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
    public BakedModelRegistry bakedBlockModels;

    // Advanced Shadows
    private final List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("QVoxelClientScheduler");
        return thread;
    });
    @Nullable
    Integer deferredWidth;
    @Nullable
    Integer deferredHeight;
    Texture windowTex;
    public DebugOverlay debugGui;
    private boolean closingWorld;
    private int oldSelected;
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private final List<Shutdownable> shutdownables = new CopyOnWriteArrayList<>();
    private final List<AutoCloseable> closeables = new CopyOnWriteArrayList<>();
    boolean loading = true;
    private final Thread mainThread;
    public BlockHitResult cursor;
    LoadingOverlay loadingOverlay;
    final String[] argv;
    public final ClientSoundRegistry soundRegistry = new ClientSoundRegistry();
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
    private final FontManager fontManager = FontManager.get();
    private final Queue<Disposable> disposalQueue = new ArrayDeque<>();
    private PlayerView playerView = PlayerView.FIRST_PERSON;

    public Vector2[] touchPosStartScl = new Vector2[Gdx.input.getMaxPointers()];
    public Vector2[] touchPosStart = new Vector2[Gdx.input.getMaxPointers()];
    public Vector2[] touchMovedScl = new Vector2[Gdx.input.getMaxPointers()];
    public Vector2[] touchMoved = new Vector2[Gdx.input.getMaxPointers()];

    {
        for (int i = 0; i < Gdx.input.getMaxPointers(); i++) {
            touchPosStartScl[i] = new Vector2();
            touchPosStart[i] = new Vector2();
            touchMovedScl[i] = new Vector2();
            touchMoved[i] = new Vector2();
        }
    }

    private final QuantumClientLoader loader = new QuantumClientLoader();

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
        this.unifont = deferDispose(new BitmapFont(Gdx.files.internal("assets/quantum/unifont/unifont.fnt"), true));
        this.font = new Font(new BitmapFont(Gdx.files.internal("assets/quantum/font/quantium.fnt"), true));
        this.newFont = new Font(new BitmapFont(Gdx.files.internal("assets/quantum/font/quantium.fnt"), true));

        // Initialize the game window
        this.window = GamePlatform.get().createWindow();

        // Initialize the inspection root
        this.inspection = deferDispose(new InspectionRoot<>(this));

        // Initialize the resource manager, texture manager, and resource loader
        this.resourceManager = new ResourceManager("assets");

        // Initialize shader provider and shader program manager. These should be initialized after the resource manager.
        this.shaderProviderManager = new ShaderProviderManager();
        this.shaderProgramManager = new ShaderProgramManager();

        // Locate resources by finding the ".ucraft-resources" file using Class.getResource() and using the parent file.
        GamePlatform.get().locateResources();

        // Set the language bootstrap
        LanguageBootstrap.bootstrap.set(Language::translate);

        this.textureManager = new TextureManager(this.resourceManager);
        this.cubemapManager = new CubemapManager(this.resourceManager);
        this.materialManager = new MaterialManager(this.resourceManager, this.textureManager, this.cubemapManager);
        this.textureAtlasManager = new TextureAtlasManager(this);

        // Load the configuration
        ModLoadingContext.withinContext(GamePlatform.get().getMod(CommonConstants.NAMESPACE).orElseThrow(), () -> {
            this.newConfig = new ClientConfig();
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
        this.imGui = !isMac && !SharedLibraryLoader.isAndroid && !SharedLibraryLoader.isARM && !SharedLibraryLoader.isIos;
        if (this.imGui)
            GamePlatform.get().preInitImGui();

        // Initialize the model loader
        G3dModelLoader modelLoader = new G3dModelLoader(new JsonReader());
        this.entityModelManager = new EntityModelRegistry(modelLoader, this);
        this.entityRendererManager = new EntityRendererRegistry(this.entityModelManager);
        this.modelLoader = modelLoader;

        // Initialize the game camera
        this.camera = new GameCamera(67, this.getWidth(), this.getHeight());
        this.camera.near = 0.1f;
        this.camera.far = 2;

        // Initialize the render pipeline
        this.pipeline = deferDispose(new RenderPipeline(new MainRenderNode(), this.camera)
                .node(new CollectNode())
                .node(new WorldNode())
                .node(new BackgroundNode()));

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
        shaderConfig.defaultDepthFunc = GL_LEQUAL;

        // Initialize ModelBatch with GameShaderProvider
        this.modelBatch = deferDispose(new ModelBatch(new SceneShaders(
                new ResourceFileHandle(id("shaders/scene.vert")),
                new ResourceFileHandle(id("shaders/scene.frag")),
                new ResourceFileHandle(id("shaders/scene.geom")))));

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
        LanguageManager.setCurrentLanguage(Locale.of("en", "us"));

        // Start memory monitor
        HardwareMonitor.start();

        this.closeButton = new ControlButton(ControlIcon.Close);
        this.maximizeButton = new ControlButton(ControlIcon.Maximize);
        this.minimizeButton = new ControlButton(ControlIcon.Minimize);

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
            link = "https://github.com/Ultreon/quantum-voxel/wiki/Crash-Hooks#important",
            allowlistAnnotations = UnsafeApi.class
    )
    @UnsafeApi
    public static void setCrashHook(Callback<CrashLog> crashHook) {
        QuantumClient.crashHook = crashHook;
    }

    private void onReloadConfig() {
        if (ClientConfig.fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        String[] split = ClientConfig.language.path().split("_");
        if (split.length == 2) {
            LanguageManager.setCurrentLanguage(Locale.of(split[0], split[1]));
        } else {
            QuantumClient.LOGGER.error("Invalid language: {}", ClientConfig.language);
            LanguageManager.setCurrentLanguage(Locale.of("en", "us"));
            ClientConfig.language = QuantumClient.id("en_us");
            this.newConfig.save();
        }

        if (ClientConfig.guiScale != 0) {
            this.setAutomaticScale(false);
            this.setGuiScale(ClientConfig.guiScale);
        } else {
            this.setAutomaticScale(true);
        }

        this.camera.fov = ClientConfig.fov;

        notifications.addOnce(UUID.fromString("b26c6826-1086-4b34-a5f2-5172e65bb55f"), Notification.builder("Missing Feature", "Discord RPC is not implemented yet!").build());
//        if (ClientConfig.hideRPC) {
//            RpcHandler.disable();
//        } else {
//            RpcHandler.enable();
//            if (this.activity != null) {
//                this.setActivity(this.activity);
//                RpcHandler.setActivity(this.activity);
//            }
//        }

        if (!ClientConfig.vibration) {
            GameInput.cancelVibration();
        }

        GamePlatform.get().setTransparentFBO(ClientConfig.useFullWindowVibrancy);

        QuantumClient.invoke(() -> {
            boolean enableVsync = ClientConfig.enableVsync;
            Gdx.graphics.setVSync(enableVsync);

            int fpsLimit = ClientConfig.fpsLimit;
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
     * Makes a screenshot of the game.
     */
    public CompletableFuture<Screenshot> screenshot(boolean worldOnly) {
        this.screenshotWorldOnly = worldOnly;
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
    @OptIn(markerClass = InternalApi.class)
    private static void launch(String[] argv) {
    }

    public static void logDebug() {
        if (QuantumClient.isPackaged()) QuantumClient.LOGGER.warn("Running in the JPackage environment.");
        QuantumClient.LOGGER.debug("Java Version: {}", System.getProperty("java.version"));
        QuantumClient.LOGGER.debug("Java Vendor: {}", System.getProperty("java.vendor"));
        QuantumClient.LOGGER.debug("Operating System: {} {} ({})", new Object[]{System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch")});
    }

    public static String[] getIcons() {
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
        return GamePlatform.get().getGameDir();
    }

    public void setAutomaticScale(boolean b) {
        this.autoScale = b;
        this.guiScale = this.calcMaxGuiScale();
        this.resize(this.width, this.height);
    }

    public void startDevWorld() {
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

    /**
     * Set up mods by invoking entry points using {@link GamePlatform#invokeEntrypoint(String, Class, Consumer)}.
     * This should be done at the start of the game.
     * <p>
     * See {@link ModInit} and {@link ClientModInit}
     * Thi also initializes and loads configurations from entry points.
     */
    private void setupMods() {
        if (GamePlatform.get().isDesktop())
            PyLoader.getInstance().initMods();

        // Set mod icon overrides.
        ModIconOverrideRegistry.set("quantum", QuantumClient.id("icon.png"));
        ModIconOverrideRegistry.set("gdx", new Identifier("gdx", "icon.png"));

        // Invoke entry points for initialization.
        GamePlatform loader = GamePlatform.get();
        loader.invokeEntrypoint(ModInit.ENTRYPOINT_KEY, ModInit.class, ModInit::onInitialize);
        loader.invokeEntrypoint(ClientModInit.ENTRYPOINT_KEY, ClientModInit.class, ClientModInit::onInitializeClient);

        CommonLoader.initConfigEntrypoints(GamePlatform.get());
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
        if (isOnMainThread()) {
            try {
                return func.call();
            } catch (Exception e) {
                throw new RejectedExecutionException("Failed to execute task", e);
            }
        }
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
        if (isOnMainThread()) {
            func.run();
            return;
        }

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
        return Thread.currentThread().threadId() == QuantumClient.instance.mainThread.threadId();
    }

    /**
     * Gets an identifier from a path with the Quantum Voxel namespace, but as a string.
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

    static FileHandle createDir(String dirName) {
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

        if (!skipScreenshot && next instanceof PauseScreen pause && world != null) {
            this.screenshot(true).thenAccept(screenshot -> {
                if (screenshot != null) {
                    screenshot.save(openedWorld.getDirectory().resolve("picture.png"));
                }

                this.skipScreenshot = true;
                invoke(() -> {
                    boolean b = this.showScreen(pause);
                    this.skipScreenshot = false;
                    return b;
                });
            });
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

        if (this.screen == null) {
            this.setWindowTitle(TextObject.literal("Playing in a world!"));
        } else  {
            this.setWindowTitle(this.screen.getTitle());
        }

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

        renderer.finish();
    }

    private void doRender(float deltaTime) {
        this.width = Gdx.graphics.getWidth();
        this.height = Gdx.graphics.getHeight();

        if (this.width == 0 || this.height == 0) return;

        if (this.screenshotWorldOnly) {
            ScreenUtils.clear(0, 0, 0, 0, true);
            this.gameRenderer.renderWorld(0f, deltaTime);
            this.captureScreenshot = false;
            this.triggerScreenshot = false;

            Screenshot grabbed = Screenshot.grab(this.width, this.height);
            screenshotFuture.complete(grabbed);
            ScreenUtils.clear(0, 0, 0, 0, true);
            this.screenshotWorldOnly = false;
        }

        if (this.triggerScreenshot) this.prepareScreenshot();

        QuantumClient.PROFILER.section("renderGame", () -> this.renderGame(renderer, deltaTime));

        if (this.captureScreenshot && !this.screenshotWorldOnly) this.handleScreenshot();

        if (!this.screenshotWorldOnly && this.screenshotFlashTime > System.currentTimeMillis() - 200) {
            this.renderer.begin();
            this.shapes.filledRectangle(0, 0, this.width, this.height, this.tmpColor.set(1, 1, 1, 1 - (System.currentTimeMillis() - this.screenshotFlashTime) / 200f));
            this.renderer.end();
        }

        if (this.isCustomBorderShown() && !loading) this.drawCustomBorder(renderer);

        if (this.imGui) {
            GamePlatform.get().renderImGui();
        }

        if (this.window.isDragging()) {
            Texture texture = null;
            if (this.cursor0 == clickCursor) {
                texture = textureManager.getTexture(id("textures/cursors/click.png"));
            }
            if (this.cursor0 == normalCursor) {
                texture = textureManager.getTexture(id("textures/cursors/normal.png"));
            }
            if (texture != null) {
                this.spriteBatch.begin();
                this.spriteBatch.draw(texture, window.dragOffX, window.dragOffY - texture.getHeight() + getDrawOffset().y * 1.5f, texture.getWidth(), -texture.getHeight());
                this.spriteBatch.end();
            }
        }
    }

    private void prepareScreenshot() {
        this.screenshotScale = 1;

        if (ClientConfig.enable4xScreenshot && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
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
        this.renderWindow(renderer, this.getWidth() / 2 + 36, this.getHeight() / 2 + 44);
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
            this.firstRender();
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
        if (GamePlatform.get().isDevEnvironment() && this.startDevLoading) {
            this.startLoading();
        }
        if (renderSplash) {
            return true;
        }

        final LoadingOverlay loading = this.loadingOverlay;
        if (loading != null) {
            this.renderLoadingOverlay(renderer, deltaTime, loading);
            return true;
        }

        ScreenUtils.clear(0, 0, 0, 0, true);

        this.gameBounds.setPos(0, 0);
        this.gameBounds.setSize(getScaledWidth() + getDrawOffset().x / 2, getScaledHeight() + getDrawOffset().y / 2);
        if (renderer.pushScissors(this.gameBounds)) {
            this.renderMain(renderer, deltaTime);
            renderer.popScissors();
        }
        return false;
    }

    private void renderMain(Renderer renderer, float deltaTime) {
        Player player = this.player;
        if (player == null) {
            this.hitResult = null;
        } else {
            QuantumClient.PROFILER.section("playerRayCast", () -> this.hitResult = player.rayCast());
        }

        renderer.begin();
        GridPoint2 drawOffset = this.getDrawOffset();
        renderer.fill(drawOffset.x, drawOffset.y, (int) (this.gameBounds.getWidth() * getGuiScale()) - drawOffset.x * 2, (int) (this.gameBounds.getHeight() * getGuiScale()) - drawOffset.y * 2, RgbColor.BLACK);
        renderer.end();

        GameInput input = this.input;
        if (input != null) {
            QuantumClient.PROFILER.section("input", input::update);
        }

        Screen screen = this.screen;
        if (screen != null && DesktopInput.isPressingAnyButton() && !this.wasClicking) {
            this.setCursor(this.clickCursor);
            this.wasClicking = true;
        } else if (screen != null && !DesktopInput.isPressingAnyButton() && this.wasClicking) {
            this.setCursor(this.normalCursor);
            this.wasClicking = false;
        }

        RenderEvents.PRE_RENDER_GAME.factory().onRenderGame(gameRenderer, renderer, deltaTime);
        this.gameRenderer.render(renderer, deltaTime);
        RenderEvents.POST_RENDER_GAME.factory().onRenderGame(gameRenderer, renderer, deltaTime);
    }

    private void setCursor(Cursor cursor) {
        this.cursor0 = cursor;
        if (this.window.isDragging()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        } else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            Gdx.graphics.setCursor(this.cursor0);
        }
    }

    private void renderLoadingOverlay(Renderer renderer, float deltaTime, LoadingOverlay loading) {
        QuantumClient.PROFILER.section("loading", () -> {
            renderer.begin();
            renderer.pushMatrix();
            renderer.translate(this.getDrawOffset().x, this.getDrawOffset().y);
            renderer.scale(this.getGuiScale(), this.getGuiScale());
            renderer.clearColor(0, 0, 0, 1);
            loading.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
            renderer.popMatrix();
            renderer.end();
        });
    }

    private void startLoading() {
        this.startDevLoading = false;

        QuantumClient.setCrashHook(this.crashes::add);

        this.loadingOverlay = new LoadingOverlay();

        CompletableFuture.runAsync(loader).exceptionally(throwable -> {
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
                this.screen = this.crashes.isEmpty() ? new DevPreviewScreen() : new CrashScreen(this.crashes);
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
     * Retrieves the game version of the Quantum Voxel mod.
     *
     * @return The game version as a {@code String}.
     */
    public static String getGameVersion() {
        return GamePlatform.get().getMod("quantum").orElseThrow().getVersion();
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

            notifications.addOnce(UUID.fromString("35c2d972-6699-4cf6-86d3-1f2daaedfc47"), Notification.builder("Missing Feature", "Game activity updates are not implemented yet!").build());
//            RpcHandler.setActivity(this.activity);
        }
    }

    private void saveScreenshot() {
        if (this.spriteBatch.isDrawing()) this.spriteBatch.flush();
        if (this.modelBatch.getCamera() != null) this.modelBatch.flush();

        Screenshot grabbed = Screenshot.grab(this.width, this.height);
        FileHandle save = grabbed.save(String.format("screenshots/%s.png", DateTimeFormatter.ofPattern("MM.dd.yyyy-HH.mm.ss").format(LocalDateTime.now())));

        this.screenshotFlashTime = System.currentTimeMillis();

        this.playSound(SoundEvents.SCREENSHOT, 0.5f);

        this.notifications.add("Screenshot taken.", save.name(), "screenshots");

        this.screenshotFuture.complete(grabbed);
    }

    private void firstRender() {
//        if (SharedLibraryLoader.isWindows) {
//            InputStream resourceAsStream = QuantumClient.class.getResourceAsStream("/assets/quantum/native/acrylic.dll");
//            try {
//                if (!Files.exists(Paths.get(".", "acrylic.dll")))
//                    Files.copy(resourceAsStream, Paths.get(".", "acrylic.dll"));
//                if (System.getProperty("os.name").endsWith(" 11")) {
//                    Acrylic.applyMica(getWindow().getPeer());
//                    this.windowVibrancyEnabled = true;
//                }
//            } catch (Exception e) {
//                LOGGER.warn("Acylic/mica effects not available", e);
//            }
//        }

        GamePlatform.get().setVisible(true);
    }

    private void renderWindow(Renderer renderer, int width, int height) {
        boolean maximized = window.isMaximized();
        int winXOff = maximized ? 18 : 0;
        int winHOff = maximized ? 22 : 0;
        renderer.draw9PatchTexture(new Identifier("textures/gui/window.png"), -winXOff, 0, width + winXOff * 2, height + winHOff, 0, 0, 18, 22, 256, 256);
        renderer.textCenter("<bold>" + window.getTitle(), width / 2, 5);

        this.closeButton.x(width - 17 - winXOff * 2);
        this.closeButton.y(3);

        this.maximizeButton.x(width - 35 - winXOff * 2);
        this.maximizeButton.y(3);

        this.minimizeButton.x(width - 53 - winXOff * 2);
        this.minimizeButton.y(3);

        this.closeButton.render(renderer, Gdx.input.getX() / 2, Gdx.input.getY() / 2, 0);
        this.maximizeButton.render(renderer, Gdx.input.getX() / 2, Gdx.input.getY() / 2, 0);
        this.minimizeButton.render(renderer, Gdx.input.getX() / 2, Gdx.input.getY() / 2, 0);

        this.window.update();
    }

    private GridPoint2 getWindowOffset() {
        if (window.isMaximized()) return MAXIMIZE_OFF;
        return ZERO;
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
            Main.displayCrash(crash);
        } catch (Exception | OutOfMemoryError t) {
            QuantumClient.LOGGER.error(QuantumClient.FATAL_ERROR_MSG, t);
            System.exit(1);
        }
    }

    private static void cleanUp(@Nullable Disposable disposable) {
        if (disposable == null) return;

        Debugger.log(Debugger.Type.CLEAN_UP, "Cleaning up " + disposable.getClass().getName());

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

        Debugger.log(Debugger.Type.CLEAN_UP, "Cleaning up " + disposable.getClass().getName());

        try {
            disposable.shutdownNow();
        } catch (Exception throwable) {
            Debugger.log("Failed to shut down " + disposable.getClass().getName(), throwable);
        }
    }

    private static void cleanUp(@Nullable AutoCloseable disposable) {
        if (disposable == null) return;

        Debugger.log(Debugger.Type.CLEAN_UP, "Cleaning up " + disposable.getClass().getName());

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

            if (hitResult instanceof BlockHitResult blockHitResult) {
                this.handleBlockBreaking(breaking, blockHitResult);
            }
        }

        // Update camera based on player position
        if (player != null) {
            this.camera.update(player);
        }

        // Execute post-game tick event
        ClientTickEvents.POST_GAME_TICK.factory().onGameTick(this);
    }

    private void handleBlockBreaking(BlockPos breaking, BlockHitResult hitResult) {
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

            if (world.continueBreaking(breaking, 1.0F / (Math.max((this.breakingBlock.getHardness() / efficiency) * QuantumServer.TPS, 1) + 1), this.player) != BreakResult.CONTINUE) {
                this.resetBreaking();
            } else {
                if (this.oldSelected != this.player.selected) {
                    this.resetBreaking();
                }
                this.oldSelected = this.player.selected;
            }
        }
    }

    private void resetBreaking(BlockHitResult hitResult) {
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
            float w = width / this.getGuiScale();
            float h = height / this.getGuiScale();
            cur.resize(ceil(w), ceil(h));
        }

        OverlayManager.resize(ceil(width / this.getGuiScale()), ceil(height / this.getGuiScale()));
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

//                QuantumServer.getWatchManager().stop();

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
                    GamePlatform.get().onGameDispose();
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

                // Clear scenes
                RenderLayer.BACKGROUND.clear();
                RenderLayer.WORLD.clear();

                // Dispose Models
                ModelManager.INSTANCE.dispose();
                QuantumClient.cleanUp(this.bakedBlockModels.atlas());
                QuantumClient.cleanUp(this.bakedBlockModels);
                QuantumClient.cleanUp(this.entityModelManager);

                MeshManager.INSTANCE.dispose();

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
                System.gc();
            } catch (Exception t) {
                QuantumClient.crash(t);
            }
        }
    }

    public boolean isDevMode() {
        return this.isDevMode;
    }

    @Deprecated
    public boolean isShowingImGui() {
        return GamePlatform.get().isShowingImGui();
    }

    public void setShowingImGui(boolean value) {
        GamePlatform.get().setShowingImGui(value);
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
        return ceil(Gdx.graphics.getWidth() / this.getGuiScale() - getDrawOffset().x);
    }

    public int getScaledHeight() {
        return ceil(Gdx.graphics.getHeight() / this.getGuiScale() - (float) getDrawOffset().y / (window.isMaximized() ? 2 : 1));
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

        if (ClientConfig.showClosePrompt && this.screen != null) {
            this.screen.showDialog(new DialogBuilder(this.screen).message(TextObject.literal("Are you sure you want to close the game?")).button(UITranslations.YES, () -> {
                if (this.world != null) {
                    this.exitWorldAndThen(() -> Gdx.app.exit());
                    return;
                }

                Gdx.app.exit();
            }));
            return false;
        }

        if (this.world != null) {
            this.exitWorldAndThen(() -> Gdx.app.exit());
            return false;
        }

        return true;
    }

    public boolean filesDropped(String[] files) {
        var currentScreen = this.screen;
        var handles = Arrays.stream(files).map(FileHandle::new).collect(Collectors.toList());

        if (currentScreen != null) {
            return currentScreen.filesDropped(handles);
        }

        return false;
    }

    public void addFuture(CompletableFuture<?> future) {
        this.futures.add(future);
    }

    public @NotNull BlockModel getBlockModel(BlockProperties block) {
        List<Pair<Predicate<BlockProperties>, BakedCubeModel>> orDefault = this.bakedBlockModels.bakedModels().getOrDefault(block.getBlock(), List.of());

        if (orDefault == null) {
            BlockModel blockModel = BlockModelRegistry.get().get(block);
            return Objects.requireNonNullElse(blockModel, BakedCubeModel.defaultModel());

        }
        
        BlockModel blockModel = orDefault
                .stream()
                .filter(pair -> pair.getFirst().test(block))
                .findFirst()
                .map(pair -> (BlockModel) pair.getSecond())
                .orElseGet(() -> BlockModelRegistry.get().get(block));

        if (blockModel == null) return BakedCubeModel.defaultModel();

        return blockModel;
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

        if (!(hitResult instanceof BlockHitResult blockHitResult)) return;

        // Check for null conditions and return if any are true
        if (this.world == null || player == null) return;

        // Stop and start breaking at the hit position for the player
        this.world.stopBreaking(new BlockPos(blockHitResult.getPos()), player);
        this.world.startBreaking(new BlockPos(blockHitResult.getPos()), player);

        // Update the breaking position and block meta
        this.breaking = blockHitResult.getPos();
        this.breakingBlock = blockHitResult.getBlockMeta();
    }

    /**
     * Starts the process of breaking a block in the game world.
     * If the player is already breaking a block, it stops the current process and starts a new one.
     */
    public void startBreaking() {
        // Get the hit result and player
        HitResult hitResult = this.hitResult;
        LocalPlayer player = this.player;

        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            this.breaking = null;
            this.breakingBlock = null;
            return;
        }

        // If hit result or world is null, return
        if (this.world == null) {
            return;
        }

        // If the block being hit is already broken, return
        if (this.world.getBreakProgress(new BlockPos(blockHitResult.getPos())) >= 0.0F) {
            return;
        }

        // If the player is null, return
        if (player == null) {
            return;
        }

        if (this.breaking != null) {
            this.world.stopBreaking(new BlockPos(this.breaking), player);
            return;
        }

        // Start breaking the block and update the breaking position and block metadata
        this.world.startBreaking(new BlockPos(blockHitResult.getPos()), player);
        this.breaking = blockHitResult.getPos();
        this.breakingBlock = blockHitResult.getBlockMeta();
    }

    public void stopBreaking() {
        HitResult hitResult = this.hitResult;
        LocalPlayer player = this.player;

        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            this.breaking = null;
            this.breakingBlock = null;
            return;
        }

        if (this.world == null || player == null || this.breaking == null) return;

        this.world.stopBreaking(new BlockPos(blockHitResult.getPos()), player);
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
            return Math.max(windowWidth / QuantumClient.MINIMUM_WIDTH, 1);
        }

        if (windowHeight / QuantumClient.MINIMUM_HEIGHT < windowWidth / QuantumClient.MINIMUM_WIDTH) {
            return Math.max(windowHeight / QuantumClient.MINIMUM_HEIGHT, 1);
        }

        int min = Math.min(windowWidth / QuantumClient.MINIMUM_WIDTH, windowHeight / QuantumClient.MINIMUM_HEIGHT);
        return Math.max(min, 1);
    }

    public boolean isPlaying() {
        return this.world != null && this.screen == null;
    }

    public static FileHandle getConfigDir() {
        return QuantumClient.instance.configDir;
    }

    public GridPoint2 getDrawOffset() {
        return this.isCustomBorderShown() ? new GridPoint2(window.isMaximized() ? 0 : 18 * 2, 22 * 2) : new GridPoint2();
    }

    @ApiStatus.Experimental
    public boolean isCustomBorderShown() {
        return GamePlatform.get().isDesktop() && !loading;
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
            QuantumClient.LOGGER.warn("Unknown sound event: %s", soundEvent.getId());
            return;
        }
        sound.play(volume);
    }

    public void startIntegratedServer() {
        var mem = ClientTcpConnection.connectToLocalServer().unwrap();
        this.connection = mem;
        MemoryConnectionContext.set(mem);

        this.integratedServer.start();

        mem.setOtherSide(((MemoryNetworker) this.integratedServer.getNetworker()).getConnections().getFirst());

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
        return this.playerView == PlayerView.THIRD_PERSON || this.playerView == PlayerView.THIRD_PERSON_FRONT;
    }

    /**
     * @param thirdPerson true to set the player to be in the third person, false for first person.
     * @deprecated Use {@link #setPlayerView(PlayerView)}
     */
    @Deprecated
    public void setInThirdPerson(boolean thirdPerson) {
        if (thirdPerson) {
            this.playerView = PlayerView.THIRD_PERSON;
        } else {
            this.playerView = PlayerView.FIRST_PERSON;
        }
    }

    public void setPlayerView(PlayerView playerView) {
        this.playerView = playerView;
    }

    public PlayerView getPlayerView() {
        return this.playerView;
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
        return ClientConfig.enableDebugUtils;
    }

    public void setShowDebugHud(boolean showDebugHud) {
        ClientConfig.enableDebugUtils = showDebugHud;
        this.newConfig.save();
    }

    public void reloadResourcesAsync() {
        if (!isOnMainThread()) {
            invokeAndWait(this::reloadResourcesAsync);
            return;
        }

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

    public void reloadResources() {
        ReloadContext context = ReloadContext.create(this, this.resourceManager);
        this.resourceManager.reload();
        this.textureManager.reload(context);
        this.cubemapManager.reload(context);
        this.materialManager.reload(context);

        QuantumClient.LOGGER.info("Initializing sounds");
        this.soundRegistry.reload();

        this.entityModelManager.reload(this.resourceManager, context);
        this.entityRendererManager.reload(this.resourceManager, context);
        this.textureAtlasManager.reload(context);
        this.shaderProgramManager.reload(context);
        this.shaderProviderManager.reload(context);
        if (this.itemRenderer != null)
            this.itemRenderer.reload();
        this.skinManager.reload();

        BlockModelRegistry.get().reload(resourceManager, context);

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

    public void cyclePlayerView() {
        if (this.playerView == PlayerView.FIRST_PERSON) {
            this.playerView = PlayerView.THIRD_PERSON;
        } else if (this.playerView == PlayerView.THIRD_PERSON) {
            this.playerView = PlayerView.THIRD_PERSON_FRONT;
        } else if (this.playerView == PlayerView.THIRD_PERSON_FRONT) {
            this.playerView = PlayerView.FIRST_PERSON;
        }
    }

    public void attack(Entity entity) {
        this.connection.send(new C2SAttackPacket(entity));
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public ConfigScreenFactory getModConfigScreen(Mod caller) {
        return cfgScreenFactories.get(caller.getId());
    }

    public void setModConfigScreen(Mod caller, ConfigScreenFactory factory) {
        cfgScreenFactories.put(caller.getId(), factory);
    }

    public CubemapManager getCubemapManager() {
        return cubemapManager;
    }

    public boolean isWindowVibrancyEnabled() {
        return windowVibrancyEnabled;
    }

    public boolean mousePress(int mouseX, int mouseY, int button) {
        if (mouseY < 44 && button == Input.Buttons.LEFT) {
            if (closeButton.isWithinBounds(mouseX - 18, mouseY - 22)) return closeButton.mousePress(mouseX - 18, mouseY - 22, button);
            if (maximizeButton.isWithinBounds(mouseX - 18, mouseY - 22)) return maximizeButton.mousePress(mouseX - 18, mouseY - 22, button);
            if (minimizeButton.isWithinBounds(mouseX - 18, mouseY - 22)) return minimizeButton.mousePress(mouseX - 18, mouseY - 22, button);

            if (this.lastPress - System.currentTimeMillis() + 1000L > 0) {
                lastPress = 0;
                window.setResizable(true);
                if (isMac) {
                    window.maximize();
                } else {
                    if (window.isMaximized())
                        window.restore();
                    else
                        window.maximize();
                }
            } else {
                this.window.setDragging(true);
            }
            this.lastPress = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        mouseX /= 2;
        mouseY /= 2;
        if (mouseY < 44) {
            closeButton.mouseRelease(mouseX, mouseY, button);
            maximizeButton.mouseRelease(mouseX, mouseY, button);
            minimizeButton.mouseRelease(mouseX, mouseY, button);
            if (closeButton.isWithinBounds(mouseX, mouseY))
                this.window.close();
            if (maximizeButton.isWithinBounds(mouseX, mouseY)) {
                if (!this.window.isMaximized()) this.window.maximize();
                else this.window.restore();
            }
            if (minimizeButton.isWithinBounds(mouseX, mouseY))
                this.window.minimize();

            this.window.setDragging(false);
            return true;
        }
        return false;
    }

    public void setWindowTitle(TextObject title) {
        String text = title == null ? null : title.getText();
        if (text != null && !text.isBlank()) {
            this.window.setTitle(String.format("Quantum Voxel %s - %s", QuantumClient.getGameVersion().split("\\+")[0], text));
            return;
        }
        this.window.setTitle(String.format("Quantum Voxel %s", QuantumClient.getGameVersion().split("\\+")[0]));
    }
}
