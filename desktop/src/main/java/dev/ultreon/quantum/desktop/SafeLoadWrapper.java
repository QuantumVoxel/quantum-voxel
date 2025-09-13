package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;
import de.damios.guacamole.gdx.graphics.NestableFrameBuffer;
import dev.ultreon.libs.commons.v0.util.ExceptionUtils;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.Main;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.GlStateStack;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.desktop.imgui.ImGuiOverlay;
import org.slf4j.LoggerFactory;

public class SafeLoadWrapper implements ApplicationListener {
    private Main quantum;
    private SpriteBatch batch;
    private String crash;
    private final String[] args;
    private Screen currentScreen = new ScreenAdapter();
    private InputProcessor inputProcessor;
    private TextureRegion whitePixel;
    private NestableFrameBuffer containerBuffer;
    private Texture titlebarTex;
    private TextureRegion windowBorder;
    private TextureRegion titleBar;
    private NinePatch titleBarPatch;
    private NinePatch windowBorderPatch;
    private final TextureRegion container = new TextureRegion();
    private final Matrix4 transform = new Matrix4();
    private final Matrix4 projection = new Matrix4();

    public SafeLoadWrapper(String[] args) {
        this.args = args;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        whitePixel = createWhitePixel();

        if (DesktopPlatform.get().hasBackPanelRemoved()) {
            containerBuffer = new NestableFrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            containerBuffer.begin();
            titlebarTex = new Texture(Gdx.files.internal("window.png"));
            windowBorder = new TextureRegion(titlebarTex, 0, 0, 30, 30);
            titleBar = new TextureRegion(titlebarTex, 30, 0, 30, 30);
            titleBarPatch = new NinePatch(titleBar, 6, 6, 2, 2);
            windowBorderPatch = new NinePatch(windowBorder, 6, 6, 6, 9);
            containerBuffer.end();
            container.flip(false, true);
        }

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

        if (DesktopPlatform.get().isGameDisabled()) {
            return;
        }

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
        if (DesktopPlatform.get().isGameDisabled()) {
            return;
        }

        if (containerBuffer != null) {
            containerBuffer.dispose();
            containerBuffer = new NestableFrameBuffer(Pixmap.Format.RGB888, width - 12, height - 12 - 20, true);
        }

        if (quantum != null) {
            try {
                if (DesktopPlatform.get().isContained()) {
                    quantum.resize(containerBuffer.getWidth(), containerBuffer.getHeight());
                } else {
                    quantum.resize(width, height);
                }
            } catch (Throwable e) {
                crash(e);
            }
        }

        if (currentScreen != null) {
            currentScreen.resize(width, height);
        }
    }

    void crash(Throwable e) {
        if (DesktopPlatform.get().isGameDisabled()) {
            ImGuiOverlay.CRASH_HOOK.accept(e);
            return;
        }

        if (crash != null) return;
        try {
            CommonConstants.LOGGER.error("Game Crashed:", e);
        } catch (Throwable t) {
            e.addSuppressed(t);
            e.printStackTrace();
        }
        crash = ExceptionUtils.getStackTrace(e).replace("\t", "    ");
        quantum = null;

//        Gdx.app.postRunnable(() -> openScreen(new CrashScreen()));
    }

    void crash(ApplicationCrash crashLog) {
        if (crash != null) return;
        crash = crashLog.toString();
        quantum = null;

//        Gdx.app.postRunnable(() -> openScreen(new CrashScreen()));
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
        if (DesktopPlatform.get().isGameDisabled()) {
            if (ImGuiOverlay.isShown()) {
                ImGuiOverlay.renderImGui(QuantumClient.get());
            }
            return;
        }

        if (crash != null) {
            if (currentScreen != null)
                currentScreen.render(Gdx.graphics.getDeltaTime());
            return;
        }
        try {
            if (DesktopPlatform.get().isContained()) {
                ScreenUtils.clear(0, 0, 0, 0, true);
                containerBuffer.begin();
                try {
                    if (quantum != null) {
                        quantum.render();
                    }
                } finally {
                    containerBuffer.end();
                }
                renderWindow();
            } else {
                if (quantum != null) {
                    quantum.render();
                }
                DesktopPlatform.get().setWindowOffset(0, 0);
            }
        } catch (Throwable t) {
            Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.app.getGraphics();
            graphics.getWindow().setVisible(true);
            this.crash(t);
        }
    }

    private void renderWindow() {
        transform.set(batch.getTransformMatrix());
        batch.setTransformMatrix(batch.getTransformMatrix().scale(2f, 2f, 1));
        batch.begin();
        titleBarPatch.draw(batch, 0, Gdx.graphics.getHeight() / 2f - 20, Gdx.graphics.getWidth() / 2f, 20);
        windowBorderPatch.draw(batch, 0, 0, Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f - 20);
        batch.end();
        batch.setTransformMatrix(transform);
        projection.set(batch.getProjectionMatrix());
        batch.setProjectionMatrix(batch.getProjectionMatrix().setToOrtho(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1));
        batch.begin();

        batch.draw(containerBuffer.getColorBufferTexture(), 12, 40, Gdx.graphics.getWidth() - 24, Gdx.graphics.getHeight() - 24 - 40);
        batch.end();
        batch.setProjectionMatrix(projection);

        QuantumClient quantumClient = QuantumClient.get();
        if (quantumClient == null) return;
        float guiScale = quantumClient.getGuiScale();
        DesktopPlatform.get().setWindowOffset((int) (12 / guiScale), (int) (40 / guiScale));
    }

