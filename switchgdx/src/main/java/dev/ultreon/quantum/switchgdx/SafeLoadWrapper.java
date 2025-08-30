package dev.ultreon.quantum.switchgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.ultreon.quantum.client.Main;

public class SafeLoadWrapper implements ApplicationListener {
    private Main quantum;
    private SpriteBatch batch;
    private BitmapFont font;
    private StackTraceElement[] crash;
    private String exceptionMessage;
    private SafeLoadWrapper safeWrapper;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        try {
            safeWrapper = new SafeLoadWrapper();
            new SwitchPlatform(safeWrapper);

            this.quantum = Main.createInstance(new String[0]);
            this.quantum.create();
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

    void crash(Throwable e) {
        this.crash = e.getStackTrace();
        exceptionMessage = e.getClass().getName() + ": " + e.getMessage();
        quantum = null;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        try {
            if (crash != null) {
                batch.begin();
                if (Gdx.graphics == null) {
                    System.out.println(exceptionMessage);
                    for (StackTraceElement element : crash) {
                        System.out.println("    at " + element.getClassName() + "." + element.getMethodName() + " (" + element.getFileName() + ".class" + ":" + element.getLineNumber() + ")");
                    }
                    batch.end();
                    System.exit(1);
                    return;
                }
                int y = Gdx.graphics.getHeight();
                if (font == null) {
                    font = new BitmapFont();
                }
                y -= (int) (font.getLineHeight() + 2);
                font.draw(batch, String.valueOf(exceptionMessage), 0, y);
                for (StackTraceElement element : crash) {
                    if (element == null) continue;
                    y -= (int) (font.getLineHeight() + 2);
                    font.draw(batch, "    at " + element.getClassName().replace('/', '.') + "." + element.getMethodName() + " (" + (element.getFileName() != null ? element.getFileName().substring(element.getFileName().lastIndexOf("/") + 1) + ".class" : "<Unknown File>") + ":" + element.getLineNumber() + ")", 0, y);
                }
                batch.end();
                return;
            }
            if (quantum != null) {
                quantum.render();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (batch.isDrawing()) batch.end();
            crash(e);
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

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
