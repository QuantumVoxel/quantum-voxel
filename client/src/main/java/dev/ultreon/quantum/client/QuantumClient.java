package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
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
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.badlogic.gdx.video.assets.VideoLoader;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.Logger;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.client.api.events.ClientLifecycleEvents;
import dev.ultreon.quantum.client.api.events.ClientTickEvents;
import dev.ultreon.quantum.client.api.events.RenderEvents;
import dev.ultreon.quantum.client.api.events.WindowEvents;
import dev.ultreon.quantum.client.api.events.gui.ScreenEvents;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.audio.ClientSound;
import dev.ultreon.quantum.client.audio.music.MusicManager;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.config.ConfigScreenFactory;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.debug.DebugOverlay;
import dev.ultreon.quantum.client.gui.overlay.LoadingOverlay;
import dev.ultreon.quantum.client.gui.overlay.ManualCrashOverlay;
import dev.ultreon.quantum.client.gui.overlay.OverlayManager;
import dev.ultreon.quantum.client.gui.screens.*;
import dev.ultreon.quantum.client.gui.screens.world.WorldLoadScreen;
import dev.ultreon.quantum.client.input.*;
import dev.ultreon.quantum.client.item.ItemRenderer;
import dev.ultreon.quantum.client.management.*;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BakedModelRegistry;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.model.item.ItemModel;
import dev.ultreon.quantum.client.model.item.ItemModelRegistry;
import dev.ultreon.quantum.client.model.model.JsonModelLoader;
import dev.ultreon.quantum.client.multiplayer.MultiplayerData;
import dev.ultreon.quantum.client.network.LoginClientPacketHandlerImpl;
import dev.ultreon.quantum.client.network.system.ClientTcpConnection;
import dev.ultreon.quantum.client.network.system.ClientWebSocketConnection;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.player.SkinManager;
import dev.ultreon.quantum.client.registry.ClientSyncRegistries;
import dev.ultreon.quantum.client.registry.EntityModelRegistry;
import dev.ultreon.quantum.client.registry.EntityRendererRegistry;
import dev.ultreon.quantum.client.registry.ModIconOverrideRegistry;
import dev.ultreon.quantum.client.render.*;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.client.resources.ResourceNotFoundException;
import dev.ultreon.quantum.client.rpc.GameActivity;
import dev.ultreon.quantum.client.rpc.RpcHandler;
import dev.ultreon.quantum.client.sound.ClientSoundRegistry;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.client.text.LanguageManager;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.client.util.*;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.debug.Debugger;
import dev.ultreon.quantum.debug.profiler.Profiler;
import dev.ultreon.quantum.debug.timing.Timing;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.tool.ToolItem;
import dev.ultreon.quantum.network.MemoryConnectionContext;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.c2s.C2SAttackPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SLoginPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.network.system.MemoryConnection;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.server.PlatformOS;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.text.LanguageBootstrap;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import space.earlygrey.shapedrawer.ShapeDrawer;

import javax.annotation.WillClose;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.math.MathUtils.ceil;
import static dev.ultreon.quantum.server.PlatformOS.isMac;

/**
 * This class is the main entry point for the Quantum Voxel Client.
 * It is responsible for initializing and running the game.
 * It also provides access to the game's main elements and resources.
 *
 * @author <a href="https://github.com/Ultreon">Ultreon Studios</a>
 * @see <a href="https://github.com/Ultreon/quantum-voxel">Quantum Voxel</a>
 * @since <i>Always :smirk:</i>
 */
@SuppressWarnings({"UnusedReturnValue", "deprecation", "t"})
public class QuantumClient extends PollingExecutorService implements DeferredDisposable, DesktopMain {
    // Public constants
    public static final Logger LOGGER = LoggerFactory.getLogger("QuantumClient");
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
    public final AsyncExecutor executor = new AsyncExecutor(Math.min(GamePlatform.get().cpuCores() / 2, 2), "ClientTask");
    //    public ControllerInput controllerInput;
    public TouchInput touchInput;
//    public VirtualKeyboard virtualKeyboard;

    public NodeCategory backgroundCat = new NodeCategory();
    public NodeCategory worldCat = new NodeCategory();
    public NodeCategory mainCat = new NodeCategory();

    // Local data
    public LocalData localData = LocalData.load();
    public ClientSyncRegistries registries = new ClientSyncRegistries(this);
    public boolean saving;
    public WorldSaveInfo worldSaveInfo;
    public GraphicsSetting graphicsSetting = GraphicsSetting.NORMAL;

    ManualCrashOverlay crashOverlay; // MANUALLY_INITIATED_CRASH

    // Cursors
    private final Cursor normalCursor;
    private final Cursor clickCursor;
    private Cursor cursor0;

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
    public JsonModelLoader j5ModelLoader;
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
    boolean imGui;
    boolean isDevMode;

    private boolean startDevLoading = false;

    // Configuration
    public ClientConfiguration newConfig;

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
    private VideoLoader videoLoader = new VideoLoader(new InternalFileHandleResolver());
    private VideoPlayer ultreonSplashVideo;
    private VideoPlayer libgdxSplashVideo;

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
    GameRenderer gameRenderer;

    // GUI stuff
    @Nullable
    public Screen screen;
    public Hud hud;

//    private FrameBuffer fbo;

    public SpriteBatch spriteBatch;
    public ShapeDrawer shapes;
    public Renderer renderer;

    private float guiScale = this.calcMaxGuiScale();

    // Notifications
    public Notifications notifications = new Notifications(this);

    // Input
    public KeyAndMouseInput keyAndMouseInput;

    // World
    public ClientWorld world;

    private final Screenshots screenshots = new Screenshots(this);


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
    public TextureAtlas environmentTextureAtlas;

    // Baked Models
    @HiddenNode
    public BakedModelRegistry bakedBlockModels;

    // Advanced Shadows
    private final List<Promise<?>> futures = new CopyOnWriteArrayList<>();

    // Window stuff
    @Nullable
    Integer deferredWidth;
    @Nullable
    Integer deferredHeight;

    int width;
    int height;

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
    private final RenderBufferSource renderBuffers = new RenderBufferSource();
    private int cachedWidth;
    private int cachedHeight;
    private boolean debugOverlayShown = GamePlatform.get().isDevEnvironment();

    // Misc
    GameEnvironment gameEnv;

    boolean loading = true;

    final String[] argv;
    private Vec2i oldMode;
    private int oldSelected;
    private boolean wasClicking;
    private final Queue<Runnable> serverTickQueue = new ArrayDeque<>();

    private final QuantumClientLoader loader = new QuantumClientLoader();
    private long libGDXSplashTime;
    private Texture videoTexture;

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

        VideoLoader.VideoParameter param = new VideoLoader.VideoParameter();
        param.looping = false;
        param.volume = 1f;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        ultreonSplashVideo = VideoPlayerCreator.createVideoPlayer();
        ultreonSplashVideo.setFilter(param.minFilter, param.magFilter);
        ultreonSplashVideo.setLooping(param.looping);
        ultreonSplashVideo.setVolume(param.volume);
        try {
            if (!ultreonSplashVideo.load(Gdx.files.internal("intro.webm"))) {
                throw new AssertionError("Failed to load Ultreon intro video!");
            }
        } catch (FileNotFoundException ignored) {
            crash(ignored);
        }
        ultreonSplashVideo.setOnCompletionListener(file -> {
            showUltreonSplash = false;
            ultreonSplashVideo.dispose();
            this.startLoading();
        });
        libgdxSplashVideo = VideoPlayerCreator.createVideoPlayer();
        libgdxSplashVideo.setFilter(param.minFilter, param.magFilter);
        libgdxSplashVideo.setLooping(param.looping);
        libgdxSplashVideo.setVolume(param.volume);
        try {
            if (!libgdxSplashVideo.load(Gdx.files.internal("libgdx.webm"))) {
                throw new AssertionError("Failed to load libGDX intro video!");
            }
        } catch (FileNotFoundException ignored) {
            crash(ignored);
        }
        libgdxSplashVideo.setOnCompletionListener(file -> {
            this.showLibGDXSplash = false;
            this.showUltreonSplash = true;
            libgdxSplashVideo.dispose();
        });

