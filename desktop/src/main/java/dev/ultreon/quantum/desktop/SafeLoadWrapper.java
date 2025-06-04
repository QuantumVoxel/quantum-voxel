package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserListener;
import dev.ultreon.libs.commons.v0.util.ExceptionUtils;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.Main;
import dev.ultreon.quantum.client.util.Utils;
import dev.ultreon.quantum.crash.ApplicationCrash;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.time.format.DateTimeFormatter;

public class SafeLoadWrapper implements ApplicationListener {
    private Main quantum;
    private SpriteBatch batch;
    private String crash;
    private final String[] args;
    private boolean ended;
    private Texture texture;
    private ShapeDrawer shapes;
    private Screen currentScreen = new ScreenAdapter();
    private InputProcessor inputProcessor;
    private TextureRegion whitePixel;

    public SafeLoadWrapper(String[] args) {
        this.args = args;
    }

    @Override
    public void create() {
        VisUI.load();

        batch = new SpriteBatch();
        whitePixel = createWhitePixel();
        shapes = new ShapeDrawer(batch, whitePixel);

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

    private TextureRegion createWhitePixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGB565);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(tex);
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

        if (currentScreen != null) {
            currentScreen.resize(width, height);
        }
    }

    void crash(Throwable e) {
        if (crash != null) return;
        try {
            CommonConstants.LOGGER.error("Game Crashed:", e);
        } catch (Throwable t) {
            e.addSuppressed(t);
            e.printStackTrace();
        }
        crash = ExceptionUtils.getStackTrace(e).replace("\t", "    ");
        quantum = null;

        Gdx.app.postRunnable(() -> openScreen(new CrashScreen()));
    }

    void crash(ApplicationCrash crashLog) {
        if (crash != null) return;
        crash = crashLog.toString();
        quantum = null;

        Gdx.app.postRunnable(() -> openScreen(new CrashScreen()));
    }

    void openScreen(ScreenAdapter screen) {
        if (screen == null) {
            if (this.currentScreen == null) return;
            currentScreen.hide();
            this.currentScreen = null;
            Gdx.input.setInputProcessor(inputProcessor);
            inputProcessor = null;
        } else if (this.currentScreen == null) {
            this.currentScreen = screen;
            this.inputProcessor = Gdx.input.getInputProcessor();
            currentScreen.show();
        } else {
            currentScreen.hide();
            this.currentScreen = screen;
            currentScreen.show();
        }
    }

    void closeScreen() {
        openScreen(null);
    }

    @Override
    public void render() {
        this.unsafeRender();
    }

    private void unsafeRender() {
        if (crash != null) {
            if (currentScreen != null)
                currentScreen.render(Gdx.graphics.getDeltaTime());
            return;
        }
        try {
            if (quantum != null) {
                quantum.render();
            }
        } catch (Throwable t) {
            Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.app.getGraphics();
            graphics.getWindow().setVisible(true);
            this.crash(t);
        }
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

        if (texture != null) texture.dispose();
        batch.dispose();

        if (whitePixel != null) whitePixel.getTexture().dispose();
    }

    private class CrashScreen extends ScreenAdapter {
        private final ScreenViewport viewport = new ScreenViewport();
        private final Stage stage = new Stage(viewport, batch);
        private final Window window;

        public CrashScreen() {
            viewport.setUnitsPerPixel(Gdx.graphics.getBackBufferScale());

            stage.addActor(new Background());
            window = new VisWindow("Game Crashed");
            window.setResizable(true);
            window.setMovable(false);
            window.setSize(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
            window.setPosition(Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 4f);
            stage.addActor(window);

            // Root table inside a window or stage
            VisTable table = new VisTable();
            table.setFillParent(true); // optional, if added to stage
            table.defaults().pad(10);  // optional padding

            VisTextArea textArea = new VisTextArea(crash, Utils.make(new VisTextField.VisTextFieldStyle(VisUI.getSkin().get(VisTextField.VisTextFieldStyle.class)), visTextFieldStyle -> {
                visTextFieldStyle.disabledFontColor = Color.WHITE;
            }));
            textArea.setDisabled(true);
            textArea.setPrefRows(crash.lines().count()); // Makes it taller than the ScrollPane

            VisScrollPane scrollPane = new VisScrollPane(textArea);
            scrollPane.setFadeScrollBars(false); // Optional: show scrollbars always
            scrollPane.setScrollingDisabled(false, false); // Allow both scroll directions

            textArea.setFillParent(false);

            table.add(scrollPane).expand().fill().pad(10).row();

            VisTable buttons = new VisTable();
            buttons.right().defaults().pad(5);
            VisTextButton button = new VisTextButton("Copy");
            button.setClip(true);

            button.addListener(new CopyClickListener(textArea));

            buttons.add(button);

            VisTextButton saveButton = new VisTextButton("Save As...");
            saveButton.setClip(true);

            saveButton.addListener(new SaveClickListener(textArea));

            buttons.add(saveButton);
            table.add(buttons).bottom().fillX().expandX().pad(5);

            window.add(table).fill().expand().pad(10);
        }

        @Override
        public void resize(int width, int height) {
            super.resize(width, height);
            stage.getViewport().update(width, height, true);
        }

        @Override
        public void show() {
            super.show();

            Gdx.input.setInputProcessor(stage);
        }

        @Override
        public void render(float delta) {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            window.setPosition(Gdx.graphics.getWidth() / 2f - window.getWidth() / 2f, Gdx.graphics.getHeight() / 2f - window.getHeight() / 2f);

            stage.act(delta);
            stage.draw();
        }

        private class Background extends Actor {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, 0.3f);

                if (texture != null) batch.draw(texture, 0, 0, stage.getWidth(), stage.getHeight());

                batch.setColor(1, 1, 1, 0.3f);
                batch.draw(whitePixel, 0, 0, stage.getWidth(), stage.getHeight());
                batch.setColor(1, 1, 1, 1f);
            }
        }

        private class CopyClickListener extends ClickListener {
            private final VisTextArea textArea;

            public CopyClickListener(VisTextArea textArea) {
                this.textArea = textArea;
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                String text = textArea.getText();
                Gdx.app.getClipboard().setContents(text);
            }
        }

        private class SaveClickListener extends ClickListener {
            private final VisTextArea textArea;

            public SaveClickListener(VisTextArea textArea) {
                this.textArea = textArea;
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                String text = textArea.getText();
                Gdx.app.getClipboard().setContents(text);

                FileChooser actor = new FileChooser(new FileHandle(System.getProperty("user.dir", ".")).child("game-crashes"), FileChooser.Mode.SAVE);
                actor.setDefaultFileName("crash-" + DateTimeFormatter.ofPattern("dd.MM.yyyy_HH.mm.ss") + ".txt");
                actor.setWatchingFilesEnabled(true);
                actor.setListener(new FileChooserListener() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle fileHandle = files.get(0);
                        fileHandle.writeString(crash, false);
                    }

                    @Override
                    public void canceled() {

                    }
                });
                stage.addActor(actor);
            }
        }
    }
}