    @Override
    public void pause() {
        if (DesktopPlatform.get().isGameDisabled()) {
            return;
        }

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
        if (DesktopPlatform.get().isGameDisabled()) {
            return;
        }

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
        if (DesktopPlatform.get().isGameDisabled()) {
            LoggerFactory.getLogger(getClass()).warn("Game disabled, can't dispose! Terminating...");
            Runtime.getRuntime().halt(1);
        }

        if (quantum != null) {
            try {
                quantum.dispose();
            } catch (Throwable e) {
                crash(e);
            }
        }

        if (containerBuffer != null) {
            containerBuffer.dispose();
            containerBuffer = null;
        }

        if (windowBorder != null) windowBorder.getTexture().dispose();
        if (titleBar != null) titleBar.getTexture().dispose();

        batch.dispose();

        if (whitePixel != null) whitePixel.getTexture().dispose();
    }

    public boolean isCrashed() {
        return crash != null;
    }

//    private class CrashScreen extends ScreenAdapter {
//        private final ScreenViewport viewport = new ScreenViewport();
//        private final Stage stage = new Stage(viewport, batch);
//        private final Window window;
//
//        public CrashScreen() {
//            viewport.setUnitsPerPixel(Gdx.graphics.getBackBufferScale());
//
//            stage.addActor(new Background());
//            window = new VisWindow("Game Crashed");
//            window.setResizable(true);
//            window.setMovable(false);
//            window.setSize(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
//            window.setPosition(Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 4f);
//            stage.addActor(window);
//
//            // Root table inside a window or stage
//            VisTable table = new VisTable();
//            table.setFillParent(true); // optional, if added to stage
//            table.defaults().pad(10);  // optional padding
//
//            VisTextArea textArea = new VisTextArea(crash, Utils.make(new VisTextField.VisTextFieldStyle(VisUI.getSkin().get(VisTextField.VisTextFieldStyle.class)), visTextFieldStyle -> {
//                visTextFieldStyle.disabledFontColor = Color.WHITE;
//            }));
//            textArea.setDisabled(true);
//            textArea.setPrefRows(crash.lines().count()); // Makes it taller than the ScrollPane
//
//            VisScrollPane scrollPane = new VisScrollPane(textArea);
//            scrollPane.setFadeScrollBars(false); // Optional: show scrollbars always
//            scrollPane.setScrollingDisabled(false, false); // Allow both scroll directions
//
//            textArea.setFillParent(false);
//
//            table.add(scrollPane).expand().fill().pad(10).row();
//
//            VisTable buttons = new VisTable();
//            buttons.right().defaults().pad(5);
//            VisTextButton button = new VisTextButton("Copy");
//            button.setClip(true);
//
//            button.addListener(new CopyClickListener(textArea));
//
//            buttons.add(button);
//
//            VisTextButton saveButton = new VisTextButton("Save As...");
//            saveButton.setClip(true);
//
//            saveButton.addListener(new SaveClickListener(textArea));
//
//            buttons.add(saveButton);
//            table.add(buttons).bottom().fillX().expandX().pad(5);
//
//            window.add(table).fill().expand().pad(10);
//        }
//
//        @Override
//        public void resize(int width, int height) {
//            super.resize(width, height);
//            stage.getViewport().update(width, height, true);
//        }
//
//        @Override
//        public void show() {
//            super.show();
//
//            Gdx.input.setInputProcessor(stage);
//        }
//
//        @Override
//        public void render(float delta) {
//            Gdx.gl.glClearColor(0, 0, 0, 1);
//            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//            window.setPosition(Gdx.graphics.getWidth() / 2f - window.getWidth() / 2f, Gdx.graphics.getHeight() / 2f - window.getHeight() / 2f);
//
//            stage.act(delta);
//            stage.draw();
//        }
//
//        private class Background extends Actor {
//            @Override
//            public void draw(Batch batch, float parentAlpha) {
//                super.draw(batch, 0.3f);
//
//                batch.setColor(1, 1, 1, 0.3f);
//                batch.draw(whitePixel, 0, 0, stage.getWidth(), stage.getHeight());
//                batch.setColor(1, 1, 1, 1f);
//            }
//        }
//
//        private class CopyClickListener extends ClickListener {
//            private final VisTextArea textArea;
//
//            public CopyClickListener(VisTextArea textArea) {
//                this.textArea = textArea;
//            }
//
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                String text = textArea.getText();
//                Gdx.app.getClipboard().setContents(text);
//            }
//        }
//
//        private class SaveClickListener extends ClickListener {
//            private final VisTextArea textArea;
//
//            public SaveClickListener(VisTextArea textArea) {
//                this.textArea = textArea;
//            }
//
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                String text = textArea.getText();
//                Gdx.app.getClipboard().setContents(text);
//
//                FileChooser actor = new FileChooser(new FileHandle(System.getProperty("user.dir", ".")).child("game-crashes"), FileChooser.Mode.SAVE);
////                actor.setDefaultFileName("crash-" + DateTimeFormatter.ofPattern("dd.MM.yyyy_HH.mm.ss") + ".txt");
//                actor.setWatchingFilesEnabled(true);
//                actor.setListener(new FileChooserListener() {
//                    @Override
//                    public void selected(Array<FileHandle> files) {
//                        FileHandle fileHandle = files.get(0);
//                        fileHandle.writeString(crash, false);
//                    }
//
//                    @Override
//                    public void canceled() {
//                        // No need to cancel
//                    }
//                });
//                stage.addActor(actor);
//            }
//        }
//    }
}