        if (QuantumClient.instance != null)
            throw new AssertionError("Double Loading!");

        for (int i = 0; i < Gdx.input.getMaxPointers(); i++) {
            touchPosStartScl[i] = new Vector2();
            touchPosStart[i] = new Vector2();
            touchMovedScl[i] = new Vector2();
            touchMoved[i] = new Vector2();
        }

        this.mainCat.add("Client", this);
        this.add("Render Buffers", this.renderBuffers);

        ModApi.init();

        // Disable shader pedantic mode
        ShaderProgram.pedantic = false;

        // Add a shutdown hook to gracefully shut down the server
        GamePlatform.get().addShutdownHook(() -> {
            this.shutdown(() -> {

            });

            QuantumClient.LOGGER.info("Shutting down game!");
            QuantumClient.instance = null;
        });

        // Log the booting of the game
        QuantumClient.LOGGER.info("Booting game!");
        QuantumClient.instance = this;

        GamePlatform.get().initMods();

        if (this.localData.username != null) {
            this.user = new User(this.localData.username);
        }

        // Initialize the unifont and font
        this.font = new GameFont(loadFont(id("luna_pixel"), false), Font.DistanceFieldType.STANDARD, 0, -13, 0, -20, true);
        this.font.useIntegerPositions(true);
        this.font.setBoldStrength(0.33f);
        this.font.lineHeight = 7f;

        KnownFonts.addEmoji(font);

        this.unifont = new GameFont(loadFont(id("unifont"), false), Font.DistanceFieldType.STANDARD, 0, -14, 0, -28, true);
        this.unifont.useIntegerPositions(true);
        this.unifont.setBoldStrength(0.33f);
        this.unifont.scale(0.5f, 0.5f);

        KnownFonts.addEmoji(unifont);

        // Initialize the game window
        this.window = GamePlatform.get().createWindow();

        // Initialize the resource manager, texture manager, and resource loader
        this.resourceManager = new ResourceManager("assets");
        this.add("Resource Manager", resourceManager);

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
        ModLoadingContext.withinContext(GamePlatform.get().getMod(CommonConstants.NAMESPACE).orElseThrow(() -> new IllegalStateException("Failed to get mod instance")), () -> {
            ClientConfiguration.load();
            onReloadConfig();
        });

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

        // Create a white pixel for the shape drawer
        Pixmap pixmap = deferDispose(new Pixmap(1, 1, Pixmap.Format.RGBA8888));
        pixmap.setColor(1F, 1F, 1F, 1F);
        pixmap.drawPixel(0, 0);
        TextureRegion white = new TextureRegion(new Texture(pixmap));

        // Initialize the sprite batch, shape drawer, and renderer
        this.spriteBatch = deferDispose(new SpriteBatch());

        // Set the projection matrix for the spriteBatch
        this.spriteBatch.getProjectionMatrix().setToOrtho(0, QuantumClient.get().getWidth(), QuantumClient.get().getHeight(), 0, 0, 1000000);

        this.shapes = new ShapeDrawer(this.spriteBatch, white);
        this.renderer = new Renderer(this.shapes);

        // Initialize GameRenderer
        this.gameRenderer = new GameRenderer(this);

        // Set up modifications
        this.setupMods();

        // Load textures for splash screen
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
        LanguageManager.setCurrentLanguage(new Locale("en", "us"));

