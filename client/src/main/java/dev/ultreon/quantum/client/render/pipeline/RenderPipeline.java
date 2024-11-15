package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.TextureBinder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.*;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.debug.ValueTracker;
import dev.ultreon.quantum.util.RgbColor;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.zip.Deflater;

import static com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder.LRU;

public class RenderPipeline implements Disposable {
    private final Array<RenderNode> nodes = new Array<>();
    private final RenderNode main;
    private final GameCamera camera;


    public RenderPipeline(RenderNode main, GameCamera camera) {
        this.main = main;
        this.camera = camera;
    }

    public RenderPipeline node(RenderNode node) {
        this.nodes.add(node);
        return this;
    }

    @SuppressWarnings("GDXJavaFlushInsideLoop") // We need to flush before the next node.
    public void render(ModelBatch modelBatch, float blurScale, float deltaTime) {
        @Nullable TerrainRenderer worldRenderer = QuantumClient.get().worldRenderer;
        if (worldRenderer != null) ScreenUtils.clear(worldRenderer.getSkybox().bottomColor, true);
        else ScreenUtils.clear(0F, 0F, 0F, 1F, true);

        ValueTracker.resetObtainRequests();
        ValueTracker.resetFlushed();
        ValueTracker.resetFlushAttempts();

        var textures = new ObjectMap<String, Texture>();
        for (var node : this.nodes) {
            if (node.requiresModel()) {
                this.modelRender(modelBatch, node, textures, deltaTime);
            } else {
                this.plainRender(modelBatch, node, textures, deltaTime);
            }
            modelBatch.flush();
        }

        ((MainRenderNode) this.main).blur(blurScale);
        this.main.render(textures, modelBatch, this.camera, deltaTime);
        modelBatch.flush();

        for (var node : this.nodes) {
            node.flush();
        }
        main.flush();
        textures.clear();
    }

    private void modelRender(ModelBatch modelBatch, RenderNode node, ObjectMap<String, Texture> textures, float deltaTime) {
        FrameBuffer frameBuffer = node.getFrameBuffer();
        frameBuffer.begin();
        ScreenUtils.clear(RgbColor.TRANSPARENT.toGdx(), true);

        modelBatch.begin(this.camera);
        node.textureBinder.begin();
        node.time += Gdx.graphics.getDeltaTime();
        node.render(textures, modelBatch, this.camera, deltaTime);
        try {
            modelBatch.end();
        } catch (Exception e) {
            throw new GdxRuntimeException("Failed to render node: " + node.getClass().getSimpleName() + "\n" + node.dump(), e);
        }
        node.textureBinder.end();

        RenderPipeline.capture(node);

        frameBuffer.end();
    }

    private void plainRender(ModelBatch modelBatch, RenderNode node, ObjectMap<String, Texture> textures, float deltaTime) {
        FrameBuffer frameBuffer = node.getFrameBuffer();
        frameBuffer.begin();
        ScreenUtils.clear(RgbColor.TRANSPARENT.toGdx(), true);

        node.textureBinder.begin();
        node.time += Gdx.graphics.getDeltaTime();
        node.render(textures, modelBatch, this.camera, deltaTime);
        node.textureBinder.end();

        RenderPipeline.capture(node);

        frameBuffer.end();
    }

    private static void capture(RenderNode node) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            Pixmap screenshot = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            PixmapIO.writePNG(Gdx.files.local("FBO_" + node.getClass().getSimpleName() + ".png"), screenshot, Deflater.DEFAULT_COMPRESSION, true);
            try (var stream = new PrintStream(Gdx.files.local("INFO_" + node.getClass().getSimpleName() + ".txt").write(false))) {
                node.dumpInfo(stream);
            }
        }
    }

    public void resize(int width, int height) {
        for (var node : this.nodes) {
            node.resize(width, height);
        }

        this.main.resize(width, height);
    }

    @Override
    public void dispose() {
        for (var node : this.nodes) {
            node.dispose();
        }

        this.main.dispose();
        this.nodes.clear();
    }

    public abstract static class RenderNode {
        protected static final Matrix4 IDENTITY_MATRIX = new Matrix4();
        protected final TextureBinder textureBinder = new DefaultTextureBinder(LRU);
        private float time = 0;
        private final RenderableFlushablePool pool = new RenderableFlushablePool();

        private FrameBuffer fbo;
        protected final QuantumClient client = QuantumClient.get();

        protected Pixmap.Format getFormat() {
            return Pixmap.Format.RGBA8888;
        }

        public abstract @NewInstance void render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, float deltaTime);

        public void resize(int width, int height) {
            if (this.fbo != null)
                this.fbo.dispose();
            this.fbo = createFrameBuffer();
        }

        protected RenderableFlushablePool pool() {
            return this.pool;
        }

        public FrameBuffer getFrameBuffer() {
            if (this.fbo == null) {
                this.fbo = createFrameBuffer();
            }
            return this.fbo;
        }

        protected FrameBuffer createFrameBuffer() {
            return new FrameBuffer(this.getFormat(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }

        public void dispose() {
            this.fbo.dispose();
        }

        public void flush() {
            this.pool.flush();
        }

        @ApiStatus.Internal
        public void dumpInfo(PrintStream stream) {

        }

        public String dump() {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (PrintStream printStream = new PrintStream(stream)) {
                this.dumpInfo(printStream);
            }
            return stream.toString();
        }

        public float getTime() {
            return this.time;
        }

        public boolean requiresModel() {
            return false;
        }

        public static class RenderableFlushablePool extends FlushablePool<Renderable> {
            @Override
            protected Renderable newObject() {
                return new Renderable();
            }

            @Override
            public Renderable obtain() {
                Renderable renderable = super.obtain();
                renderable.environment = null;
                renderable.material = null;
                renderable.meshPart.set("", null, 0, 0, 0);
                renderable.shader = null;
                renderable.userData = null;
                return renderable;
            }

            public int getObtained() {
                return this.obtained.size;
            }
        }
    }
}


