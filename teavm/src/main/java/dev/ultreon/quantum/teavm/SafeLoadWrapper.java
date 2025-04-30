package dev.ultreon.quantum.teavm;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import dev.ultreon.quantum.client.Main;
import dev.ultreon.quantum.crash.ApplicationCrash;
import org.jetbrains.annotations.Nullable;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSError;

public class SafeLoadWrapper implements ApplicationListener {
    private Main quantum;
    private SpriteBatch batch;
    private BitmapFont font;
    private String crash;
    private String exceptionMessage;
    private final String[] args;
    private boolean ended;

    public SafeLoadWrapper(String[] args) {
        this.args = args;
    }

    @Override
    public void create() {
        TeaApplicationConfiguration config = ((TeaApplication) Gdx.app).getConfig();
        Console.log("Config: ", config);

        batch = new SpriteBatch();
        font = new BitmapFont();

        batch.setTransformMatrix(batch.getTransformMatrix().scl(Gdx.graphics.getBackBufferScale(), Gdx.graphics.getBackBufferScale(), 1));

        Gdx.input.setCatchKey(Input.Keys.ESCAPE, true);
        Gdx.input.setCatchKey(Input.Keys.BACKSPACE, true);
        Gdx.input.setCatchKey(Input.Keys.F12, true);
        Gdx.input.setCatchKey(Input.Keys.F11, true);
        Gdx.input.setCatchKey(Input.Keys.F10, true);
        Gdx.input.setCatchKey(Input.Keys.F9, true);
        Gdx.input.setCatchKey(Input.Keys.F7, true);
        Gdx.input.setCatchKey(Input.Keys.F3, true);
        Gdx.input.setCatchKey(Input.Keys.F1, true);
        Gdx.input.setCatchKey(Input.Keys.SYM, true);
        Gdx.input.setCatchKey(Input.Keys.SPACE, true);

        try {
            quantum = Main.createInstance(args);
            quantum.create();
        } catch (Throwable e) {
            crash(e);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (quantum != null) {
            try {
                quantum.resize(width, height);
            } catch (Throwable e) {
                crash(e);
            }
        }
    }

    @JSBody(script = "return t.$jsException.stack;", params = {"t"})
    public static native String getJSStack(Object o);

    @JSBody(script = "return t.stack;", params = {"t"})
    public static native String getJSStackNative(Object o);

    void crash(ApplicationCrash e) {
        if (crash != null) return;

        JSError.catchNative(() -> {
            crash = e.toString();
            Console.error(crash);
            CrashOverlay.createOverlay(crash);
            return null;
        }, t -> {
            crash = getJSStack(e);
            Console.error(crash);
            CrashOverlay.createOverlay(crash);
            return null;
        });
        exceptionMessage = e.getClass().getName() + ": " + e.getMessage();
        quantum = null;
    }

    void crash(Throwable e) {
        if (crash != null) return;

        JSError.catchNative(() -> {
            crash = getJSStack(e);
            Console.error(crash);
            CrashOverlay.createOverlay(crash);
            return null;
        }, t -> {
            crash = getJSStackNative(e);
            Console.error(crash);
            CrashOverlay.createOverlay(crash);
            return null;
        });
        exceptionMessage = e.getClass().getName() + ": " + e.getMessage();
        quantum = null;
    }

    void crash(JSObject e) {
        if (crash != null) return;

        JSError.catchNative(() -> {
            crash = getJSStackNative(e);
            Console.error(crash);
            CrashOverlay.createOverlay(crash);
            return null;
        }, t -> {
            crash = getJSStackNative(t);
            Console.error(crash);
            CrashOverlay.createOverlay(crash);
            return null;
        });
        quantum = null;
    }

    @Override
    public void render() {
        if (ended) return;

        JSError.catchNative(this::unsafeRender, this::handleCrash);
    }

    private @Nullable Object handleCrash(JSObject e) {
        if (crash != null) return null;
        JSError.catchNative(() -> {
            if (batch.isDrawing()) batch.end();
            return null;
        }, t -> {
            if (batch.isDrawing()) batch.end();
            crash(t);
            return null;
        });
        if (batch.isDrawing()) batch.end();
        crash(e);
        return null;
    }

    private @Nullable Object unsafeRender() {
        if (crash != null) {
            return null;
        }
        if (quantum != null) {
            quantum.render();
        }
        return null;
    }

    @Override
    public void pause() {
        if (quantum != null) {
            try {
                quantum.pause();
            } catch (Throwable e) {
                crash(e);
            }
        }
    }

    @Override
    public void resume() {
        if (quantum != null) {
            try {
                quantum.resume();
            } catch (Throwable e) {
                crash(e);
            }
        }
    }

    @Override
    public void dispose() {
        if (quantum != null) {
            try {
                quantum.dispose();
            } catch (Throwable e) {
                crash(e);
            }
        }
    }

}