        this.closeButton = new ControlButton(ControlIcon.Close);
        this.maximizeButton = new ControlButton(ControlIcon.Maximize);
        this.minimizeButton = new ControlButton(ControlIcon.Minimize);
    }

    public BitmapFont loadFont(@NotNull NamespaceID resource, boolean b) {
        NamespaceID id = resource.mapPath(path -> "font/" + path + ".fnt");
        var handle = resource(id);
        if (!handle.exists()) {
            throw new ResourceNotFoundException(id);
        }
        BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData(handle, false);
        if (data.imagePaths == null)
            throw new IllegalArgumentException("The font data must have an images path.");

        // Load each path.
        int n = data.imagePaths.length;
        Array<TextureRegion> regions = new Array(n);
        for (int i = 0; i < n; i++) {
            if (handle instanceof ResourceFileHandle) {
                id = NamespaceID.parse(data.imagePaths[i]).mapPath(path -> "textures/" + path);
            } else {
                String path = handle.parent().path().replace('\\', '/');
                if (path.endsWith("/")) {
                    path += "/";
                }
                id = NamespaceID.of("textures/font" + data.imagePaths[i].substring(path.length()));
            }
            FileHandle file = resource(id);
            if (!file.exists())
                throw new ResourceNotFoundException(id);
            regions.add(new TextureRegion(new Texture(file, false)));
        }
        return new BitmapFont(data, regions, true);
    }

    /**
     * Returns a shader file handle for the given namespace ID.
     *
     * @param id the namespace ID of the shader.
     * @return the shader file handle.
     */
    public static @NotNull FileHandle shader(NamespaceID id) {
        return resource(new NamespaceID(id.getDomain(), "shaders/" + id.getPath()));
    }

    /**
     * Returns an instance of IClipboard.
     *
     * @return An instance of IClipboard.
     */
    private IClipboard createClipboard() {
        return new DefaultClipboard();
    }

    /**
     * Game crash hook, which will be called when a crash occurs.
     * <p>
     * <p style="font-size: 16px"><b>ONLY USE THIS IF YOU KNOW WHAT YOU ARE DOING</b></p>
     * <p>WHEN THIS IS NON-NULL CRASHES WILL BE CAPTURED AND WILL STOP THE GAME FROM HANDLING THEM.</p>
     * <p>So, make sure to actually handle the crash when using this.</p>
     */
    @UnsafeApi
    public static void setCrashHook(Callback<CrashLog> crashHook) {
        QuantumClient.crashHook = crashHook;
    }

    /**
     * This method is used to reload the config.
     */
    void onReloadConfig() {
        if (ClientConfiguration.fullscreen.getValue()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        String[] split = ClientConfiguration.language.getValue().getPath().split("_");
        if (split.length == 2) {
            LanguageManager.setCurrentLanguage(new Locale(split[0], split[1]));
        } else {
            QuantumClient.LOGGER.error("Invalid language: {}", ClientConfiguration.language);
            LanguageManager.setCurrentLanguage(new Locale("en", "us"));
            ClientConfiguration.language.setValue(QuantumClient.id("en_us"));
            ClientConfiguration.save();
        }

        if (ClientConfiguration.guiScale.getValue() != 0) {
            this.setAutomaticScale(false);
            this.setGuiScale(ClientConfiguration.guiScale.getValue());
        } else {
            this.setAutomaticScale(true);
        }

        this.camera.fov = ClientConfiguration.fov.getValue();
        this.graphicsSetting = GraphicsSetting.of(ClientConfiguration.graphicsSetting.getValue());

        // Cancel vibration if it is not enabled
        if (!ClientConfiguration.vibration.getValue()) {
            GameInput.cancelVibration();
        }

        QuantumClient.invoke(() -> {
            // Set vsync
            boolean enableVsync = ClientConfiguration.enableVsync.getValue();
            Gdx.graphics.setVSync(enableVsync);

            // Set fps limit
            int fpsLimit = ClientConfiguration.fpsLimit.getValue();
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
        return GamePlatform.get().isMobile() ? Gdx.files.external(path) : Gdx.files.local(path);
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
     * <p>
     * This method initializes an array of strings representing the icons based on whether the platform is macOS or not.
     * If the platform is macOS, it sets all icons to "icons/mac.png". Otherwise, it sets them to "icons/icon.png".
     *
     * @return The array of icon paths. Each element in the array corresponds to an icon size defined in QuantumClient.SIZES or a single size if not macOS.
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
    public static FileHandle getGameDir() {
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
        WorldStorage storage = new WorldStorage(Gdx.files.local("worlds/dev"));
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
     * Thi also initializes and loads configurations from entry points.
     */
    private void setupMods() {
        // Set mod icon overrides.
        ModIconOverrideRegistry.set("quantum", QuantumClient.id("icon.png"));
        ModIconOverrideRegistry.set("java", QuantumClient.id("textures/java_icon.png"));
        ModIconOverrideRegistry.set("gdx", new NamespaceID("gdx", "icon.png"));

        // Invoke entry points for initialization.
        GamePlatform loader = GamePlatform.get();
//        loader.invokeEntrypoint(ModInit.ENTRYPOINT_KEY, ModInit.class, ModInit::onInitialize);

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
    public static <T> T invokeAndWait(@NotNull Callable<T> func) {
        if (isOnRenderThread()) {
            try {
                return func.call();
            } catch (Exception e) {
                throw new RejectedExecutionException("Failed to execute task", e);
            }
        }
        return QuantumClient.instance.submit(func).get();
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
     * Invokes the given runnable asynchronously and returns a {@link CompletionPromise} that completes with Void.
     *
     * @param func the runnable to be invoked
     * @return a CompletionPromise that completes with Void once the runnable has been invoked
     */
    public static @NotNull CompletionPromise<Void> invoke(Runnable func) {
        return QuantumClient.instance.submit(func);
    }

    /**
     * Invokes the given callable function asynchronously and returns a {@link CompletionPromise}.
     *
     * @param func the callable function to be invoked
     * @param <T>  the type parameter of the callable function's return value
     * @return a CompletionPromise representing the pending result of the callable function
     */
    public static <T> @NotNull CompletionPromise<T> invoke(Callable<T> func) {
        return QuantumClient.instance.submit(func);
    }

    /**
     * Returns a new instance of FileHandle for the specified resource identifier.
     *
     * @param id The identifier of the resource.
     * @return A new instance of FileHandle for the specified resource.
     */
    public static @NotNull FileHandle resource(NamespaceID id) {
        if (instance.resourceManager != null) {
            ResourceFileHandle handle = new ResourceFileHandle(id);
            if (handle.getResource() != null) return handle;
        }
        return Gdx.files.internal("assets/" + id.getDomain() + "/" + id.getPath());
    }

    /**
     * Checks whether the current thread is the main thread.
     *
     * @return true if the current thread is the main thread, false otherwise.
     */
    public static boolean isOnRenderThread() {
        if (GamePlatform.get().isWeb()) return true;
        return Thread.currentThread().getId() == QuantumClient.instance.mainThread.getId();
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

    /**
     * Creates a directory if it does not exist, and recreates it if it is a non-directory file.
     *
     * @param dirName The name of the directory to create or recreate.
     * @return The handle to the created or recreated directory.
     */
    static FileHandle createDir(String dirName) {
        var directory = QuantumClient.data(dirName);
        if (!directory.exists()) {
            // Create the directory if it doesn't exist
            directory.mkdirs();
        } else if (!directory.isDirectory()) {
            // Delete the non-directory file, and recreate directory.
            directory.delete();
            directory.mkdirs();
        }
        return directory; // Return the created or recreated directory
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
    public boolean showScreen(@Nullable Screen next) {
        // If not on render thread, invoke on render thread
        if (!isOnRenderThread()) {
            @Nullable Screen finalNext = next;
            return invokeAndWait(() -> this.showScreen(finalNext));
        }

        // Remove previous screen if it exists
        if (this.screen != null) this.remove(this.screen);

        // Handle pause screen screenshots for world
        WorldStorage theWorldStorage = openedWorld;
        if (!screenshots.isSkipScreenshot() && next instanceof PauseScreen && world != null && theWorldStorage != null) {
            PauseScreen pause = (PauseScreen) next;
            screenshots.screenshot(true, screenshot -> {
                if (screenshot != null) {
                    try {
                        // Try to save world screenshot
                        screenshot.save(theWorldStorage.getDirectory().child("picture.png"));
                    } catch (Exception e) {
                        notifications.add("Save Error", "Failed to save world screenshot", "AUTO SAVE");
                    }
                }

                // Show pause screen after screenshot
                screenshots.setSkipScreenshot(true);
                invoke(() -> {
                    boolean b = this.showScreen(pause);
                    screenshots.setSkipScreenshot(false);
                    return b;
                });
            });
        }

        // Get current screen for handling transitions
        var cur = this.screen;

        // Default to title screen if no world loaded
        if (next == null && this.world == null) {
            next = new TitleScreen();
            LOGGER.warn("World is null, showing title screen");
        }

        // Handle null screen cases
        if (next == null) {
            Gdx.input.setCursorPosition(QuantumClient.get().getWidth() / 2, QuantumClient.get().getHeight() / 2);
            this.setWindowTitle(TextObject.literal("Playing in a world!"));
            LOGGER.warn("Next screen is null, showing null screen");
            return cur == null || this.closeScreen(cur);
        }

        // Call screen open event handlers
        var openResult = ScreenEvents.OPEN.factory().onOpenScreen(next);
        if (openResult.isCanceled()) {
            LOGGER.warn("Opening screen was canceled");
            return false;
        }

        // Handle interrupted open event
        if (openResult.isInterrupted()) {
            next = openResult.getValue();
            LOGGER.warn("Opening screen was interrupted, and new screen was {}", next == null ? "null" : next);
        }

        // Close current screen if needed
        if (cur != null && this.closeScreen(next, cur)) {
            LOGGER.warn("Closing screen was canceled");
            return false; // Close was canceled
        }

        // Handle null screen after events
        if (next == null) {
            LOGGER.warn("Next screen is null, cancelling");
            Gdx.input.setCursorPosition(QuantumClient.get().getWidth() / 2, QuantumClient.get().getHeight() / 2);
            this.setWindowTitle(TextObject.literal("Playing in a world!"));
            return false; // The next screen is null, cancelling.
        }

        // Log screen transition
        if (cur != null) {
            LOGGER.warn("Switching from {} to {}", cur, next);
            cur.mouseExit();
        } else {
            LOGGER.warn("Switching to {}", next);
        }

        // Initialize new screen
        this.screen = next;
        GridPoint2 mouse = getMousePos();
        this.screen.mouseEnter(mouse.x, mouse.y);
        this.screen.init(this.getScaledWidth(), this.getScaledHeight());
        this.add("Screen", next);
        KeyAndMouseInput.setCursorCaught(false);

        // Update window title
        this.setWindowTitle(this.screen.getTitle());

        return true;
    }

    /**
     * Handles the closing of a screen.
     *
     * @param next The screen to switch to, may be null
     * @param cur  The current screen being closed
     * @return true if screen close was cancelled, false if screen was closed successfully
     */
    private boolean closeScreen(@Nullable Screen next, Screen cur) {
        // Fire screen close event and check if cancelled
        var closeResult = ScreenEvents.CLOSE.factory().onCloseScreen(cur);
        if (closeResult.isCanceled()) return true;

        // Allow screen to handle its own closing and check if cancelled
        if (!cur.onClose(next)) return true;

        // Screen close was successful, notify screen it is now closed
        cur.onClosed();
        return false;
    }

    /**
     * Closes the current screen.
     *
     * @param cur The screen to close
     * @return true if screen was closed successfully, false if closing was cancelled
     */
    private boolean closeScreen(Screen cur) {
        // Try to close the screen, return false if cancelled
        if (this.closeScreen(null, cur)) return false;

        // Clear current screen reference and catch cursor
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
        // Handle music based on world and screen state
        if (world != null) {
            if (screen == null) {
                // If in web platform and cursor not caught, show pause screen
                if (!Gdx.input.isCursorCatched() && GamePlatform.get().isWeb()) {
                    showScreen(new PauseScreen());
                    MusicManager.get().pause();
                } else {
                    MusicManager.get().resume();
                    MusicManager.get().update();
                }
            } else {
                MusicManager.get().pause();
            }
        } else {
            MusicManager.get().stop();
        }

        // Handle memory connection updates for web platform
        IConnection<ClientPacketHandler, ServerPacketHandler> connection1 = connection;
        if (connection1 instanceof MemoryConnection) {
            MemoryConnection<ClientPacketHandler, ServerPacketHandler> connection2 = (MemoryConnection<ClientPacketHandler, ServerPacketHandler>) connection1;
            if (GamePlatform.get().isWeb()) {
                connection2.update();
                connection2.getOtherSide().update();
            }
        }

        float deltaTime = Gdx.graphics.getDeltaTime();

        // Handle window resize if dimensions changed
        if (this.cachedWidth != this.getWidth() || this.cachedHeight != this.getHeight()) {
            this.cachedWidth = this.getWidth();
            this.cachedHeight = this.getHeight();
            this.resize(this.getWidth(), this.getHeight());
        }

        // Set OpenGL viewport
        Gdx.gl.glViewport(0, 0, this.getWidth(), this.getHeight());

        // Clean up any pending disposables
        Disposable disposable;
        while ((disposable = this.disposalQueue.poll()) != null) {
            try {
                cleanUp(disposable);
            } catch (Exception e) {
                QuantumClient.crash(new Throwable("Failed to dispose " + disposable + " during render", e));
            }
        }

        try {
            // Update profiler
            QuantumClient.PROFILER.update();

            // Update debug GUI if enabled
            if (this.debugGui != null && !this.loading) {
                if (this.isShowDebugHud()) this.debugGui.updateProfiler();
                this.debugGui.update();
            }

            // Do main rendering
            try (var ignored = PROFILER.start("render")) {
                this.doRender(deltaTime);
            }
            this.renderer.actuallyEnd();
        } catch (OutOfMemoryError e) {
            // Try to free memory and show the OOM screen
            System.gc();
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

        // Disable face culling
        Gdx.gl.glDisable(GL_CULL_FACE);

        // Finish rendering
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
            this.width = QuantumClient.get().getWidth();
            this.height = QuantumClient.get().getHeight();
        }

        // If the deferred width or height is not null and the width or height is not equal to the deferred width or height, set the width and height to the deferred width and height.
        if (deferredWidth != null && deferredHeight != null && (width != deferredWidth || height != deferredHeight)) {
            width = deferredWidth;
            height = deferredHeight;

            this.resize(QuantumClient.get().getWidth(), QuantumClient.get().getHeight());
        }

        screenshots.prepareScreenshot(deltaTime);

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

        screenshots.renderFlash(renderer, width, height);

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
     * Draws the custom window border with title bar and control buttons.
     *
     * @param renderer The renderer used to draw the border graphics
     */
    private void drawCustomBorder(Renderer renderer) {
        // Begin rendering
        this.renderer.begin();

        // Save current transform and scale up by 2x
        renderer.pushMatrix();
        renderer.scale(2, 2);

        // Draw window border with title bar at calculated position
        this.renderWindow(renderer, this.getWidth() / 2 + 36, this.getHeight() / 2 + 44);

        // Restore transform
        renderer.popMatrix();

        // End rendering
        this.renderer.end();
    }

    /**
     * Renders the game.
     *
     * @param renderer  the renderer.
     * @param deltaTime the delta time.
     * @return whether the game was rendered.
     */
    private void renderGame(Renderer renderer, float deltaTime) {
        // Handle first render
        if (Gdx.graphics.getFrameId() == 2) {
            this.firstRender();
        }

        // Update game activity
        this.updateActivity();

        // Poll for updates
        this.pollAll();

        // Process game ticks
        Timing.start("try_client_tick");
        tryClientTick();
        Timing.end("try_client_tick");

        // Check if splash screens should be shown
        boolean renderSplash = this.showUltreonSplash || this.showLibGDXSplash;

        // Render LibGDX splash screen
        if (this.showLibGDXSplash) {
            this.renderLibGDXSplash(renderer);
        }

        // Render Ultreon splash screen
        if (this.showUltreonSplash) {
            this.renderUltreonSplash(renderer);
        }

        // Return early if splash screens are showing
        if (renderSplash) {
            return;
        }

        // Start loading in dev environment
        if (GamePlatform.get().isDevEnvironment() && this.startDevLoading) {
            this.startLoading();
        }

        // Show loading overlay if needed
        final LoadingOverlay loading = this.loadingOverlay;
        if (loading != null) {
            this.renderLoadingOverlay(renderer, deltaTime, loading);
            return;
        }

        // Set up OpenGL blending
        Gdx.gl.glEnable(GL_BLEND);
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glFlush();
        ScreenUtils.clear(0, 0, 0, 0, true);
        Gdx.gl.glFlush();

        // Render main game content
        Timing.start("render_main");
        this.renderMain(renderer, deltaTime);
        Timing.end("render_main");
        return;
    }

    /**
     * Renders the main game.
     *
     * @param renderer  the renderer.
     * @param deltaTime the delta time.
     */
    private void renderMain(Renderer renderer, float deltaTime) {
        // Handle player raycast
        Player player = this.player;
        if (player == null) {
            this.hit = null;
        } else {
            QuantumClient.PROFILER.section("playerRayCast", () -> this.hit = player.rayCast());
        }

        // Draw black background if needed
        renderer.begin();
        GridPoint2 drawOffset = this.getDrawOffset();
        if (!GamePlatform.get().hasBackPanelRemoved()) {
            renderer.fill(drawOffset.x, drawOffset.y, (int) (this.gameBounds.getWidth() * getGuiScale()) - drawOffset.x * 2, (int) (this.gameBounds.getHeight() * getGuiScale()) - drawOffset.y * 2, Color.BLACK);
        }
        renderer.end();

        // Update keyboard/mouse and touch input
        if (this.keyAndMouseInput != null) {
            try (var ignored = QuantumClient.PROFILER.start("input")) {
                this.keyAndMouseInput.update();
                if (this.touchInput != null) this.touchInput.update();
            }
        }

        // TODO: Properly support controllers without breaking TeaVM
//        if (this.controllerInput != null && this.keyAndMouseInput != null) {
//            try (var ignored = QuantumClient.PROFILER.start("input")) {
//                this.controllerInput.update();
//                this.keyAndMouseInput.update();
//                if (this.touchInput != null) this.touchInput.update();
//            }
//        }
//
//        ControllerContext.register(InGameControllerContext.INSTANCE, client -> client.screen == null);
//        ControllerContext.register(MenuControllerContext.INSTANCE, client -> client.screen != null);

        // Update cursor icon based on mouse state
        Screen screen = this.screen;
        if (screen != null && KeyAndMouseInput.isPressingAnyButton() && !this.wasClicking) {
            this.setCursor(this.clickCursor);
            this.wasClicking = true;
        } else if (screen != null && !KeyAndMouseInput.isPressingAnyButton() && this.wasClicking) {
            this.setCursor(this.normalCursor);
            this.wasClicking = false;
        }

        // Render the game
        RenderEvents.PRE_RENDER_GAME.factory().onRenderGame(gameRenderer, renderer, deltaTime);
        Timing.start("render_game");
        this.gameRenderer.render(renderer, deltaTime);

        // Render debug overlay if enabled
        try (var ignored = QuantumClient.PROFILER.start("debug")) {
            if (this.hideHud || !debugOverlayShown || this.isLoading()) return;
            renderer.begin();
            renderer.scale(2, 2);
            this.debugGui.render(renderer, 2);
            renderer.scale(0.5, 0.5);
            renderer.end();
        }

        // End game rendering
        Timing.end("render_game");
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
     * @param renderer  the renderer.
     * @param deltaTime the delta time.
     * @param loading   the loading overlay.
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
    /**
     * Starts loading game resources asynchronously. Shows loading overlay and handles crashes.
     * This is called when the game starts up.
     */
    private void startLoading() {
        this.startDevLoading = false;

        // Set crash hook to collect any crashes during loading
        QuantumClient.setCrashHook(this.crashes::add);

        // Show loading overlay
        this.loadingOverlay = new LoadingOverlay();

        // Start async loading process
        Promise.runAsync(loader).exceptionally(throwable -> {
            // Clear the crash handling
            QuantumClient.crashHook = null;

            // Record the loading failure
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
            // Clear the crash handling after successful load
            QuantumClient.crashHook = null;

            // Initialize appropriate screen based on load results
            QuantumClient.invoke(() -> {
                if (crashes.isEmpty()) {
                    // Show dev preview if no crashes, with username screen if needed
                    screen = new DevPreviewScreen(user == null ? new UsernameScreen() : null);
                } else {
                    // Show crash screen if there were crashes
                    screen = new CrashScreen(crashes);
                }

                // Initialize the screen and remove loading overlay
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
        try (var ignored = QuantumClient.PROFILER.start("libgdxSplash")) {
            // Initialize splash screen timing and play sound on the first render
            if (!this.libgdxSplashVideo.isPlaying())
                this.libgdxSplashVideo.play();
            libgdxSplashVideo.update();
            videoTexture = this.libgdxSplashVideo.getTexture();

            // Clear screen to black
            ScreenUtils.clear(0, 0, 0, 1, true);

            // Calculate zoom animation based on elapsed time
            if (videoTexture != null) {
                // Calculate scaled thumbnail dimensions
                resizer.set(videoTexture.getWidth(), videoTexture.getHeight());
                Vec2f thumbnail = this.resizer.thumbnail(this.getWidth(), this.getHeight());
                float drawWidth = thumbnail.x;
                float drawHeight = thumbnail.y;

                // Center the thumbnail on screen
                float drawX = (this.getWidth() - drawWidth) / 2;
                float drawY = (this.getHeight() - drawHeight) / 2;

                // Render background and logo textures
                renderer.begin();
                renderer.blit(videoTexture, drawX, drawY, drawWidth, drawHeight, 0, 0, videoTexture.getWidth(), videoTexture.getHeight(), videoTexture.getWidth(), videoTexture.getHeight());
                renderer.end();
            }
        }
    }

    /**
     * Renders the Ultreon splash screen with zoom animation.
     *
     * @param renderer The renderer used to draw the splash screen
     */
    private void renderUltreonSplash(Renderer renderer) {
        try (var ignored = QuantumClient.PROFILER.start("ultreonSplash")) {
            // Initialize splash screen timing and play sound on the first render
            if (!this.ultreonSplashVideo.isPlaying())
                this.ultreonSplashVideo.play();
            ultreonSplashVideo.update();
            videoTexture = this.ultreonSplashVideo.getTexture();

            // Clear screen to black
            ScreenUtils.clear(0, 0, 0, 1, true);

            // Calculate zoom animation based on elapsed time
            if (videoTexture != null) {
                // Calculate scaled thumbnail dimensions
                resizer.set(videoTexture.getWidth(), videoTexture.getHeight());
                Vec2f thumbnail = this.resizer.thumbnail(this.getWidth(), this.getHeight());
                float drawWidth = thumbnail.x;
                float drawHeight = thumbnail.y;

                // Center the thumbnail on screen
                float drawX = (this.getWidth() - drawWidth) / 2;
                float drawY = (this.getHeight() - drawHeight) / 2;

                // Render background and logo textures
                renderer.begin();
                renderer.blit(videoTexture, drawX, drawY, drawWidth, drawHeight, 0, 0, videoTexture.getWidth(), videoTexture.getHeight(), videoTexture.getWidth(), videoTexture.getHeight());
                renderer.end();
            }
        }
    }

    /**
     * Retrieves the game version of the Quantum Voxel mod.
     *
     * @return The game version as a {@code String}.
     */
    public static String getGameVersion() {
        return GamePlatform.get().getMod("quantum").orElseThrow(() -> new IllegalStateException("Quantum mod not found")).getVersion();
    }

    /**
     * Tries to tick the client.
     */
    /**
     * Handles client-side game tick logic and timing
     */
    private void tryClientTick() {
        var canTick = false;

        // Get current time and calculate time passed since last update
        double time2 = System.currentTimeMillis();
        var passed = time2 - this.time;
        this.frameTime += (float) passed;
        this.tickTime += (float) passed;

        this.time = time2;

        // Calculate tick interval based on server TPS
        float tickCap = 1000f / QuantumServer.TPS;

        // Process any accumulated tick time
        while (this.frameTime >= tickCap) {
            this.frameTime -= tickCap;
            this.partialTick = this.frameTime / tickCap;

            canTick = true;
        }

        // Execute client tick if enough time has passed
        if (canTick) {
            this.ticksPassed++;
            try {
                Timing.start("client_tick");
                this.clientTick();
                Timing.end("client_tick");
            } catch (ApplicationCrash e) {
                QuantumClient.crash(e.getCrashLog());
            } catch (Exception t) {
                CommonConstants.LOGGER.error("An error occurred while ticking the game.", t);
                var crashLog = new CrashLog("Game being ticked.", t);
                QuantumClient.crash(crashLog);
            }
        }

        // Update TPS counter every second
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
     * Renders the first frame.
     */
    private void firstRender() {
//        if (PlatformOS.isWindows) {
//            InputStream resourceAsStream = QuantumClient.class.getResourceAsStream("/assets/quantum/native/acrylic.dll");
//            try {
//                if (!Files.exists(Path.of(".", "acrylic.dll")))
//                    Files.copy(resourceAsStream, Path.of(".", "acrylic.dll"));
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
     * @param width    the width.
     * @param height   the height.
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
            GamePlatform.get().halt(1);
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
            GamePlatform.get().handleCrash(crash);
        } catch (Exception | OutOfMemoryError t) {
            QuantumClient.LOGGER.error(QuantumClient.FATAL_ERROR_MSG, t);

            if (instance != null) instance.shutdown(() -> {
            });
            GamePlatform.get().halt(1);
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
            disposable.shutdown(() -> {
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

        if (integratedServer != null && GamePlatform.get().isWeb()) {
            integratedServer.runTick();
        }

        // Update connection tick
        IConnection<ClientPacketHandler, ServerPacketHandler> connection = this.connection;
        if (connection != null) {
            connection.tick();
            // Update client connection tick
            this.connection.tick();
        }

//        if (this.controllerInput != null) this.controllerInput.tick();
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

            if (hit instanceof BlockHit) {
                BlockHit blockHitResult = (BlockHit) hit;
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
     * @param breaking  the breaking.
     * @param hitResult the hit result.
     */
    private void handleBlockBreaking(BlockVec breaking, BlockHit hitResult) {
        @Nullable ClientWorldAccess world = this.world;

        // Return early if world is null
        if (world == null) return;

        // Check if the block position or block meta has changed, or if player is null
        if (!hitResult.getBlockVec().equals(breaking.asBlockVec()) || !hitResult.getBlockMeta().equals(this.breakingBlock) || this.player == null) {
            // Reset breaking state if any condition fails
            this.resetBreaking(hitResult);
        } else {
            float efficiency = 1.0F;

            // Get the player's currently selected item stack
            ItemStack stack = this.player.getSelectedItem();

            // Get the item from the stack
            Item item = stack.getItem();

            // Check if the item is a tool and matches the effective tool type for the breaking block
            if (item instanceof ToolItem && this.breakingBlock.getEffectiveTool() == ((ToolItem) item).getToolType()) {
                ToolItem toolItem = (ToolItem) item;
                // Get the tool efficiency for faster breaking
                efficiency = toolItem.getEfficiency();
            }

            // Continue breaking the block with adjusted progress based on hardness and tool efficiency
            BreakResult breakResult = world.continueBreaking(breaking, 1.0F / (Math.max((this.breakingBlock.getHardness() / efficiency) * QuantumServer.TPS, 1) + 1), this.player);

            // Handle the result of the breaking attempt
            if (breakResult == BreakResult.FAILED) {
                // Reset breaking state if breaking failed
                this.resetBreaking();
            } else if (breakResult == BreakResult.BROKEN) {
                // Clear breaking state if block was broken
                this.breaking = null;
                this.breakingBlock = null;
            } else {
                // If the selected item has changed, reset brNULLeaking state
                if (this.oldSelected != this.player.selected) {
                    this.resetBreaking();
                }
                // Update oldSelected to current selected item
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

        // Return early if world, breaking block or player isn't available.
        if (this.world == null || this.breaking == null || player == null) return;

        // Stop breaking the current block
        this.world.stopBreaking(new BlockVec(this.breaking), player);

        // Get the block state at the hit result position
        BlockState block = hitResult.getBlockMeta();

        // Check if the block is null or air (non-solid)
        if (block == null || block.isAir()) {
            // Clear breaking references if the block is no longer valid
            this.breaking = null;
            this.breakingBlock = null;
        } else {
            // Update the breaking block position and state
            this.breaking = hitResult.getBlockVec();
            this.breakingBlock = block;

            // Start breaking the new block
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
        int expectedWidth = getWidth();
        int expectedHeight = getHeight();

        if (expectedWidth == 0 && expectedHeight == 0) {
            return;
        }

        this.cachedWidth = width;
        this.cachedHeight = height;

        // Set the projection matrix for the spriteBatch
        this.spriteBatch.getProjectionMatrix().setToOrtho(0, width, height, 0, 0, 1000000);

        // Update the deferred width and height values
        this.deferredWidth = width;
        this.deferredHeight = height;

        this.width = width;
        this.height = height;

        // Resize the renderer
        this.renderer.resize(width, height);

        this.autoScale = ClientConfiguration.guiScale.getValue() == 0;

        // Auto-scale the GUI if enabled
        if (this.autoScale) {
            this.guiScale = this.calcMaxGuiScale();
        } else {
            this.guiScale = Mth.clamp(ClientConfiguration.guiScale.getValue(), 1, calcMaxGuiScale());
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
     * Disposes of all game-related resources and performs necessary cleanup operations.
     * This method should only be invoked on the LibGDX main render thread.
     * <p>
     * Exceptions occurring during disposal will result in the client crashing to ensure
     * proper error handling and reporting. Misuse of this method outside the render thread
     * will throw an IllegalThreadError.
     */
    @Override
    public void dispose() {
        if (!QuantumClient.isOnRenderThread()) {
            throw new IllegalThreadError("Should only dispose on LibGDX main thread");
        }

        synchronized (this) {
            this.disposed = true;

            try {
                // Remove completed futures from the list
                while (!this.futures.isEmpty()) {
                    this.futures.removeIf(Promise::isDone);
                }

                // Cancel any ongoing vibration effects
                GameInput.cancelVibration();

                // Clean up integrated server resources
                QuantumClient.cleanUp((Shutdownable) this.integratedServer);

                // Clean up font resources
                QuantumClient.cleanUp(this.unifont);
                QuantumClient.cleanUp(this.font);
                QuantumClient.cleanUp(this.fontManager);

                // Clean up world and profiler
                QuantumClient.cleanUp(this.world);
                QuantumClient.cleanUp(this.profiler);

                // Notify platform that the game is disposing
                GamePlatform.get().onGameDispose();

                // Clean up various disposables
                this.disposables.forEach(QuantumClient::cleanUp);
                this.shutdownables.forEach(QuantumClient::cleanUp);
                this.closeables.forEach(QuantumClient::cleanUp);

                // Dispose renderers to free graphics resources
                QuantumClient.cleanUp(this.renderer);
                QuantumClient.cleanUp(this.gameRenderer);
                QuantumClient.cleanUp(this.itemRenderer);
                QuantumClient.cleanUp(this.worldRenderer);

                // Pop the main category from the stack
                mainCat.pop(this);

                // Clear different scene categories
                backgroundCat.clear();
                worldCat.clear();
                mainCat.clear();

                // Dispose model manager and baked models
                ModelManager.INSTANCE.dispose();
                BakedModelRegistry bakedBlockModels1 = this.bakedBlockModels;
                if (bakedBlockModels1 != null) QuantumClient.cleanUp(bakedBlockModels1.atlas());
                if (bakedBlockModels1 != null) QuantumClient.cleanUp(bakedBlockModels1);
                QuantumClient.cleanUp(this.entityModelManager);

                // Dispose mesh manager
                MeshManager.INSTANCE.dispose();

                // Dispose various textures
                QuantumClient.cleanUp(this.ultreonBgTex);
                QuantumClient.cleanUp(this.ultreonLogoTex);
                QuantumClient.cleanUp(this.libGDXLogoTex);
                QuantumClient.cleanUp(this.textureManager);

                // Dispose resource manager and skin manager
                QuantumClient.cleanUp((AutoCloseable) this.resourceManager);
                QuantumClient.cleanUp(this.skinManager);

                // Dispose cursor resources
                QuantumClient.cleanUp(this.normalCursor);
                QuantumClient.cleanUp(this.clickCursor);

                // Dispose connection resources
                QuantumClient.cleanUp(this.connection);

                // Fire client stopped event
                ClientLifecycleEvents.CLIENT_STOPPED.factory().onGameDisposed();

                // Suggest garbage collection to free memory
                System.gc();

                // Nuke all platform threads to ensure clean shutdown
                GamePlatform.get().nukeThreads();
            } catch (Exception t) {
                // Crash the client on any exception during disposal
                QuantumClient.crash(t);

                GamePlatform.get().halt(1);
            }
        }
    }

    /**
     * Gets the width of the game.
     *
     * @return the width of the game.
     */
    public int getWidth() {
        GameInsets insets = GamePlatform.get().getInsets();
        if (insets.right == 0) return Gdx.graphics.getWidth();
        return GamePlatform.get().isShowingImGui() ? insets.right : Gdx.graphics.getWidth();
    }

    /**
     * Gets the height of the game.
     *
     * @return the height of the game.
     */
    public int getHeight() {
        GameInsets insets = GamePlatform.get().getInsets();
        if (insets.bottom == 0) return Gdx.graphics.getHeight();
        return GamePlatform.get().isShowingImGui() ? insets.bottom : Gdx.graphics.getHeight();
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
    public void startWorld(FileHandle path) {
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
     * Handles the sequence of exiting the current game world and performing subsequent actions.
     * This method ensures proper cleanup of resources, including server, network connections,
     * and rendering components, and then executes the specified callback once the world-exit
     * process is complete.
     *
     * @param afterExit a {@link Runnable} to be executed after the world exit and cleanup process is finished
     */
    public void exitWorldAndThen(Runnable afterExit) {
        this.closingWorld = true;
        this.renderWorld = false;

        final @Nullable TerrainRenderer worldRenderer = this.worldRenderer;
        
        // Display a message screen indicating the world is being saved
        this.showScreen(new MessageScreen(TextObject.translation("quantum.screen.message.saving_world"))); // "Saving world..."

        // Run the following code asynchronously
        Promise.runAsync(() -> {
            try {
                // Attempt to close the network connection
                this.connection.close();
            } catch (IOException e) {
                // Crash the client if closing connection fails
                QuantumClient.crash(e);
                return;
            }

            IntegratedServer server = integratedServer;
            if (server != null)
                // Remove the integrated server if it exists
                this.remove(integratedServer);

            // Clean up the integrated server resources
            QuantumClient.cleanUp((Shutdownable) this.integratedServer);

            // Clear the server tick queue
            this.serverTickQueue.clear();

            try {
                QuantumClient.invoke(() -> {
                    // Clean up the world renderer and world resources
                    QuantumClient.cleanUp(worldRenderer);
                    QuantumClient.cleanUp(this.world);
                    // Disable rendering and nullify references
                    this.renderWorld = false;
                    this.worldRenderer = null;
                    this.world = null;
                    this.integratedServer = null;
                    this.player = null;

                    // Cancel any ongoing game input vibrations
                    GameInput.cancelVibration();

                    // Run the after-exit callback
                    afterExit.run();
                });
            } catch (Exception e) {
                // Crash the client if any exception occurs during cleanup
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
            // Check if the window close event is canceled
            return false;
        }

        // Trigger window closed event (uncancellable)
        ClientLifecycleEvents.WINDOW_CLOSED.factory().onWindowClose();

        // If a close prompt is enabled and there is a screen, show a confirmation dialog
        if (ClientConfiguration.closePrompt.getValue() && this.screen != null) {
            this.screen.showDialog(new DialogBuilder(this.screen).message(TextObject.literal("Are you sure you want to close the game?")).button(UITranslations.YES, () -> {
                if (this.world != null) {
                    // Exit the world before shutting down if a world is open
                    this.exitWorldAndThen(() -> shutdown(() -> {
                    }));
                    return;
                }

                // Shutdown directly if no world is open
                this.shutdown(() -> {
                });
            }));
            return false;
        }

        // If there is a world, exit it then shutdown
        if (this.world != null) {
            this.exitWorldAndThen(() -> shutdown(() -> {
            }));
            return false;
        }

        // Run shutdown asynchronously if no world and no prompt
        Promise.runAsync(() -> shutdown(() -> {
        }));

        // Prevent default close handling
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
    public void addFuture(Promise<?> future) {
        this.futures.add(future);
    }

    /**
     * Gets the block model.
     *
     * @param block the block.
     * @return the block model.
     */
    public @NotNull BlockModel getBlockModel(BlockState block) {
        return QuantumClient.invokeAndWait(() -> {
            BlockModel blockModel = BlockModelRegistry.get().get(block);
            return Objects.requireNonNullElse(blockModel, BakedCubeModel.defaultModel());
        });
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

        if (!(hit instanceof BlockHit)) return;
        BlockHit blockHitResult = (BlockHit) hit;

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

        if (!(hit instanceof BlockHit)) {
            this.breaking = null;
            this.breakingBlock = null;
            return;
        }
        BlockHit blockHitResult = (BlockHit) hit;

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

        // Check if the hit is not a block hit
        if (!(hit instanceof BlockHit)) {
            // Reset breaking state if not hitting a block
            this.breaking = null;
            this.breakingBlock = null;
            return;
        }
        BlockHit blockHitResult = (BlockHit) hit;

        // Return early if world, player, or breaking state is null
        if (this.world == null || player == null || this.breaking == null) return;

        // Stop breaking the block at the hit position
        this.world.stopBreaking(new BlockVec(blockHitResult.getBlockVec()), player);

        // Reset breaking state after stopping
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

        // Compare width ratio and height ratio to determine the scaling factor
        if (windowWidth / QuantumClient.MINIMUM_WIDTH < windowHeight / QuantumClient.MINIMUM_HEIGHT) {
            // If the width ratio is smaller, return the max of width ratio or 1
            return Math.max(windowWidth / QuantumClient.MINIMUM_WIDTH, 1);
        }

        if (windowHeight / QuantumClient.MINIMUM_HEIGHT < windowWidth / QuantumClient.MINIMUM_WIDTH) {
            // If the height ratio is smaller, return the max of height ratio or 1
            return Math.max(windowHeight / QuantumClient.MINIMUM_HEIGHT, 1);
        }

        // If both ratios are equal, take the minimum and ensure it is at least 1
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
        return this.offset.set(0, 0);
    }

    /**
     * Gets the mouse position.
     *
     * @return the mouse position.
     */
    public GridPoint2 getMousePos() {
        GameInsets insets = GamePlatform.get().getInsets();
        return GamePlatform.get().isShowingImGui() && !Gdx.input.isCursorCatched()
                ? this.offset.set(insets.left, insets.top)
                : this.offset.set(Gdx.input.getX(), Gdx.input.getY());
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
     * Checks if the game is in singleplayer.
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
     * @param volume     the volume.
     */
    public void playSound(@NotNull SoundEvent soundEvent, float volume) {
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
        // Connect to the local server and unwrap the connection object
        var mem = ClientTcpConnection.connectToLocalServer().unwrap();
        this.connection = mem;
        MemoryConnectionContext.set(mem);

        // Create a new client world for the overworld dimension
        this.world = new ClientWorld(this, DimensionInfo.OVERWORLD);
        this.mainCat.add("Client World", this.world);

        // Start the integrated server instance
        this.integratedServer.start();

        // Set the other side of the memory connection to the server's network connection
        mem.setOtherSide((MemoryConnection<ServerPacketHandler, ClientPacketHandler>) this.integratedServer.getNetworker().getConnections().get(0));

        // Initialize multiplayer data for the client
        this.multiplayerData = new MultiplayerData(this);

        // Initiate the connection with the login packet handler and login packet
        this.connection.initiate(new LoginClientPacketHandlerImpl(this.connection), new C2SLoginPacket(this.user.name()));    }

    /**
     * Connects to a server.
     *
     * @param location The webserver location (e.g., wss://play.example.com:38800)
     */
    public void connectToServer(String location) {
        // Initialize the overworld client world
        this.world = new ClientWorld(this, DimensionInfo.OVERWORLD);

        // Establish connection to the server
        this.connection = ClientWebSocketConnection.connectToServer(this, location + "/server", () -> {
            var conn = this.connection;
            if (conn == null) return;

            // Initialize remote connection and multiplayer data
            this.multiplayerData = new MultiplayerData(this);

            // Begin the login handshake with server using client packet handler and login packet
            conn.initiate(new LoginClientPacketHandlerImpl(conn), new C2SLoginPacket(this.user.name()));
        }, e -> {
            // Log error and handle disconnection on failure to connect
            CommonConstants.LOGGER.error(
                    "Failed to connect to " + location + ":", e);
            GamePlatform.get().handleDisconnect(e);
            Throwable cause = e.getCause();
            if (cause != null) {
                // Show disconnect screen with cause if available
                this.showScreen(new DisconnectedScreen("Failed to connect!\n" + cause, true));
                return;
            }
            // Show disconnect screen with exception message
            this.showScreen(new DisconnectedScreen("Failed to connect!\n" + e, true));
            connection = null;
        }).map(Function.identity(), e -> {
            // Log error and display disconnect screen if map operation fails
            CommonConstants.LOGGER.error("Failed to connect:", e);
            Throwable cause = e.getCause();
            if (cause != null) {
                // Show a disconnect screen with cause if present
                this.showScreen(new DisconnectedScreen("Failed to connect!\n" + cause, true));
                return null;
            }

            // Show a disconnect screen with the exception message
            this.showScreen(new DisconnectedScreen("Failed to connect!\n" + e, true));
            connection = null;
            return null;
        }).getValueOrNull();
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
            // Check if the desired fullscreen state is different from the current state
            if (fullScreen) {
                // Save the current window size before switching to fullscreen
                this.oldMode = new Vec2i(this.getWidth(), this.getHeight());

                // Set the display mode to fullscreen using the current display's mode
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                // Restore the windowed mode using the previously saved window size
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
        return ClientConfiguration.enableDebugUtils.getValue();
    }

    /**
     * Sets whether the debug HUD is shown.
     *
     * @param showDebugHud whether the debug HUD is shown.
     */
    public void setShowDebugHud(boolean showDebugHud) {
        ClientConfiguration.enableDebugUtils.setValue(showDebugHud);
        this.newConfig.save();
    }

    /**
     * Reloads the resources asynchronously.
     */
    public void reloadResourcesAsync() {
        if (!isOnRenderThread()) {
            // If not on the render thread, schedule resource reload on the render thread and return
            invokeAndWait(this::reloadResourcesAsync);
            return;
        }

        // Initialize the loading overlay and mark loading as true
        this.loadingOverlay = new LoadingOverlay();
        loading = true;

        // Run resource reloading asynchronously
        Promise.runAsync(() -> {
            LOGGER.info("Reloading resources...");

            // Perform the actual resource reload
            this.reloadResources();
            LOGGER.info("Resources reloaded.");

            // Mark loading as complete and clear the loading overlay
            this.loading = false;
            this.loadingOverlay = null;
        }).exceptionally(throwable -> {
            // Log any errors that occur during resource reload
            LOGGER.error("Failed to reload resources:", throwable);
            return null;
        });
    }

    public void reloadResources() {
        // Create a reload context using the current instance and resource manager
        ReloadContext context = ReloadContext.create(this, this.resourceManager);

        // Reload all resources from the resource manager
        this.resourceManager.reload();

        // Reload textures, cubemaps and materials using the context.
        this.textureManager.reload(context);
        this.cubemapManager.reload(context);
        this.materialManager.reload(context);

        // Log information about sound initialization
        QuantumClient.LOGGER.info("Initializing sounds");

        // Reload sound & music
        this.soundRegistry.reload();
        MusicManager.get().reload();

        // Reload block models with resource manager and context
        BlockModelRegistry.get().reload(resourceManager, context);
        ItemModelRegistry.get().reload(resourceManager, context);

        // Register rendering components for the current instance
        RenderingRegistration.registerRendering(this);

        // Reload the managers with resource manager and context
        this.entityModelManager.reload(this.resourceManager, context);
        this.entityRendererManager.reload(this.resourceManager, context);
        this.textureAtlasManager.reload(context);
        this.shaderProgramManager.reload(context);
        this.shaderProviderManager.reload(context);
        this.skinManager.reload();

        // If a world renderer exists, reload it with context and material manager
        if (this.worldRenderer != null) {
            this.worldRenderer.reload(context, materialManager);
        }

        // Wait until the reload context signals completion, yielding control in the meantime
        while (!context.isDone()) {
            GamePlatform.get().yield();
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
    public void onDisconnect(String message, boolean isMemoryConnection) {
        Registries.unload();

        try {
            var conn = this.connection;
            if (conn != null) conn.close();
        } catch (IOException e) {
            CommonConstants.LOGGER.warn("Failed to close connection", e);
        }
        this.connection = null;
        IntegratedServer server = this.integratedServer;
        QuantumClient.invokeAndWait(() -> {
            if (this.screen instanceof TitleScreen) return;
            this.showScreen(new MessageScreen(TextObject.translation("quantum.message.disconnecting"), TextObject.translation("quantum.message.disconnecting.desc")));
        });
        if (server != null) {
            server.shutdown(() -> {
                this.showScreen(new DisconnectedScreen(message, !isMemoryConnection));
            });
        }
    }

    /**
     * Cycles the player view.
     */
    public void cyclePlayerView() {
        switch (this.playerView) {
            case FIRST_PERSON:
                this.playerView = PlayerView.THIRD_PERSON;
                break;
            case THIRD_PERSON:
                this.playerView = PlayerView.THIRD_PERSON_FRONT;
                break;
            case THIRD_PERSON_FRONT:
                this.playerView = PlayerView.FIRST_PERSON;
                break;
            default:
                throw new IllegalArgumentException();
        }
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
        return cfgScreenFactories.get(caller.getId());
    }

    /**
     * Sets the mod config screen.
     *
     * @param caller  the mod that to get the config screen for.
     * @param factory the factory to set as the config screen for the mod.
     */
    public void setModConfigScreen(Mod caller, ConfigScreenFactory factory) {
        cfgScreenFactories.put(caller.getId(), factory);
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
     * Shuts down the client, waiting for all tasks to complete.
     */
    @Override
    public void shutdown(Runnable finalizer) {
        if (this.shuttingDown) return;
        this.shuttingDown = true;
        super.shutdown(finalizer);

        LOGGER.info("Shutting down executor service");
        executor.dispose();

        IntegratedServer server = this.integratedServer;
        if (server != null) {
            if (this.screen instanceof ShutdownScreen) {
                ShutdownScreen shutdownScreen;
                shutdownScreen = (ShutdownScreen) this.screen;
                shutdownScreen.setMessage("Waiting for server to terminate");
            }
            LOGGER.info("Waiting for server to terminate");
            server.shutdown(() -> {
                if (this.screen instanceof ShutdownScreen) {
                    ShutdownScreen shutdownScreen;
                    shutdownScreen = (ShutdownScreen) this.screen;
                    shutdownScreen.setMessage("Finalizing...");
                }
            });
        }

        CommonConstants.LOGGER.info("Shutting down RPC handler");
        RpcHandler.disable();

        LOGGER.info("Shutting down Quantum Client");
        Gdx.app.exit();
    }

    /**
     * Updates the viewport.
     */
    public void updateViewport() {
        if (!insets.equals(GamePlatform.get().getInsets())) {
            resize(QuantumClient.get().getWidth(), QuantumClient.get().getHeight());
            insets.set(GamePlatform.get().getInsets());
        }
    }

    /**
     * Runs a task asynchronously.
     *
     * @param o the task.
     * @return the future.
     */
    public Promise<Void> runAsyncTask(Runnable o) {
        return Promise.runAsync(o, executor);
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

    public TextureAtlas getAtlas(NamespaceID id) {
        return textureAtlasManager.get(id);
    }

    public @Nullable ItemModel getItemModel(Item item) {
        return ItemModelRegistry.get().get(item);
    }

    public Screenshots getScreenshots() {
        return screenshots;
    }

    public void setDebugOverlayShown(boolean debugOverlayShown) {
        this.debugOverlayShown = debugOverlayShown;
    }

    public boolean isDebugOverlayShown() {
        return debugOverlayShown;
    }

    public void setGraphicsSetting(GraphicsSetting setting) {
        this.graphicsSetting = setting;
        WorldRenderer currentWorldRenderer = this.worldRenderer;
        if (currentWorldRenderer != null) currentWorldRenderer.reloadFully();
    }
}
