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
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.RestrictedApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.mixinprovider.PlatformOS;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.api.events.ClientLifecycleEvents;
import dev.ultreon.quantum.client.api.events.ClientTickEvents;
import dev.ultreon.quantum.client.api.events.RenderEvents;
import dev.ultreon.quantum.client.api.events.WindowEvents;
import dev.ultreon.quantum.client.api.events.gui.ScreenEvents;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.audio.ClientSound;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.config.ConfigScreenFactory;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.debug.DebugOverlay;
import dev.ultreon.quantum.client.gui.overlay.LoadingOverlay;
import dev.ultreon.quantum.client.gui.overlay.ManualCrashOverlay;
import dev.ultreon.quantum.client.gui.overlay.OverlayManager;
import dev.ultreon.quantum.client.gui.screens.*;
import dev.ultreon.quantum.client.input.*;
import dev.ultreon.quantum.client.input.controller.ControllerContext;
import dev.ultreon.quantum.client.input.controller.ControllerInput;
import dev.ultreon.quantum.client.input.controller.context.InGameControllerContext;
import dev.ultreon.quantum.client.input.controller.context.MenuControllerContext;
import dev.ultreon.quantum.client.input.controller.gui.VirtualKeyboard;
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
import dev.ultreon.quantum.client.render.*;
import dev.ultreon.quantum.client.render.pipeline.*;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.client.rpc.GameActivity;
import dev.ultreon.quantum.client.rpc.RpcHandler;
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
import dev.ultreon.quantum.client.world.ClientWorldAccess;
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
import dev.ultreon.quantum.js.JsLoader;
import dev.ultreon.quantum.network.MemoryConnectionContext;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.c2s.C2SAttackPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SLoginPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.network.system.MemoryConnection;
import dev.ultreon.quantum.python.PyLoader;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.text.LanguageBootstrap;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.BreakResult;
import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.WorldStorage;
import dev.ultreon.quantum.world.vec.BlockVec;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.earlygrey.shapedrawer.ShapeDrawer;

import javax.annotation.WillClose;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.math.MathUtils.ceil;
import static dev.ultreon.mixinprovider.PlatformOS.isMac;

/**
 * This class is the main entry point for the Quantum Voxel Client.
 * It is responsible for initializing and running the game.
 * It also provides access to the game's main elements and resources.
 *
 * @author <a href="https://github.com/Ultreon">Ultreon Studios</a>
 * @see <a href="https://github.com/Ultreon/quantum-voxel">Quantum Voxel</a>
 * @since <i>Always :smirk:</i>
 */
@SuppressWarnings("UnusedReturnValue")
public non-sealed class QuantumClient extends PollingExecutorService implements DeferredDisposable, DesktopMain {
    // Public constants
    public static final Logger LOGGER = LoggerFactory.getLogger("QuantumClient");
    public static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setPrettyPrinting().create();
    public static final int[] SIZES = new int[]{16, 24, 32, 40, 48, 64, 72, 80, 96, 108, 128, 160, 192, 256, 1024};
    public static final float FROM_ZOOM = 2.0f;
    public static final float TO_ZOOM = 1.3f;

    // Profiler
    @SuppressWarnings("GDXJavaStaticResource")
    public static final Profiler PROFILER = new Profiler();

    // Maximize offset for custom window border
    public static final GridPoint2 MAXIMIZE_OFF = new GridPoint2(18, 0);

    // Constants
    private static final float DURATION = 6000f;

    // Maximum-scaled size before automatic resize.
    // This is used for the "Automatic" gui scale.
    private static final int MINIMUM_WIDTH = 550;
    private static final int MINIMUM_HEIGHT = 300;

    // Zero, what else could it be? :thinking:
    private static final GridPoint2 ZERO = new GridPoint2();
    private static final ThreadGroup CLIENT_GROUP = new ThreadGroup("RenderGroup");

    // Client instance
    @UnknownNullability
    @SuppressWarnings("GDXJavaStaticResource")
    private static QuantumClient instance;

    // Arguments
    private static final ArgParser arguments = new ArgParser();

    // Crash handling
    private static boolean crashing;

    @ApiStatus.Experimental
    private static Callback<CrashLog> crashHook;

    private final Thread mainThread = Thread.currentThread();

    private final List<CrashLog> crashes = new CopyOnWriteArrayList<>();
    public int viewMode;
    public NamespaceID fontId = id("quantium");
    public final ExecutorService executor = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors() / 2, 2), r -> {
        Thread thread = new Thread(CLIENT_GROUP, r);
        thread.setName("ClientTask");
        thread.setDaemon(true);
        return thread;
    });
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Math.min(Runtime.getRuntime().availableProcessors() / 2, 2), r -> {
        Thread thread = new Thread(CLIENT_GROUP, r);
        thread.setName("ClientScheduleTask");
        thread.setDaemon(true);
        return thread;
    });
    public ControllerInput controllerInput;
    public TouchInput touchInput;
    public VirtualKeyboard virtualKeyboard;

    public SceneCategory backgroundCat = new SceneCategory();
    public SceneCategory worldCat = new SceneCategory();
    public SceneCategory mainCat = new SceneCategory();

    // Local data
    public LocalData localData = LocalData.load();

    ManualCrashOverlay crashOverlay; // MANUALLY_INITIATED_CRASH

    // Cursors
    private final Cursor normalCursor;
    private final Cursor clickCursor;
    private Cursor cursor0;

    // Render pipeline
    private final RenderPipeline pipeline;

    /**
     * The clipboard for the game.
     */
    public final IClipboard clipboard = createClipboard();

    // Particle batches (currently disabled), so this is a TODO
//    public Array<ParticleBatch<?>> batches = new Array<ParticleBatch<?>>(new ParticleBatch[]{
//            new BillboardParticleBatch(ParticleShader.AlignMode.Screen, true, 5000)
//    });

    // Useless asset manager
    private final AssetManager assetManager = new AssetManager(fileName -> new ResourceFileHandle(NamespaceID.parse(fileName)));

    // Window buttons
    private final ControlButton closeButton;
    private final ControlButton maximizeButton;
    private final ControlButton minimizeButton;

    // Input
    public TouchPoint motionPointer = null;
    public Vector2 scrollPointer = new Vector2();

    // JSON5 model loader
    public Json5ModelLoader j5ModelLoader;
    public WorldStorage openedWorld;
    private final Map<String, ConfigScreenFactory> cfgScreenFactories = new HashMap<>();

    // Window vibrancy
    private final boolean windowVibrancyEnabled = false;
    private final Rectangle gameBounds = new Rectangle();
    private long lastCBPress;

    // G3D model loader (libGDX 3D models)
    public final G3dModelLoader modelLoader;

    // Multiplayer stuff
    public IConnection<ClientPacketHandler, ServerPacketHandler> connection;
    public ServerInfo serverInfo;
    private MultiplayerData multiplayerData;

    // Local user
    private User user;

    // Developer stuff
    final boolean devWorld;
    boolean imGui = false;
    boolean isDevMode;

    private boolean startDevLoading = false;

    public InspectionRoot<QuantumClient> inspection;

    // Configuration
    public ClientConfig newConfig;

    // HUD stuff
    public boolean hideHud = false;

    // Boot info
    @SuppressWarnings("FieldMayBeFinal")
    boolean booted;
    Duration bootTime;

    // Splash stuff
    private final Sound logoRevealSound;
    private final Texture ultreonBgTex;
    private final Texture ultreonLogoTex;
    private final Texture libGDXLogoTex;
    private final Resizer resizer;
    private boolean showUltreonSplash = false;
    private boolean showLibGDXSplash = true;
