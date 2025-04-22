package dev.ultreon.quantum.teavm;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.ultreon.quantum.client.Main;
import org.jetbrains.annotations.Nullable;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSExceptions;
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
        batch = new SpriteBatch();
        font = new BitmapFont();
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

    void crash(Throwable e) {
        JSError.catchNative(() -> {
            crash = getJSStack(e);
            return null;
        }, t -> {
            crash = getJSStackNative(e);
            return null;
        });
        exceptionMessage = e.getClass().getName() + ": " + e.getMessage();
        quantum = null;
    }

    private void crash(JSObject e) {
        JSError.catchNative(() -> {
            crash = getJSStackNative(e);
            return null;
        }, t -> {
            crash = getJSStackNative(e);
            return null;
        });
        quantum = null;
    }

    @Override
    public void render() {
        if (ended) return;

        ScreenUtils.clear(0, 0, 0, 1);
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
            batch.begin();
            if (Gdx.graphics == null) {
                System.out.println(exceptionMessage);
                Console.error(crash);
                batch.end();
                ended = true;
                return null;
            }
            int y = Gdx.graphics.getHeight();
            if (font == null) {
                font = new BitmapFont();
            }
            y -= (int) (font.getLineHeight() + 2);
            for (String element : crash.split("(\\n|\\r|\\r\\n)")) {
                y -= (int) (font.getLineHeight() + 2);
                font.draw(batch, element, 0, y);
            }
            batch.end();
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
