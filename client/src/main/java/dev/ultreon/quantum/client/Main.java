package dev.ultreon.quantum.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.ultreon.libs.commons.v0.util.StringUtils;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import kotlin.OptIn;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.badlogic.gdx.graphics.profiling.GLInterceptor.resolveErrorNumber;

/**
 * LibGDX wrapper for Quantum Voxel to handle uncaught exceptions.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@ApiStatus.Internal
@OptIn(markerClass = InternalApi.class)
public final class Main implements ApplicationListener {
    private static Main instance;
    private static CrashLog crashOverride;
    private final String[] argv;
    @Nullable
    private DesktopMain client;
    private long crashFrame;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GLProfiler glProfiler;
    private final Logger logger = LoggerFactory.getLogger("GAME");
    private GameWindow window;
    private boolean windowVibrancyEnabled;

    /**
     * Constructs a new GameLibGDXWrapper object.
     *
     * @param argv The command line arguments.
     */
    private Main(String[] argv) {
        this.argv = argv;

        if (instance == null) {
            instance = this;
        }
    }

    static void displayCrash(ApplicationCrash crash) {
        crashOverride = crash.getCrashLog();

        while (ScissorStack.peekScissors() != null) ScissorStack.popScissors();

        CommonConstants.LOGGER.error("\n" + crash);
    }

    public static Main createInstance(String[] argv) {
        if (instance == null) {
            instance = new Main(argv);
        }
        return instance;
    }

    /**
     * Handles uncaught exceptions.
     * If the exception is an ApplicationCrash, delays the crash log processing using QuantumClient.
     * Logs the exception otherwise.
     *
     * @param thread    The thread where the exception occurred
     * @param throwable The uncaught exception
     */
    private void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable instanceof ApplicationCrash e) {
            try {
                CrashLog crashLog = e.getCrashLog();
                QuantumClient.get().delayCrash(crashLog);
                return;
            } catch (Throwable t) {
                CommonConstants.LOGGER.error("Failed to handle uncaught exception", t);
            }
        }
        CommonConstants.LOGGER.error("Uncaught exception", throwable);
    }

    /**
     * Initializes the QuantumClient and sets up exception handlers.
     */
    @Override
    public void create() {
        if (client != null) return;

        if (GamePlatform.get().isDevEnvironment()) {
            glProfiler = new GLProfiler(Gdx.graphics);
            glProfiler.setListener(error -> {
                String stackTrace = ExceptionUtils.getStackTrace(new Exception());
                Gdx.app.error("GLProfiler", "Error " + resolveErrorNumber(error) + " at:\n" + stackTrace);
            });
        }

        try {
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();

            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/roboto-mono.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 15;

            font = generator.generateFont(parameter);

            // Set log level to debug
            Gdx.app.setLogLevel(Application.LOG_DEBUG);

            // Check for datagen system property
            if (Objects.equals(System.getProperty("quantum.datagen"), "true")) {
                this.client = new DataGeneratorClient();
            }

            // Initialize QuantumClient with given arguments
            this.client = new QuantumClient(this.argv);
        } catch (ApplicationCrash t) {
            // Handle ApplicationCrash exception
            QuantumClient.crash(t);
        }
    }

    /**
     * Resizes the client if it is not null.
     *
     * @param width  the new width
     * @param height the new height
     */
    @Override
    public void resize(int width, int height) {
        if (this.client != null) {
            this.client.resize(width, height);
        }

        if (batch != null) {
            Matrix4 matrix4 = batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
            batch.setProjectionMatrix(matrix4);
            if (shapeRenderer != null) {
                shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            }
        }

    }

    /**
     * Renders the client if it is not null, handling any ApplicationCrash exceptions.
     */
    @Override
    public void render() {
        try {
            if (crashOverride != null) {
                CrashLog crashLog = crashOverride;
                this.renderCrash(crashLog);
                return;
            }

            if (this.client != null) {
                this.client.render();
            }
        } catch (ApplicationCrash e) {
            CrashLog crashLog = e.getCrashLog();
            QuantumClient.crash(crashLog);
        }
    }

    /**
     * Renders the crash screen with the provided crash log.
     * Clears the screen, displays crash log, and handles input for copying crash log to clipboard or uploading to GitHub.
     *
     * @param crashLog The crash log to be displayed.
     */
    private void renderCrash(CrashLog crashLog) {
        // Clear any active scissors
        while (ScissorStack.peekScissors() != null) ScissorStack.popScissors();

        // Set window mode if currently in fullscreen
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1280, 720);
            Gdx.graphics.setResizable(false);
            Gdx.graphics.setVSync(false);
        }

        // Set crash frame if not set
        if (this.crashFrame == 0) {
            this.crashFrame = Gdx.graphics.getFrameId();
        }

        // Clear the screen
        ScreenUtils.clear(0, 0, 0, 1, true);

        // Render filled shapes for crash screen
        this.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        this.shapeRenderer.setColor(Color.BLACK);
        this.shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.shapeRenderer.setColor(Color.RED);
        this.shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), 2);
        this.shapeRenderer.rect(0, Gdx.graphics.getHeight() - 2, Gdx.graphics.getWidth(), 2);
        this.shapeRenderer.end();

        // Begin rendering crash log text
        this.batch.begin();
        this.batch.setColor(Color.WHITE);

        // Split crash log into lines and render each line
        List<String> string = StringUtils.splitIntoLines(crashLog.toString().replace("\t", "    "));
        for (int i = 0; i < string.size(); i++) {
            String line = string.get(i);
            this.font.draw(this.batch, line, 10, Gdx.graphics.getHeight() - 30 - i * (this.font.getLineHeight() + 2));
        }

        this.batch.end();

        // Handle input for copying or uploading crash log
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            if (Gdx.input.isKeyPressed(Input.Keys.C)) {
                Clipboard clipboard = Gdx.app.getClipboard();
                clipboard.setContents(crashLog.toString());
            } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                //String string1 = crashLog.toString();
                // TODO: Implement GitHub issue upload.
            }
        }
    }

    /**
     * Pauses the client if it is not null and handles any ApplicationCrash exceptions.
     */
    @Override
    public void pause() {
        try {
            if (this.client != null) {
                this.client.pause();
            }
        } catch (ApplicationCrash e) {
            CrashLog crashLog = e.getCrashLog();
            QuantumClient.crash(crashLog);
        }
    }

    /**
     * Resumes the client.
     */
    @Override
    public void resume() {
        try {
            // Check if the client is not null before resuming
            if (this.client != null) {
                this.client.resume();
            }
        } catch (ApplicationCrash e) {
            // If an ApplicationCrash exception occurs, handle it by logging the crash
            CrashLog crashLog = e.getCrashLog();
            QuantumClient.crash(crashLog);
        }
    }

    /**
     * Clean up resources and handle any potential crashes.
     */
    @Override
    public void dispose() {
        try {
            if (this.glProfiler != null) {
                this.glProfiler.disable();
                logger.info("GL Draw Calls = {}", this.glProfiler.getDrawCalls());
                logger.info("GL Calls = {}", this.glProfiler.getCalls());
                logger.info("Shader Switches = {}", this.glProfiler.getShaderSwitches());
                logger.info("Texture Bindings = {}", this.glProfiler.getTextureBindings());
                logger.info("Max Vertex Count = {}", this.glProfiler.getVertexCount().max);
                logger.info("Average Vertex Count = {}", this.glProfiler.getVertexCount().average);
            }

            // Dispose the client if it exists
            if (this.client != null) this.client.dispose();
        } catch (ApplicationCrash e) {
            // Handle the application crash
            CrashLog crashLog = e.getCrashLog();
            QuantumClient.crash(crashLog);
        }
    }

    public static GLProfiler getGlProfiler() {
        return instance.glProfiler;
    }

    public GameWindow getWindow() {
        return window;
    }

    public boolean isWindowVibrancyEnabled() {
        return windowVibrancyEnabled;
    }
}