//    private boolean showLibGDXSplash = !GamePlatform.get().isDevEnvironment();
    private long ultreonSplashTime;
    private long libGDXSplashTime;

    // File handles
    public FileHandle configDir;

    // Metadata
    public Metadata metadata = Metadata.get();

    // Error messages
    private static final String FATAL_ERROR_MSG = "Fatal error occurred when handling crash:";

    // Finalize lists. Those are used to clean up after the game is shutdown.
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private final List<Shutdownable> shutdownables = new CopyOnWriteArrayList<>();
    private final List<AutoCloseable> closeables = new CopyOnWriteArrayList<>();

    // Font stuff
    public GameFont font;
    private GameFont unifont;

    // Generic renderers
    @Nullable
    public WorldRenderer worldRenderer;
    public ItemRenderer itemRenderer;
    private GameRenderer gameRenderer;

    // GUI stuff
    @Nullable
    public Screen screen;
    public Hud hud;

    private FrameBuffer fbo;

    public SpriteBatch spriteBatch;
    public ModelBatch modelBatch;
    public ShapeDrawer shapes;
    public Renderer renderer;

    private float guiScale = this.calcMaxGuiScale();

    // Notifications
    public Notifications notifications = new Notifications(this);

    // Input
    public KeyAndMouseInput keyAndMouseInput;

    // World
    public ClientWorld world;

    // Screenshots
    private CompletableFuture<Screenshot> screenshotFuture;
    private boolean skipScreenshot = false;
    private boolean screenshotWorldOnly = false;
    private boolean triggerScreenshot = false;
    private boolean captureScreenshot = false;
    public int screenshotScale = 4;
    private long screenshotFlashTime = 0;

    // Player
    @Nullable
    public LocalPlayer player;
    public final GameCamera camera;
    public final PlayerInput playerInput = new PlayerInput();

    // Managers
    private final TextureManager textureManager;
    private final CubemapManager cubemapManager;
    private final ResourceManager resourceManager;
    private final SkinManager skinManager = new SkinManager();
    private final MaterialManager materialManager;
    private final ShaderProviderManager shaderProviderManager;
    private final ShaderProgramManager shaderProgramManager;
    private final TextureAtlasManager textureAtlasManager;

    @HiddenNode
    public final FontManager fontManager = new FontManager();

    // Registries
    public final EntityModelRegistry entityModelManager;
    public final EntityRendererRegistry entityRendererManager;
    public final ClientSoundRegistry soundRegistry = new ClientSoundRegistry();

    // Raycast and block breaking
    public Hit hit;

    @ShowInNodeView
    private BlockVec breaking;

    @ShowInNodeView
    private BlockState breakingBlock;

    public @Nullable Hit cursor;

    // Public Flags
    public boolean renderWorld = false;

    // Startup time
    public static final long BOOT_TIMESTAMP = System.currentTimeMillis();

    // Texture Atlases
    public TextureAtlas blocksTextureAtlas;
    public TextureAtlas itemTextureAtlas;

    // Baked Models
    @HiddenNode
    public BakedModelRegistry bakedBlockModels;

    // Advanced Shadows
    private final List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();

    // Window stuff
    @Nullable
    Integer deferredWidth;
    @Nullable
    Integer deferredHeight;

    private int width;
    private int height;

    @ShowInNodeView
    Texture windowTex;

    @ShowInNodeView
    private final GameWindow window;

    // Frames and ticking
    private int currentTps;
    private float tickTime = 0f;
    public float partialTick = 0f;
    public float frameTime = 0f;
    private int ticksPassed = 0;

    double time = System.currentTimeMillis();

    // Debug
    public DebugOverlay debugGui;

    // Flags
    private boolean closingWorld;

    // Disposal queue for deferred disposables, this is disposed next frame
    private final Queue<Disposable> disposalQueue = new ArrayDeque<>();

    // Server
    public IntegratedServer integratedServer;

    // Game activity
    private GameActivity activity = null;
    private GameActivity oldActivity = null;

    // Loading overlay
    LoadingOverlay loadingOverlay;

    // Environment
    private final Environment defaultEnv = new Environment();
    private boolean autoScale;
    private boolean disposed;

    // Temporaries
    private final Color tmpColor = new Color();

    // Player view
    private PlayerView playerView = PlayerView.FIRST_PERSON;

    // Touch input variables
    public Vector2[] touchPosStartScl = new Vector2[Gdx.input.getMaxPointers()];
    public Vector2[] touchPosStart = new Vector2[Gdx.input.getMaxPointers()];
    public Vector2[] touchMovedScl = new Vector2[Gdx.input.getMaxPointers()];
    public Vector2[] touchMoved = new Vector2[Gdx.input.getMaxPointers()];
    private boolean shuttingDown = false;
    private GridPoint2 offset = new GridPoint2(0, 0);
    private final GameInsets insets = new GameInsets(0, 0, 0, 0);
    private boolean hovered = false;
    private int clicks;
    private long lastPress;
    private RenderBufferSource renderBuffers = new RenderBufferSource();

    {
        for (int i = 0; i < Gdx.input.getMaxPointers(); i++) {
            touchPosStartScl[i] = new Vector2();
            touchPosStart[i] = new Vector2();
            touchMovedScl[i] = new Vector2();
            touchMoved[i] = new Vector2();
        }
    }

    // Misc
    GameEnvironment gameEnv;

    boolean loading = true;

    final String[] argv;
    private Vec2i oldMode;
    private int oldSelected;
    private boolean wasClicking;
    private final Queue<Runnable> serverTickQueue = new ArrayDeque<>();

    private final QuantumClientLoader loader = new QuantumClientLoader();

    /**
     * Initializer for the Quantum Voxel Client.
     * It does all preparations for the game to start.
     * This is called by {@link Main#create()}.
     * <p>
     * NOTE: This method should not be called.
     *
     * @param argv the arguments to pass to the game
     * @author <a href="https://github.com/XyperCode">Qubilux</a>
     * @since <i>Always :smirk:</i>
     */
    QuantumClient(String[] argv) {
        super(QuantumClient.PROFILER);

        this.mainCat.add("Client", this);

        ModApi.init();

        // Disable shader pedantic mode
        ShaderProgram.pedantic = false;

        // Add a shutdown hook to gracefully shut down the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.shutdown();

            QuantumClient.LOGGER.info("Shutting down game!");
            QuantumClient.instance = null;
        }));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        }));
        // Log the booting of the game
        QuantumClient.LOGGER.info("Booting game!");
        QuantumClient.instance = this;

        FabricLoader.getInstance().invokeEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);
        FabricLoader.getInstance().invokeEntrypoints("client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);

        if (this.localData.username != null) {
            this.user = new User(this.localData.username);
        }

        // Initialize the unifont and font
        this.font = new GameFont(new BitmapFont(resource(id("font/luna_pixel.fnt")), false), Font.DistanceFieldType.STANDARD, 0, -13, 0, -20, true);
        this.font.useIntegerPositions(true);
        this.font.setBoldStrength(0.33f);
        this.font.lineHeight = 7f;

        KnownFonts.addEmoji(font);

        this.unifont = new GameFont(new BitmapFont(resource(id("unifont/unifont.fnt")), false), Font.DistanceFieldType.STANDARD, 0, -14, 0, -28, true);
        this.unifont.useIntegerPositions(true);
        this.unifont.setBoldStrength(0.33f);
        this.unifont.scale(0.5f, 0.5f);

        KnownFonts.addEmoji(unifont);

        // Initialize the game window
        this.window = GamePlatform.get().createWindow();

        // Initialize the inspection root
        this.inspection = deferDispose(new InspectionRoot<>(this));

        // Initialize the resource manager, texture manager, and resource loader
        this.resourceManager = new ResourceManager("assets");

        // Initialize shader provider and shader program manager. These should be initialized after the resource manager.
        this.shaderProviderManager = new ShaderProviderManager();
        this.shaderProgramManager = new ShaderProgramManager();

        // Locate resources by finding the ".quantum-resources" file using Class.getResource() and using the parent file.
        GamePlatform.get().locateResources();

        // Set the language bootstrap
        LanguageBootstrap.bootstrap.set(Language::translate);

        // Initialize texture, texture atlas, cubemap, and material managers
        this.textureManager = new TextureManager(this.resourceManager);
        this.cubemapManager = new CubemapManager(this.resourceManager);
        this.materialManager = new MaterialManager(this.resourceManager, this.textureManager, this.cubemapManager);
        this.textureAtlasManager = new TextureAtlasManager(this);

        // Initialize the game camera
        this.camera = new GameCamera(67, this.getWidth(), this.getHeight());
        this.camera.near = 0.1f;
        this.camera.far = 2;

        // Load the configuration
        ModLoadingContext.withinContext(GamePlatform.get().getMod(CommonConstants.NAMESPACE).orElseThrow(), () -> {
            this.newConfig = new ClientConfig();
            this.newConfig.event.subscribe(this::onReloadConfig);
            this.newConfig.load();
        });

        // Register auto fillers for debugging
        DebugRegistration.registerAutoFillers();

        // Set the command line arguments
        this.argv = argv;

        // Set the flag for the development world
        this.devWorld = QuantumClient.arguments.getFlags().contains("devWorld");

        // Create a new user
        RpcHandler.enable();

        // Initialize ImGui if necessary
        this.imGui = !PlatformOS.isAndroid && !PlatformOS.isIos;
        if (this.imGui) {
            GamePlatform.get().preInitImGui();
            GamePlatform.get().setupImGui();
        }

        // Initialize the model loader
        G3dModelLoader modelLoader = new G3dModelLoader(new JsonReader());
        this.entityModelManager = new EntityModelRegistry(modelLoader, this);
        this.entityRendererManager = new EntityRendererRegistry(this.entityModelManager);
        this.modelLoader = modelLoader;

        // Initialize the render pipeline
        this.pipeline = deferDispose(new RenderPipeline(new MainRenderNode(), this.camera)
                .node(new CollectNode())
                .node(new WorldNode())
                .node(new ForegroundNode())
                .node(new BackgroundNode()));

        // Create a white pixel for the shape drawer
        Pixmap pixmap = deferDispose(new Pixmap(1, 1, Pixmap.Format.RGBA8888));
        pixmap.setColor(1F, 1F, 1F, 1F);
        pixmap.drawPixel(0, 0);
        TextureRegion white = new TextureRegion(new Texture(pixmap));

        // Initialize the sprite batch, shape drawer, and renderer
        this.spriteBatch = deferDispose(new SpriteBatch());

        // Set the projection matrix for the spriteBatch
        this.spriteBatch.getProjectionMatrix().setToOrtho(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1000000);

        this.shapes = new ShapeDrawer(this.spriteBatch, white);
        this.renderer = new Renderer(this.shapes);

        // Initialize ModelBatch with GameShaderProvider
        this.modelBatch = deferDispose(new ModelBatch(new SceneShaders(
                resource(id("shaders/scene.vert")),
                resource(id("shaders/scene.frag")),
                resource(id("shaders/scene.geom")))));

        // Initialize GameRenderer
        this.gameRenderer = new GameRenderer(this, this.modelBatch, this.pipeline);

        // Set up modifications
        this.setupMods();

        // Load textures
        this.ultreonBgTex = new Texture("assets/quantum/textures/gui/loading_overlay_bg.png");
        this.ultreonLogoTex = new Texture("assets/quantum/logo.png");
        this.libGDXLogoTex = new Texture("assets/quantum/libgdx_logo.png");
        this.logoRevealSound = Gdx.audio.newSound(Gdx.files.internal("assets/quantum/sounds/logo_reveal.ogg"));

        // Initialize Resizer
        this.resizer = new Resizer(this.ultreonLogoTex.getWidth(), this.ultreonLogoTex.getHeight());

        // Create cursor textures
        this.normalCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("assets/quantum/textures/cursors/normal.png")), 0, 0);
        this.clickCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("assets/quantum/textures/cursors/click.png")), 0, 0);

        // Set current language
        LanguageManager.setCurrentLanguage(Locale.of("en", "us"));

        this.closeButton = new ControlButton(ControlIcon.Close);
        this.maximizeButton = new ControlButton(ControlIcon.Maximize);
        this.minimizeButton = new ControlButton(ControlIcon.Minimize);

        // Create inspection nodes for libGdx and graphics
        if (DebugFlags.INSPECTION_ENABLED.isEnabled()) {
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
     * Returns a shader file handle for the given namespace ID.
     *
     * @param id the namespace ID of the shader.
     * @return the shader file handle.
     */
    public static @NotNull FileHandle shader(NamespaceID id) {
        if (GamePlatform.get().isAngleGLES()) {
            return resource(new NamespaceID(id.getDomain(), "shaders/angle/" + id.getPath()));
        }

        return resource(new NamespaceID(id.getDomain(), "shaders/" + id.getPath()));
    }

    /**
     * Returns an instance of IClipboard.
     * If the game platform is macOS, a {@link NullClipboard} is returned.
     * Otherwise, a {@link DefaultClipboard} is returned.
     *
     * @return An instance of IClipboard.
     */
    private IClipboard createClipboard() {
        // Check if the game platform is macOS
        if (GamePlatform.get().isMacOSX()) {
            // If it is, return a NullClipboard
            return new NullClipboard();
        }

        // Otherwise, return a DefaultClipboard
        return new DefaultClipboard();
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

    /**
     * This method is used to reload the config.
     */
    void onReloadConfig() {
        if (ClientConfig.fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        String[] split = ClientConfig.language.getPath().split("_");
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

        // Cancel vibration if it is not enabled
        if (!ClientConfig.vibration) {
            GameInput.cancelVibration();
        }

        QuantumClient.invoke(() -> {
            // Set vsync
            boolean enableVsync = ClientConfig.enableVsync;
            Gdx.graphics.setVSync(enableVsync);

            // Set fps limit
            int fpsLimit = ClientConfig.fpsLimit;
            if (fpsLimit >= 240) QuantumClient.setFpsLimit(0);
            else QuantumClient.setFpsLimit(fpsLimit < 10 ? 60 : fpsLimit);
        });
    }

    /**
     * Sets the fps limit for the game.
     *
     * @param limit the fps limit.
     */
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
        return Gdx.files.absolute(GamePlatform.get().getGameDir().toAbsolutePath().toString()).child(path);
    }

    /**
     * Makes a screenshot of the game.
     *
     * @return the screenshot.
     */
    public CompletableFuture<Screenshot> screenshot() {
        this.triggerScreenshot = true;
        this.screenshotFuture = new CompletableFuture<>();
        return this.screenshotFuture;
    }

    /**
     * Makes a screenshot of the game.
     *
     * @param worldOnly whether to only screenshot the world.
     * @return the screenshot.
     */
    public CompletableFuture<Screenshot> screenshot(boolean worldOnly) {
        this.screenshotWorldOnly = worldOnly;
        this.triggerScreenshot = true;
        this.screenshotFuture = new CompletableFuture<>();
        return this.screenshotFuture;
    }

    /**
     * Logs a debug message.
     */
    public static void logDebug() {
        if (QuantumClient.isPackaged()) QuantumClient.LOGGER.warn("Running in the JPackage environment.");
        QuantumClient.LOGGER.debug("Java Version: {}", System.getProperty("java.version"));
        QuantumClient.LOGGER.debug("Java Vendor: {}", System.getProperty("java.vendor"));
        QuantumClient.LOGGER.debug("Operating System: {} {} {}", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
    }

    /**
     * Gets the icons for the game.
     *
     * @return the icons for the game.
     */
    public static String[] getIcons() {
        int[] sizes = QuantumClient.SIZES;
        if (!isMac) {
            sizes = new int[]{1024};
        }
        String[] icons = new String[sizes.length];
        for (int i = 0, sizesLength = sizes.length; i < sizesLength; i++) {
            if (isMac) {
                icons[i] = "icons/mac.png";
            } else {
                icons[i] = "icons/icon.png";
            }
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

    /**
     * Sets the automatic scale for the game.
     *
     * @param b whether to set the automatic scale.
     */
    public void setAutomaticScale(boolean b) {
        this.autoScale = b;
        this.guiScale = this.calcMaxGuiScale();
        this.resize(this.width, this.height);
    }

    /**
     * Starts the development world.
     */
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
        if (GamePlatform.get().isDesktop()) {
            PyLoader.getInstance().initMods();
            JsLoader.getInstance().initMods();
        }

        // Set mod icon overrides.
        ModIconOverrideRegistry.set("quantum", QuantumClient.id("icon.png"));
        ModIconOverrideRegistry.set("gdx", new NamespaceID("gdx", "icon.png"));

        // Invoke entry points for initialization.
        GamePlatform loader = GamePlatform.get();
        loader.invokeEntrypoint(ModInit.ENTRYPOINT_KEY, ModInit.class, ModInit::onInitialize);

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
        if (isOnRenderThread()) {
            try {
                return func.call();
            } catch (Exception e) {
                throw new RejectedExecutionException("Failed to execute task", e);
            }
        }
        try {
            return QuantumClient.instance.submit(func).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RejectedExecutionException("Failed to execute task", e);
        }
    }

    /**
     * Executes the specified {@link Runnable} function on the client thread and waits until it completes.
     * This method is designed to be invoked from a different thread to ensure that the function is executed on the
     * client thread.
     *
     * @param func the {@link Runnable} function to be executed on the QuantumClient thread
     */
    public static void invokeAndWait(Runnable func) {
        if (isOnRenderThread()) {
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
    public static @NotNull FileHandle resource(NamespaceID id) {
        return Gdx.files.internal("assets/" + id.getDomain() + "/" + id.getPath());
    }

    /**
     * Checks whether the current thread is the main thread.
     *
     * @return true if the current thread is the main thread, false otherwise.
     */
    public static boolean isOnRenderThread() {
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
        if (disposables.contains(disposable)) return disposable;

        if (disposed) {
            QuantumClient.LOGGER.warn("QuantumClient already disposed, immediately disposing {}", disposable.getClass().getName());
            disposable.dispose();
            return disposable;
        }

        disposables.add(disposable);
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
     * @deprecated Use {@link NamespaceID#of(String)} instead
     */
    @Deprecated
    public static NamespaceID id(String path) {
        return new NamespaceID(CommonConstants.NAMESPACE, path);
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
        if (!isOnRenderThread()) {
            @Nullable Screen finalNext = next;
            return invokeAndWait(() -> this.showScreen(finalNext));
        }

        if (this.screen != null) this.remove(this.screen);
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

        if (next == null) {
            Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
            this.setWindowTitle(TextObject.literal("Playing in a world!"));
            return cur == null || this.closeScreen(cur);
        }

        // Call open event.
        var openResult = ScreenEvents.OPEN.factory().onOpenScreen(next);
        if (openResult.isCanceled())
            return false;

        if (openResult.isInterrupted())
            next = openResult.getValue();

        if (cur != null && this.closeScreen(next, cur))
            return false; // Close was canceled
        if (next == null) {
            Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
            this.setWindowTitle(TextObject.literal("Playing in a world!"));
            return false; // The next screen is null, canceling.
        }

        if (cur != null) cur.mouseExit();
        this.screen = next;

        GridPoint2 mouse = getMousePos();
        this.screen.mouseEnter(mouse.x, mouse.y);
        this.screen.init(this.getScaledWidth(), this.getScaledHeight());
        this.add("Screen", next);
        KeyAndMouseInput.setCursorCaught(false);

        this.setWindowTitle(this.screen.getTitle());

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
        KeyAndMouseInput.setCursorCaught(true);

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

            try (var ignored = PROFILER.start("render")) {
                this.doRender(deltaTime);
            }
            this.renderer.actuallyEnd();
        } catch (OutOfMemoryError e) {
            System.gc(); // try to free up some memory before handling out of memory.
            try {
                if (this.integratedServer != null) {
                    this.integratedServer.shutdownNow();
                    this.remove(integratedServer);
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

    /**
     * Renders the game.
     *
     * @param deltaTime the delta time.
     */
    private void doRender(float deltaTime) {
        // If the width or height is 0, set them to the current width and height.
        if (this.width == 0 || this.height == 0) {
            this.width = Gdx.graphics.getWidth();
            this.height = Gdx.graphics.getHeight();
        }

        // If the deferred width or height is not null and the width or height is not equal to the deferred width or height, set the width and height to the deferred width and height.
        if (deferredWidth != null && deferredHeight != null && (width != deferredWidth || height != deferredHeight)) {
            width = deferredWidth;
            height = deferredHeight;

            this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // If the screenshot world only flag is true, clear the screen, render the world, and set the capture screenshot and trigger screenshot flags to false.
        // Then, grab a screenshot, complete the screenshot future, clear the screen, set the screenshot world only flag to false, and set the capture screenshot and trigger screenshot flags to false.
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

        // If the trigger screenshot flag is true, prepare the screenshot.
        if (this.triggerScreenshot) this.prepareScreenshot();

        // If the game bounds are not set, set them to the draw offset and the width and height.
        try (var ignored0 = PROFILER.start("renderGame")) {
            gameBounds.set(getDrawOffset().x, getDrawOffset().y, width, height);

            // If the scissor stack is pushed, render the game.
            if (ScissorStack.pushScissors(gameBounds)) {
                try {
                    this.renderGame(renderer, deltaTime);
                } finally {
                    ScissorStack.popScissors();
                }
            }
        }

        // If the capture screenshot flag is true and the screenshot world only flag is false, handle the screenshot.
        if (this.captureScreenshot && !this.screenshotWorldOnly) this.handleScreenshot();

        // If the screenshot world only flag is false and the screenshot flash time is greater than the current time minus 200, draw the screenshot flash.
        if (!this.screenshotWorldOnly && this.screenshotFlashTime > System.currentTimeMillis() - 200) {
            this.renderer.begin();
            this.shapes.filledRectangle(0, 0, this.width, this.height, this.tmpColor.set(1, 1, 1, 1 - (System.currentTimeMillis() - this.screenshotFlashTime) / 200f));
            this.renderer.end();
        }

        // If the custom border is shown and the loading flag is false, draw the custom border.
        if (this.isCustomBorderShown() && !loading) this.drawCustomBorder(renderer);

        // If the ImGui flag is true, render the ImGui.
        if (this.imGui) {
            GamePlatform.get().renderImGui();
        }

        // If the window is dragging, get the texture for the cursor, and draw it.
        // This is used to not have the cursor look disconnected from the window.
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

    /**
     * Prepares the screenshot.
     */
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

    /**
     * Draws the custom border.
     *
     * @param renderer the renderer.
     */
    private void drawCustomBorder(Renderer renderer) {
        this.renderer.begin();
        renderer.pushMatrix();
        renderer.scale(2, 2);
        this.renderWindow(renderer, this.getWidth() / 2 + 36, this.getHeight() / 2 + 44);
        renderer.popMatrix();
        this.renderer.end();
    }

    /**
     * Handles the screenshot.
     *
     * @param renderer the renderer.
     */
    private void handleScreenshot() {
        this.captureScreenshot = false;

        this.saveScreenshot();
        this.fbo.end();
        this.fbo.dispose();

        this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Renders the game.
     *
     * @param renderer the renderer.
     * @param deltaTime the delta time.
     * @return whether the game was rendered.
     */
    private boolean renderGame(Renderer renderer, float deltaTime) {
        if (Gdx.graphics.getFrameId() == 2) {
            this.firstRender();
        }

        this.updateActivity();

        this.pollAll();

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

        Gdx.gl.glEnable(GL_BLEND);
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glFlush();
        ScreenUtils.clear(0, 0, 0, 0, true);
        Gdx.gl.glFlush();

        this.renderMain(renderer, deltaTime);
        return false;
    }

    /**
     * Renders the main game.
     *
     * @param renderer the renderer.
     * @param deltaTime the delta time.
     */
    private void renderMain(Renderer renderer, float deltaTime) {
        Player player = this.player;
        if (player == null) {
            this.hit = null;
        } else {
            QuantumClient.PROFILER.section("playerRayCast", () -> this.hit = player.rayCast());
        }

        renderer.begin();
        GridPoint2 drawOffset = this.getDrawOffset();
        if (!GamePlatform.get().hasBackPanelRemoved()) {
            renderer.fill(drawOffset.x, drawOffset.y, (int) (this.gameBounds.getWidth() * getGuiScale()) - drawOffset.x * 2, (int) (this.gameBounds.getHeight() * getGuiScale()) - drawOffset.y * 2, Color.BLACK);
        }
        renderer.end();

        if (this.controllerInput != null && this.keyAndMouseInput != null) {
            try (var ignored = QuantumClient.PROFILER.start("input")) {
                this.controllerInput.update();
                this.keyAndMouseInput.update();
                if (this.touchInput != null) this.touchInput.update();
            }
        }

        ControllerContext.register(InGameControllerContext.INSTANCE, client -> client.screen == null);
        ControllerContext.register(MenuControllerContext.INSTANCE, client -> client.screen != null);

        Screen screen = this.screen;
        if (screen != null && KeyAndMouseInput.isPressingAnyButton() && !this.wasClicking) {
            this.setCursor(this.clickCursor);
            this.wasClicking = true;
        } else if (screen != null && !KeyAndMouseInput.isPressingAnyButton() && this.wasClicking) {
            this.setCursor(this.normalCursor);
            this.wasClicking = false;
        }

        RenderEvents.PRE_RENDER_GAME.factory().onRenderGame(gameRenderer, renderer, deltaTime);
        this.gameRenderer.render(renderer, deltaTime);
        RenderEvents.POST_RENDER_GAME.factory().onRenderGame(gameRenderer, renderer, deltaTime);
    }

    /**
     * Sets the cursor.
     *
     * @param cursor the cursor.
     */
    private void setCursor(Cursor cursor) {
        this.cursor0 = cursor;
        if (this.window.isDragging()) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        } else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            Gdx.graphics.setCursor(this.cursor0);
        }
    }

    /**
     * Renders the loading overlay.
     *
     * @param renderer the renderer.
     * @param deltaTime the delta time.
     * @param loading the loading overlay.
     */
    private void renderLoadingOverlay(Renderer renderer, float deltaTime, LoadingOverlay loading) {
        try (var ignored = QuantumClient.PROFILER.start("loading")) {
            renderer.begin();
            renderer.pushMatrix();
            renderer.translate(this.getDrawOffset().x, this.getDrawOffset().y);
            renderer.scale(this.getGuiScale(), this.getGuiScale());
            if (!GamePlatform.get().hasBackPanelRemoved())
                renderer.clearColor(0, 0, 0, 1);
            else {
                renderer.clearColor(0, 0, 0, 0);
            }
            loading.render(renderer, deltaTime);
            renderer.popMatrix();
            renderer.end();
        }
    }

    /**
     * Starts the loading of the game.
     */
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
                if (crashes.isEmpty()) {
                    screen = new DevPreviewScreen(user == null ? new UsernameScreen() : null);
                } else {
                    screen = new CrashScreen(crashes);
                }

                screen.init(getScaledWidth(), getScaledHeight());
                loadingOverlay = null;
            }).exceptionally(throwable -> {
                crash(throwable);
                return null;
            });
        });
    }

    /**
     * Renders the LibGDX splash.
     *
     * @param renderer the renderer.
     */
    private void renderLibGDXSplash(Renderer renderer) {
        try (var ignored = QuantumClient.PROFILER.start("libGdxSplash")) {
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
        }
    }

    /**
     * Renders the Ultreon splash.
     *
     * @param renderer the renderer.
     */
    private void renderUltreonSplash(Renderer renderer) {
        try (var ignored = QuantumClient.PROFILER.start("ultreonSplash")) {
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
        }
    }

    /**
     * Retrieves the game version of the Quantum Voxel mod.
     *
     * @return The game version as a {@code String}.
     */
    public static String getGameVersion() {
        return GamePlatform.get().getMod("quantum").orElseThrow().getVersion();
    }

    /**
     * Tries to tick the client.
     */
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

    /**
     * Updates the activity.
     */
    private void updateActivity() {
        if (this.activity != this.oldActivity) {
            this.oldActivity = this.activity;

            RpcHandler.newActivity(this.activity);
        }
    }

    /**
     * Saves a screenshot.
     */
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

    /**
     * Renders the first frame.
     */
    private void firstRender() {
//        if (PlatformOS.isWindows) {
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

    /**
     * Renders the window.
     *
     * @param renderer the renderer.
     * @param width the width.
     * @param height the height.
     */
    private void renderWindow(Renderer renderer, int width, int height) {
        boolean maximized = window.isMaximized();
        int winXOff = maximized ? 18 : 0;
        int winHOff = maximized ? 22 : 0;
        renderer.draw9PatchTexture(new NamespaceID("textures/gui/window.png"), -winXOff, 0, width + winXOff * 2, height + winHOff, 0, 0, 18, 22, 256, 256);
        renderer.textCenter("[*]" + window.getTitle(), width / 2, 5);

        this.closeButton.setX(width - 17 - winXOff * 2);
        this.closeButton.setY(3);

        this.maximizeButton.setX(width - 35 - winXOff * 2);
        this.maximizeButton.setY(3);

        this.minimizeButton.setX(width - 53 - winXOff * 2);
        this.minimizeButton.setY(3);

        this.closeButton.render(renderer, 0);
        this.maximizeButton.render(renderer, 0);
        this.minimizeButton.render(renderer, 0);

        this.window.update();
    }

    /**
     * Gets the window offset.
     *
     * @return the window offset.
     */
    private GridPoint2 getWindowOffset() {
        if (window.isMaximized()) return MAXIMIZE_OFF;
        return ZERO;
    }

    /**
     * Crashes the game.
     *
     * @param throwable the throwable.
     */
    public static void crash(Throwable throwable) {
        QuantumClient.LOGGER.error("Game crash triggered:", throwable);
        var crashLog = new CrashLog("An unexpected error occurred", throwable);
        QuantumClient.crash(crashLog);
    }

    /**
     * Crashes the game.
     *
     * @param crashLog the crash log.
     */
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

    /**
     * Fills the game info.
     *
     * @param crashLog the crash log.
     */
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

    /**
     * Crashes the game.
     *
     * @param crash the crash.
     */
    private static void crash(ApplicationCrash crash) {
        if (crashing) {
            LOGGER.error("Double crash detected, ignoring.");
            return;
        }

        crashing = true;
        try {
            var crashLog = crash.getCrashLog();
            CrashHandler.handleCrash(crashLog);
            String string = crashLog.toString();
            LOGGER.error("Dumping crash report...\n{}", string);

            if (instance != null) instance.shutdown();
            System.exit(1);
        } catch (Exception | OutOfMemoryError t) {
            QuantumClient.LOGGER.error(QuantumClient.FATAL_ERROR_MSG, t);

            if (instance != null) instance.shutdown();
            System.exit(1);
        }
    }

    /**
     * Cleans up a disposable.
     *
     * @param disposable the disposable.
     */
    private static void cleanUp(@Nullable Disposable disposable) {
        if (disposable == null) return;

        Debugger.log(Debugger.Type.CLEAN_UP, "Cleaning up " + disposable.getClass().getName());

        try {
            disposable.dispose();
        } catch (Exception throwable) {
            Debugger.log("Failed to dispose " + disposable.getClass().getName(), throwable);
        }
    }

    /**
     * Cleans up a shutdownable.
     *
     * @param disposable the disposable.
     */
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

    /**
     * Cleans up an executor service.
     *
     * @param disposable the disposable.
     */
    private static void cleanUp(@Nullable ExecutorService disposable) {
        if (disposable == null) return;

        Debugger.log(Debugger.Type.CLEAN_UP, "Cleaning up " + disposable.getClass().getName());

        try {
            disposable.shutdownNow();
        } catch (Exception throwable) {
            Debugger.log("Failed to shut down " + disposable.getClass().getName(), throwable);
        }
    }

    /**
     * Cleans up an auto closeable.
     *
     * @param disposable the disposable.
     */
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
            this.cursor = this.world.rayCast(new Ray(player.getPosition(this.partialTick).add(0, player.getEyeHeight(), 0), player.getLookVector()), player, player.getReach(), CommonConstants.VEC3D_0_C);
        }

        // Update connection tick
        IConnection<ClientPacketHandler, ServerPacketHandler> connection = this.connection;
        if (connection != null) {
            connection.tick();
            // Update client connection tick
            this.connection.tick();
        }

        if (this.controllerInput != null) this.controllerInput.tick();
        if (this.keyAndMouseInput != null) this.keyAndMouseInput.tick();
        if (this.touchInput != null) this.touchInput.tick();

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
        BlockVec breaking = this.breaking != null ? new BlockVec(this.breaking) : null;
        if (this.world != null && breaking != null) {
            Hit hit = this.hit;

            if (hit instanceof BlockHit blockHitResult) {
                this.handleBlockBreaking(breaking, blockHitResult);
            }
        }

        // Update camera based on player position
        if (player != null) {
            this.camera.update(player);
        }

        Screen screen1 = screen;
        if (screen1 != null) {
            screen1.tick();
        }

        // Execute post-game tick event
        ClientTickEvents.POST_GAME_TICK.factory().onGameTick(this);
    }

    /**
     * Handles the block breaking.
     *
     * @param breaking the breaking.
     * @param hitResult the hit result.
     */
    private void handleBlockBreaking(BlockVec breaking, BlockHit hitResult) {
        @Nullable ClientWorldAccess world = this.world;
        if (world == null) return;
        if (!hitResult.getBlockVec().equals(breaking.asBlockVec()) || !hitResult.getBlockMeta().equals(this.breakingBlock) || this.player == null) {
            this.resetBreaking(hitResult);
        } else {
            float efficiency = 1.0F;
            ItemStack stack = this.player.getSelectedItem();
            Item item = stack.getItem();
            if (item instanceof ToolItem toolItem && this.breakingBlock.getEffectiveTool() == toolItem.getToolType()) {
                efficiency = toolItem.getEfficiency();
            }

            BreakResult breakResult = world.continueBreaking(breaking, 1.0F / (Math.max((this.breakingBlock.getHardness() / efficiency) * QuantumServer.TPS, 1) + 1), this.player);
            if (breakResult == BreakResult.FAILED) {
                this.resetBreaking();
            } else if (breakResult == BreakResult.BROKEN) {
                this.breaking = null;
                this.breakingBlock = null;
            } else {
                if (this.oldSelected != this.player.selected) {
                    this.resetBreaking();
                }
                this.oldSelected = this.player.selected;
            }
        }
    }

    /**
     * Resets the breaking.
     *
     * @param hitResult the hit result.
     */
    private void resetBreaking(BlockHit hitResult) {
        LocalPlayer player = this.player;

        if (this.world == null) return;
        if (this.breaking == null) return;
        if (player == null) return;

        this.world.stopBreaking(new BlockVec(this.breaking), player);
        BlockState block = hitResult.getBlockMeta();

        if (block == null || block.isAir()) {
            this.breaking = null;
            this.breakingBlock = null;
        } else {
            this.breaking = hitResult.getBlockVec();
            this.breakingBlock = block;
            this.world.startBreaking(new BlockVec(hitResult.getBlockVec()), player);
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
        if (this.spriteBatch == null
            || this.renderer == null) return;
        if (width == 0 && height == 0) {
            return;
        }
        if (!QuantumClient.isOnRenderThread()) {
            QuantumClient.invokeAndWait(() -> this.resize(width, height));
            return;
        }

        // Set the projection matrix for the spriteBatch
        this.spriteBatch.getProjectionMatrix().setToOrtho(0, width, height, 0, 0, 1000000);

        // Update the deferred width and height values
        this.deferredWidth = width;
        this.deferredHeight = height;

        this.width = width;
        this.height = height;

        // Resize the renderer
        this.renderer.resize(width, height);

        this.autoScale = ClientConfig.guiScale == 0;

        // Auto-scale the GUI if enabled
        if (this.autoScale) {
            this.guiScale = this.calcMaxGuiScale();
        } else {
            this.guiScale = Math.clamp(ClientConfig.guiScale, 1, calcMaxGuiScale());
        }

        // Update the camera if present
        if (this.camera != null) {
            this.camera.viewportWidth = getWidth();
            this.camera.viewportHeight = getHeight();
            this.camera.update();
        }

        // Resize the item renderer
        if (this.itemRenderer != null) {
            this.itemRenderer.resize(getWidth(), getHeight());
        }

        // Resize the game renderer
        this.gameRenderer.resize(getWidth(), getHeight());

        // Resize the current screen
        var cur = this.screen;
        if (cur != null) {
            float w = getWidth() / this.getGuiScale();
            float h = getHeight() / this.getGuiScale();
            cur.resize(ceil(w), ceil(h));
        }

        OverlayManager.resize(ceil(getWidth() / this.getGuiScale()), ceil(height / this.getGuiScale()));
    }

    /**
     * Disposes of the client.
     */
    @Override
    public void dispose() {
        if (!QuantumClient.isOnRenderThread()) {
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

                mainCat.pop(this);

                // Clear scenes
                backgroundCat.clear();
                worldCat.clear();
                mainCat.clear();

                // Dispose Models
                ModelManager.INSTANCE.dispose();
                BakedModelRegistry bakedBlockModels1 = this.bakedBlockModels;
                if (bakedBlockModels1 != null) QuantumClient.cleanUp(bakedBlockModels1.atlas());
                if (bakedBlockModels1 != null) QuantumClient.cleanUp(bakedBlockModels1);
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

                int secondsPassed = 0;
                LongSet threadIds = new LongArraySet();
                while (true) {
                    Set<Thread> threads = Thread.getAllStackTraces().keySet().stream().filter(t -> !t.isDaemon() && !t.isInterrupted() && t.threadId() != Thread.currentThread().threadId()).collect(Collectors.toSet());
                    for (Thread t : threads) {
                        if (threadIds.add(t.threadId())) LOGGER.debug("{}: {}", t.getName(), t.getState());
                        t.interrupt();
                    }

                    if (threads.isEmpty()) {
                        break;
                    } else {
                        LOGGER.info("Waiting for {} threads to finish...", threads.size());
                        Thread.sleep(1000);

                        if (secondsPassed++ > 10) {
                            LOGGER.warn("Still waiting for {} threads to finish. Terminating...", threads.size());
                            Runtime.getRuntime().halt(1);
                        }
                    }
                }
            } catch (Exception t) {
                QuantumClient.crash(t);
            }
        }
    }

    /**
     * Checks if the game is in dev mode.
     *
     * @return whether the game is in dev mode.
     */
    public boolean isDevMode() {
        return this.isDevMode;
    }

    /**
     * Gets the width of the game.
     *
     * @return the width of the game.
     */ 
    public int getWidth() {
        GameInsets insets = GamePlatform.get().getInsets();
        return GamePlatform.get().isShowingImGui() ? insets.width : Gdx.graphics.getWidth();
    }

    /**
     * Gets the height of the game.
     *
     * @return the height of the game.
     */ 
    public int getHeight() {
        GameInsets insets = GamePlatform.get().getInsets();
        return GamePlatform.get().isShowingImGui() ? insets.height : Gdx.graphics.getHeight();
    }

    /**
     * Gets the texture manager.
     *
     * @return the texture manager.
     */ 
    public TextureManager getTextureManager() {
        return this.textureManager;
    }

    /**
     * Starts the world.
     *
     * @param storage the storage.
     */
    public void startWorld(WorldStorage storage) {
        this.showScreen(new WorldLoadScreen(storage));
    }

    /**
     * Starts the world.
     *
     * @param path the path.
     */
    public void startWorld(Path path) {
        this.showScreen(new WorldLoadScreen(new WorldStorage(path)));
    }

    /**
     * Gets the GUI scale.
     *
     * @return the GUI scale.
     */
    public float getGuiScale() {
        return this.guiScale;
    }

    /**
     * Gets the scaled width.
     *
     * @return the scaled width.
     */
    public int getScaledWidth() {
        return ceil(getWidth() / this.getGuiScale());
    }

    /**
     * Gets the scaled height.
     *
     * @return the scaled height.
     */
    public int getScaledHeight() {
        return ceil(getHeight() / this.getGuiScale());
    }

    /**
     * Exits the world to the title screen.
     */
    public void exitWorldToTitle() {
        this.exitWorldAndThen(() -> this.showScreen(new TitleScreen()));
    }

    /**
     * Exits the world and then runs a runnable.
     *
     * @param afterExit the runnable.
     */
    public void exitWorldAndThen(Runnable afterExit) {
        this.closingWorld = true;
        this.renderWorld = false;

        final @Nullable TerrainRenderer worldRenderer = this.worldRenderer;
        this.showScreen(new MessageScreen(TextObject.translation("quantum.screen.message.saving_world"))); // "Saving world..."

        CompletableFuture.runAsync(() -> {
            try {
                this.connection.close();
            } catch (IOException e) {
                QuantumClient.crash(e);
                return;
            }

            IntegratedServer server = integratedServer;
            if (server != null)
                this.remove(integratedServer);

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

    /**
     * Checks if the world is closing.
     *
     * @return whether the world is closing.
     */
    public boolean isClosingWorld() {
        return this.closingWorld;
    }

    /**
     * Schedules a task.
     *
     * @param task the task.
     * @param timeMillis the time in milliseconds.
     * @return the scheduled future.
     */
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

    /**
     * Schedules a task.
     *
     * @param task the task.
     * @param time the time.
     * @param unit the unit.
     * @return the scheduled future.
     */
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

    /**
     * Gets the resource manager.
     *
     * @return the resource manager.
     */
    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    /**
     * Plays a sound.
     *
     * @param event the event.
     */
    public void playSound(ClientSound event) {
        event.getSound().play();
    }

    /**
     * Tries to shutdown the game.
     *
     * @return whether the game was shutdown.
     */
    public boolean tryShutdown() {
        if (WindowEvents.WINDOW_CLOSE_REQUESTED.factory().onWindowCloseRequested(this.window).isCanceled()) {
            return false;
        }

        ClientLifecycleEvents.WINDOW_CLOSED.factory().onWindowClose();

        if (ClientConfig.showClosePrompt && this.screen != null) {
            this.screen.showDialog(new DialogBuilder(this.screen).message(TextObject.literal("Are you sure you want to close the game?")).button(UITranslations.YES, () -> {
                if (this.world != null) {
                    this.exitWorldAndThen(this::shutdown);
                    return;
                }

                this.shutdown();
            }));
            return false;
        }

        if (this.world != null) {
            this.exitWorldAndThen(this::shutdown);
            return false;
        }

        CompletableFuture.runAsync(this::shutdown);

        return false;
    }

    /**
     * Checks if files were dropped.
     *
     * @param files the files.
     * @return whether the files were dropped.
     */
    public boolean filesDropped(String[] files) {
        var currentScreen = this.screen;
        var handles = Arrays.stream(files).map(FileHandle::new).collect(Collectors.toList());

        if (currentScreen != null) {
            return currentScreen.filesDropped(handles);
        }

        return false;
    }

    /**
     * Adds a future.
     *
     * @param future the future.
     */ 
    public void addFuture(CompletableFuture<?> future) {
        this.futures.add(future);
    }

    /**
     * Gets the block model.
     *
     * @param block the block.
     * @return the block model.
     */
    public @NotNull BlockModel getBlockModel(BlockState block) {
        List<Pair<Predicate<BlockState>, BakedCubeModel>> orDefault = this.bakedBlockModels.bakedModels().getOrDefault(block.getBlock(), List.of());

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
        Hit hit = this.hit;
        Player player = this.player;

        if (!(hit instanceof BlockHit blockHitResult)) return;

        // Check for null conditions and return if any are true
        if (this.world == null || player == null) return;

        // Stop and start breaking at the hit position for the player
        this.world.stopBreaking(new BlockVec(blockHitResult.getBlockVec()), player);
        this.world.startBreaking(new BlockVec(blockHitResult.getBlockVec()), player);

        // Update the breaking position and block meta
        this.breaking = blockHitResult.getBlockVec();
        this.breakingBlock = blockHitResult.getBlockMeta();
    }

    /**
     * Starts the process of breaking a block in the game world.
     * If the player is already breaking a block, it stops the current process and starts a new one.
     */
    public void startBreaking() {
        // Get the hit result and player
        Hit hit = this.hit;
        LocalPlayer player = this.player;

        if (!(hit instanceof BlockHit blockHitResult)) {
            this.breaking = null;
            this.breakingBlock = null;
            return;
        }

        // If hit result or world is null, return
        if (this.world == null) {
            return;
        }

        // If the block being hit is already broken, return
        if (this.world.getBreakProgress(new BlockVec(blockHitResult.getBlockVec())) >= 0.0F) {
            return;
        }

        // If the player is null, return
        if (player == null) {
            return;
        }

        if (this.breaking != null) {
            this.world.stopBreaking(new BlockVec(this.breaking), player);
            return;
        }

        // Start breaking the block and update the breaking position and block metadata
        this.world.startBreaking(new BlockVec(blockHitResult.getBlockVec()), player);
        this.breaking = blockHitResult.getBlockVec();
        this.breakingBlock = blockHitResult.getBlockMeta();
    }

    /**
     * Stops the breaking process.
     */
    public void stopBreaking() {
        Hit hit = this.hit;
        LocalPlayer player = this.player;

        if (!(hit instanceof BlockHit blockHitResult)) {
            this.breaking = null;
            this.breakingBlock = null;
            return;
        }

        if (this.world == null || player == null || this.breaking == null) return;

        this.world.stopBreaking(new BlockVec(blockHitResult.getBlockVec()), player);
        this.breaking = null;
        this.breakingBlock = null;
    }

    /**
     * Gets the break progress.
     *
     * @return the break progress.
     */
    public float getBreakProgress() {
        BlockVec breaking = this.breaking;
        @Nullable ClientWorldAccess world = this.world;
        if (breaking == null || world == null) return -1;
        return world.getBreakProgress(new BlockVec(breaking));
    }

    /**
     * Calculates the maximum GUI scale.
     *
     * @return the maximum GUI scale.
     */
    public int calcMaxGuiScale() {
        var windowWidth = getWidth();
        var windowHeight = getHeight();

        if (windowWidth / QuantumClient.MINIMUM_WIDTH < windowHeight / QuantumClient.MINIMUM_HEIGHT) {
            return Math.max(windowWidth / QuantumClient.MINIMUM_WIDTH, 1);
        }

        if (windowHeight / QuantumClient.MINIMUM_HEIGHT < windowWidth / QuantumClient.MINIMUM_WIDTH) {
            return Math.max(windowHeight / QuantumClient.MINIMUM_HEIGHT, 1);
        }

        int min = Math.min(windowWidth / QuantumClient.MINIMUM_WIDTH, windowHeight / QuantumClient.MINIMUM_HEIGHT);
        return Math.max(min, 1);
    }

    /**
     * Checks if the game is playing.
     *
     * @return whether the game is playing.
     */ 
    public boolean isPlaying() {
        return this.world != null && this.screen == null;
    }

    /**
     * Gets the config directory.
     *
     * @return the config directory.
     */
    public static FileHandle getConfigDir() {
        return QuantumClient.instance.configDir;
    }

    /**
     * Gets the draw offset.
     *
     * @return the draw offset.
     */
    public GridPoint2 getDrawOffset() {
        GameInsets insets = GamePlatform.get().getInsets();
        return this.offset.set(0, 0);
    }

    /**
     * Gets the mouse position.
     *
     * @return the mouse position.
     */
    public GridPoint2 getMousePos() {
        GameInsets insets = GamePlatform.get().getInsets();
        GridPoint2 gridPoint2 = GamePlatform.get().isShowingImGui() && !Gdx.input.isCursorCatched() ? this.offset.set(insets.left, insets.top) : this.offset.set(Gdx.input.getX(), Gdx.input.getY());
        return gridPoint2;
    }

    /**
     * Checks if the custom border is shown.
     *
     * @return whether the custom border is shown.
     */
    @ApiStatus.Experimental
    public boolean isCustomBorderShown() {
//        return GamePlatform.get().isDesktop() && !loading;
        return false;
    }

    /**
     * Checks if the game is loading.
     *
     * @return whether the game is loading.
     */ 
    public boolean isLoading() {
        return this.loading;
    }

    /**
     * Gets the game environment.
     *
     * @return the game environment.
     */ 
    public static GameEnvironment getGameEnv() {
        if (QuantumClient.instance == null) return GameEnvironment.UNKNOWN;
        return QuantumClient.instance.gameEnv;
    }

    /**
     * Gets the singleplayer server.
     *
     * @return the singleplayer server.
     */ 
    public IntegratedServer getSingleplayerServer() {
        return this.integratedServer;
    }

    /**
     * Checks if the game is singleplayer.
     *
     * @return whether the game is singleplayer.
     */ 
    public boolean isSinglePlayer() {
        return this.integratedServer != null && !this.integratedServer.isOpenToLan();
    }

    /**
     * Plays a sound.
     *
     * @param soundEvent the sound event.
     * @param volume the volume.
     */ 
    public void playSound(@NotNull SoundEvent soundEvent, float volume) {
        Preconditions.checkNotNull(soundEvent);
        Preconditions.checkArgument(volume >= 0.0F && volume <= 1.0F, "Volume must be between 0.0F and 1.0F");

        Sound sound = this.soundRegistry.getSound(soundEvent.getId());
        if (sound == null) {
            QuantumClient.LOGGER.warn("Unknown sound event: {}", soundEvent.getId());
            return;
        }
        if (soundEvent.isVaryingPitch()) {
            sound.play(volume, 1.0F + MathUtils.random(-0.1F, 0.1F), 1.0F);
        } else {
            sound.play(volume);
        }
    }

    /**
     * Starts the integrated server.
     */
    public void startIntegratedServer() {
        var mem = ClientTcpConnection.connectToLocalServer().unwrap();
        this.connection = mem;
        MemoryConnectionContext.set(mem);

        this.world = new ClientWorld(this, DimensionInfo.OVERWORLD);

        this.integratedServer.start();

        mem.setOtherSide((MemoryConnection<ServerPacketHandler, ClientPacketHandler>) this.integratedServer.getNetworker().getConnections().getFirst());

        // Initialize (memory) connection.
        this.multiplayerData = new MultiplayerData(this);
        this.connection.initiate(new LoginClientPacketHandlerImpl(this.connection), new C2SLoginPacket(this.user.name()));
    }

    /**
     * Connects to a server.
     *
     * @param host the host.
     * @param port the port.
     */
    public void connectToServer(String host, int port) {
        this.world = new ClientWorld(this, DimensionInfo.OVERWORLD);

        this.connection = ClientTcpConnection.connectToServer(host, port).map(Function.identity(), e -> {
            this.showScreen(new DisconnectedScreen("Failed to connect!\n" + e.getMessage(), true));
            return null;
        }).getValueOrNull();

        if (this.connection == null) return;

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

    /**
     * Sets the activity.
     *
     * @param activity the activity.
     */ 
    public void setActivity(GameActivity activity) {
        this.activity = activity;
    }

    /**
     * Sets the full screen.
     *
     * @param fullScreen the full screen.
     */ 
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

    /**
     * Sets the player view.
     *
     * @param playerView the player view.
     */
    public void setPlayerView(PlayerView playerView) {
        this.playerView = playerView;
    }

    /**
     * Gets the player view.
     *
     * @return the player view.
     */
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

        if (guiScale <= 0) throw new IllegalArgumentException("GUI scale must be greater than 0");

        // If autoScale is disabled, set the GUI scale to the provided scale value and resize the GUI
        this.guiScale = guiScale;
        this.resize(this.width, this.height);
    }

    /**
     * Gets the multiplayer data.
     * If the game is not in multiplayer, this will return null.
     *
     * @return the multiplayer data.
     * @see #isSinglePlayer()
     */ 
    public @Nullable MultiplayerData getMultiplayerData() {
        return this.multiplayerData;
    }

    /**
     * Checks if the world is rendering.
     *
     * @return whether the world is rendering.
     */ 
    public boolean isRenderingWorld() {
        return this.world != null && this.worldRenderer != null && this.renderWorld;
    }

    /**
     * Converts the client to a string.
     *
     * @return the string.
     */ 
    @Override
    public @NotNull String toString() {
        return "QuantumClient[" + this.user + "]";
    }

    /**
     * Runs a task in the tick queue.
     *
     * @param func the task.
     */ 
    public void runInTick(Runnable func) {
        this.serverTickQueue.add(func);
    }

    /**
     * Polls the server tick queue.
     */ 
    @ApiStatus.Internal
    public void pollServerTick() {
        Runnable task;
        while ((task = this.serverTickQueue.poll()) != null) {
            task.run();
        }
    }

    /**
     * Gets the user.
     *
     * @return the user.
     */ 
    public User getUser() {
        return this.user;
    }

    /**
     * Gets the environment.
     *
     * @return the environment.
     */ 
    public Environment getEnvironment() {
        if (this.worldRenderer != null) {
            return this.worldRenderer.getEnvironment();
        }
        return this.defaultEnv;
    }

    /**
     * Gets the pipeline.
     *
     * @return the pipeline.
     */ 
    public RenderPipeline getPipeline() {
        return pipeline;
    }

    /**
     * Gets the window.
     *
     * @return the window.
     */ 
    public GameWindow getWindow() {
        return window;
    }

    /**
     * Gets the skin manager.
     *
     * @return the skin manager.
     */ 
    public SkinManager getSkinManager() {
        return skinManager;
    }

    /**
     * Checks if the debug HUD is shown.
     *
     * @return whether the debug HUD is shown.
     */ 
    public boolean isShowDebugHud() {
        return ClientConfig.enableDebugUtils;
    }

    /**
     * Sets whether the debug HUD is shown.
     *
     * @param showDebugHud whether the debug HUD is shown.
     */ 
    public void setShowDebugHud(boolean showDebugHud) {
        ClientConfig.enableDebugUtils = showDebugHud;
        this.newConfig.save();
    }

    /**
     * Reloads the resources asynchronously.
     */ 
    public void reloadResourcesAsync() {
        if (!isOnRenderThread()) {
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

    /**
     * Gets the material manager.
     *
     * @return the material manager.
     */ 
    public MaterialManager getMaterialManager() {
        return materialManager;
    }

    /**
     * Gets the shader provider manager.
     *
     * @return the shader provider manager.
     */ 
    public ShaderProviderManager getShaderProviderManager() {
        return shaderProviderManager;
    }

    /**
     * Gets the shader program manager.
     *
     * @return the shader program manager.
     */ 
    public ShaderProgramManager getShaderProgramManager() {
        return shaderProgramManager;
    }

    /**
     * Disconnects the client.
     *
     * @param message the message.
     */ 
    public void onDisconnect(String message) {
        this.showScreen(new DisconnectedScreen(message, !connection.isMemoryConnection()));
    }

    /**
     * Cycles the player view.
     */ 
    public void cyclePlayerView() {
        this.playerView = switch (this.playerView) {
            case FIRST_PERSON -> PlayerView.THIRD_PERSON;
            case THIRD_PERSON -> PlayerView.THIRD_PERSON_FRONT;
            case THIRD_PERSON_FRONT -> PlayerView.FIRST_PERSON;
        };
    }

    /**
     * Attacks an entity.
     *
     * @param entity the entity.
     */ 
    public void attack(Entity entity) {
        if (entity == null) return;
        this.connection.send(new C2SAttackPacket(entity));
    }

    /**
     * Gets the asset manager.
     *
     * @return the asset manager.
     */ 
    public AssetManager getAssetManager() {
        return assetManager;
    }

    /**
     * Gets the mod config screen.
     *
     * @param caller the mod that to get the config screen for.
     * @return the mod config screen.
     */ 
    public ConfigScreenFactory getModConfigScreen(Mod caller) {
        return cfgScreenFactories.get(caller.getName());
    }

    /**
     * Sets the mod config screen.
     *
     * @param caller the mod that to get the config screen for.
     * @param factory the factory to set as the config screen for the mod.
     */ 
    public void setModConfigScreen(Mod caller, ConfigScreenFactory factory) {
        cfgScreenFactories.put(caller.getName(), factory);
    }

    /**
     * Gets the cubemap manager.
     *
     * @return the cubemap manager.
     */ 
    public CubemapManager getCubemapManager() {
        return cubemapManager;
    }

    /**
     * Checks if the window vibrancy is enabled.
     *
     * @return whether the window vibrancy is enabled.
     */ 
    public boolean isWindowVibrancyEnabled() {
        return windowVibrancyEnabled;
    }

    /**
     * Checks if a mouse button is pressed.
     *
     * @param mouseX the mouse X coordinate.
     * @param mouseY the mouse Y coordinate.
     * @param button the button.
     * @return whether the button is pressed.
     */ 
    public boolean mousePress(int mouseX, int mouseY, int button) {
        if (mouseX < 0 || mouseY < 0 || mouseX > getWidth() || mouseY > getHeight()) return false;

        this.lastPress = System.currentTimeMillis();

        // Close, maximize, and minimize buttons
        if (isCustomBorderShown() && mouseY < 44 && button == Input.Buttons.LEFT) {
            if (closeButton.isWithinBounds(mouseX - 18, mouseY - 22))
                return closeButton.mousePress(mouseX - 18, mouseY - 22, button);
            if (maximizeButton.isWithinBounds(mouseX - 18, mouseY - 22))
                return maximizeButton.mousePress(mouseX - 18, mouseY - 22, button);
            if (minimizeButton.isWithinBounds(mouseX - 18, mouseY - 22))
                return minimizeButton.mousePress(mouseX - 18, mouseY - 22, button);

            if (this.lastCBPress - System.currentTimeMillis() + 1000L > 0) {
                lastCBPress = 0;
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
            this.lastCBPress = System.currentTimeMillis();
            return true;
        }

        // Handle mouse press events for the current screen
        Screen scr = this.screen;
        if (scr != null) {
            GridPoint2 mouse = getMousePos();
            scr.mousePress((int) (mouse.x / guiScale), (int) (mouse.y / guiScale), button);
        }

        return false;
    }

    /**
     * Handles mouse release events.
     *
     * @param mouseX the mouse X coordinate.
     * @param mouseY the mouse Y coordinate.
     * @param button the button.
     * @return whether the button was released.
     */ 
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        // Check if the mouse is outside the window
        if (mouseX < 0 || mouseY < 0 || mouseX > getWidth() || mouseY > getHeight()) return false;

        // Scale mouse coordinates
        mouseX /= 2;
        mouseY /= 2;

        // Close, maximize, and minimize buttons
        if (isCustomBorderShown() && mouseY < 44) {
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
        }

        // Handle mouse release events for the current screen
        Screen scr = this.screen;
        if (scr != null) {
            GridPoint2 mouse = getMousePos();
            if (lastPress - System.currentTimeMillis() < 1000L) {
                clicks++;
            } else {
                clicks = 1;
            }
            scr.mouseClick((int) (mouse.x / guiScale), (int) (mouse.y / guiScale), button, clicks);
            scr.mouseRelease((int) (mouse.x / guiScale), (int) (mouse.y / guiScale), button);
        }

        return false;
    }

    /**
     * Sets the title of the game window.
     *
     * @param title The title of the game window. If null or blank, the default title will be used.
     */
    public void setWindowTitle(TextObject title) {
        String text = title == null ? null : title.getText();
        if (text != null && !text.isBlank()) {
            this.window.setTitle(String.format("Quantum Voxel %s - %s", QuantumClient.getGameVersion().split("\\+")[0], text));
            return;
        }
        this.window.setTitle(String.format("Quantum Voxel %s", QuantumClient.getGameVersion().split("\\+")[0]));
    }

    /**
     * Returns the current game time in seconds.
     *
     * @return The current game time in seconds.
     */
    public double getGameTime() {
        return System.currentTimeMillis() / 1000.0;
    }

    /**
     * Schedules a task to be executed repeatedly at a fixed rate.
     *
     * @param func        The task to be executed.
     * @param initialDelay The time to delay first execution.
     * @param interval    The period between successive executions.
     * @param unit        The time unit of the initialDelay and interval parameters.
     * @return A ScheduledFuture representing pending completion of the task, and whose {@code get()} method will return null upon completion.
     */
    public ScheduledFuture<?> scheduleRepeat(Runnable func, int initialDelay, int interval, TimeUnit unit) {
        return this.scheduler.scheduleAtFixedRate(func, initialDelay, interval, unit);
    }

    /**
     * Shuts down the client, waiting for all tasks to complete.
     */ 
    @SuppressWarnings("BusyWait")
    @Override
    public void shutdown() {
        try {
            if (this.shuttingDown) return;
            this.shuttingDown = true;
            super.shutdown();

            LOGGER.info("Shutting down executor service");
            executor.shutdown();

            LOGGER.info("Shutting down scheduler");
            scheduler.shutdown();

            while (!executor.isTerminated() || !scheduler.isTerminated()) {
                if (this.screen instanceof ShutdownScreen shutdownScreen) {
                    shutdownScreen.setMessage("Waiting for executor services to terminate");
                }
                LOGGER.info("Waiting for executor services to terminate");
                
                executor.awaitTermination(1, TimeUnit.SECONDS);
                scheduler.awaitTermination(1, TimeUnit.SECONDS);
            }

            if (this.integratedServer != null) {
                this.integratedServer.shutdown();

                // Wait for the server to terminate
                while (!this.integratedServer.isShutdown()) {
                    if (this.screen instanceof ShutdownScreen shutdownScreen) {
                        shutdownScreen.setMessage("Waiting for server to terminate");
                    }
                    LOGGER.info("Waiting for server to terminate");
                    Thread.sleep(1000);
                }
            }

            CommonConstants.LOGGER.info("Shutting down RPC handler");
            RpcHandler.disable();
        } catch (InterruptedException e) {
            QuantumClient.LOGGER.error("Failed to shutdown background tasks", e);
            Runtime.getRuntime().halt(1); // Forcefully terminate the process
        }

        LOGGER.info("Shutting down Quantum Client");
        Gdx.app.exit();
    }

    /**
     * Updates the viewport.
     */ 
    public void updateViewport() {
        if (!insets.equals(GamePlatform.get().getInsets())) {
            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            insets.set(GamePlatform.get().getInsets());
        }
    }

    /**
     * Runs a task asynchronously.
     *
     * @param o the task.
     * @return the future.
     */ 
    public CompletableFuture<Void> runAsyncTask(Runnable o) {
        return CompletableFuture.runAsync(o, executor);
    }

    /**
     * Sets the user.
     *
     * @param user the user.
     */ 
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Handles mouse wheel events.
     *
     * @param amountX the amount of X movement.
     * @param amountY the amount of Y movement.
     */ 
    public void mouseWheel(float amountX, float amountY) {
        Screen scr = this.screen;
        if (scr != null) {
            GridPoint2 mouse = getMousePos();
            if (mouse.x < 0 || mouse.y < 0 || mouse.x > getWidth() || mouse.y > getHeight()) return;

            scr.mouseWheel((int) (mouse.x / guiScale), (int) (mouse.y / guiScale), amountY);
        }
    }

    /**
     * Handles mouse moved events.
     *
     * @param screenX the screen X coordinate.
     * @param screenY the screen Y coordinate.
     */ 
    public void mouseMoved(int screenX, int screenY) {
        lastPress = Integer.MIN_VALUE / 2;

        // Handle mouse moved events for the current screen
        Screen scr = this.screen;
        if (scr != null) {
            if (screenX < 0 || screenY < 0 || screenX > getWidth() || screenY > getHeight()) {
                if (hovered) {
                    scr.mouseExit();
                }
                this.hovered = false;
                return;
            } else if (!hovered) {
                scr.mouseEnter((int) (screenX / guiScale), (int) (screenY / guiScale));
                this.hovered = true;
            }

            scr.mouseMoved((int) (screenX / guiScale), (int) (screenY / guiScale));
        }
    }

    public RenderBufferSource renderBuffers() {
        return renderBuffers;
    }
}
