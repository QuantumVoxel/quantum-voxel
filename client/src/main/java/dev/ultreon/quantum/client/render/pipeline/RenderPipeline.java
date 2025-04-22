package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.TextureBinder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.*;
import dev.ultreon.quantum.client.ClientRegistries;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.util.GameCamera;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.render.RenderBuffer;
import dev.ultreon.quantum.debug.ValueTracker;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.zip.Deflater;

import static com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder.LRU;

/**
 * The rendering pipeline.
 * <p>
 * This class is responsible for rendering the game, it contains a list of nodes that are executed in order.
 * The nodes are executed in the order they are added to the pipeline.
 * <p>
 * The main node is the node that is responsible for rendering the game.
 * It is the last node to be executed.
 * <p>
 * The pipeline is responsible for capturing the output of the nodes.
 * And then passing the output to the main node. Or the next node in the pipeline.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class RenderPipeline implements Disposable {
    private final Array<RenderNode> nodes = new Array<>();
    private final RenderNode main;
    private final GameCamera camera;
    private final IntMap<RenderBuffer> passes = new IntMap<>();

    /**
     * Creates a new render pipeline.
     *
     * @param main the main node.
     * @param camera the camera.
     */
    public RenderPipeline(RenderNode main, GameCamera camera) {
        this.main = main;
        this.camera = camera;
    }

    public void renderPass(ShaderProvider shaderProvider, RenderBuffer pass) {
        this.passes.put(ClientRegistries.SHADER_PROVIDER.getRawId(shaderProvider), pass);
    }

    public RenderBuffer getPass(ShaderProvider shaderProvider) {
        return this.passes.get(ClientRegistries.SHADER_PROVIDER.getRawId(shaderProvider));
    }

    /**
     * Adds a node to the pipeline.
     * <p> This method is used to add a node to the pipeline. It is used to build the pipeline. </p>
     *
     * @param node the node to add.
     * @return the pipeline.
     */
    public RenderPipeline node(RenderNode node) {
        this.nodes.add(node);
        return this;
    }

    /**
     * Renders the pipeline. This is the main method that is used to render the pipeline. It is called by the client.
     * <p> This method is responsible for rendering the pipeline. And rendering the nodes in the pipeline. </p>
     *
     * @param modelBatch the model batch.
     * @param blurScale the blur scale.
     * @param deltaTime the delta time.
     */
    public void render(ModelBatch modelBatch, float blurScale, float deltaTime) {
        @Nullable TerrainRenderer worldRenderer = QuantumClient.get().worldRenderer;
        if (worldRenderer != null) ScreenUtils.clear(worldRenderer.getSkybox().bottomColor, true);
        else ScreenUtils.clear(0F, 0F, 0F, 1F, true);

        QuantumClient.get().renderBuffers().begin(camera);
        ValueTracker.resetObtainRequests();
        ValueTracker.resetFlushed();
        ValueTracker.resetFlushAttempts();

        var textures = new ObjectMap<String, Texture>();
        for (RenderNode node : this.nodes.toArray(RenderNode.class)) {
            if (node.requiresModel()) {
                this.modelRender(modelBatch, node, textures, deltaTime);
            } else {
                this.plainRender(modelBatch, node, textures, deltaTime);
            }
            modelBatch.flush();
        }

        ((MainRenderNode) this.main).blur(blurScale);
        this.main.render(textures, this.camera, deltaTime);
        modelBatch.flush();

        for (var node : this.nodes) {
            node.flush();
        }
        main.flush();
        textures.clear();
    }

    /**
     * Renders the model. This is used to render the node with a model.
     * <p> This is useful for rendering the player, or other things that require a model. </p>
     *
     * @param modelBatch the model batch.
     * @param node the node.
     * @param textures the textures.
     * @param deltaTime the delta time.
     */
    private void modelRender(ModelBatch modelBatch, RenderNode node, ObjectMap<String, Texture> textures, float deltaTime) {
        FrameBuffer frameBuffer = node.getFrameBuffer();
        frameBuffer.begin();
        ScreenUtils.clear(RgbColor.TRANSPARENT.toGdx(), true);

        modelBatch.begin(this.camera);
        node.textureBinder.begin();
        node.time += Gdx.graphics.getDeltaTime();
        node.render(textures, this.camera, deltaTime);
        try {
            modelBatch.end();
        } catch (Exception e) {
            throw new GdxRuntimeException("Failed to render node: " + node.getClass().getSimpleName() + "\n" + node.dump(), e);
        }
        node.textureBinder.end();

        RenderPipeline.capture(node);

        frameBuffer.end();
        Gdx.gl.glViewport(0, 0, QuantumClient.get().getWidth(), QuantumClient.get().getHeight());
    }

    /**
     * Renders the plain. This is used to render the node without a model.
     * <p> This is useful for rendering the skybox, or other things that don't require a model. </p>
     *
     * @param modelBatch the model batch.
     * @param node the node.
     * @param textures the textures.
     * @param deltaTime the delta time.
     */
    private void plainRender(ModelBatch modelBatch, RenderNode node, ObjectMap<String, Texture> textures, float deltaTime) {
        FrameBuffer frameBuffer = node.getFrameBuffer();
        frameBuffer.begin();
        ScreenUtils.clear(RgbColor.TRANSPARENT.toGdx(), true);

        node.textureBinder.begin();
        node.time += Gdx.graphics.getDeltaTime();
        node.render(textures, this.camera, deltaTime);
        node.textureBinder.end();

        // Capture the node.
        RenderPipeline.capture(node);


        frameBuffer.end();
        Gdx.gl.glViewport(0, 0, QuantumClient.get().getWidth(), QuantumClient.get().getHeight());
    }

    /**
     * Captures the node. This is used to capture the node and save it as a PNG file. And dump info about the node.
     * This is useful for debugging. And only happens when the F8 key is pressed.
     * <p>
     *  The file is saved in the game directory, and is named like "FBO_<NodeName>.png" and "INFO_<NodeName>.txt"
     *  Where <NodeName> is the name of the node.
     * </p>
     *
     * @param node the node.
     */
    private static void capture(RenderNode node) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            Pixmap screenshot = Pixmap.createFromFrameBuffer(0, 0, QuantumClient.get().getWidth(), QuantumClient.get().getHeight());
            PixmapIO.writePNG(QuantumClient.data("FBO_" + node.getClass().getSimpleName() + ".png"), screenshot, Deflater.DEFAULT_COMPRESSION, true);

            // Dump info about the node.
            try (var stream = new PrintStream(QuantumClient.data("INFO_" + node.getClass().getSimpleName() + ".txt").write(false))) {
                node.dumpInfo(stream);
            }
        }
    }

    /**
     * Resizes the pipeline, and all nodes the pipeline contains.
     * <p> This is used to resize the pipeline when the window is resized. </p> 
     *
     * @param width the width.
     * @param height the height.
     */
    public void resize(int width, int height) {
        for (var node : this.nodes) {
            node.resize(width, height);
        }

        this.main.resize(width, height);
    }

    /**
     * Disposes the pipeline. Along with all nodes the pipeline contains.
     * <p> This is used to dispose of the pipeline when the game is closed. </p>
     */
    @Override
    public void dispose() {
        for (var node : this.nodes) {
            node.dispose();
        }

        this.main.dispose();
        this.nodes.clear();
    }

    /**
     * The render node.
     * <p> This class is responsible for rendering a node. </p>
     * 
     * @author <a href="https://github.com/XyperCode">Qubilux</a>
     */
    public abstract static class RenderNode {
        /**
         * The identity matrix.
         * <p> This is used to render the node. </p>
         */
        protected static final Matrix4 IDENTITY_MATRIX = new Matrix4();
        /**
         * The texture binder.
         * <p> This is used to bind the textures. </p>
         */
        protected final TextureBinder textureBinder = new DefaultTextureBinder(LRU);
        private float time = 0;
        private final RenderableFlushablePool pool = new RenderableFlushablePool();

        private FrameBuffer fbo;

        /**
         * The client.
         * <p> This is the client. </p>
         */
        protected final QuantumClient client = QuantumClient.get();

        /**
         * The format.
         * <p> This is the format of the frame buffer. </p>
         *
         * @return the format.
         */
        protected Pixmap.Format getFormat() {
            return Pixmap.Format.RGBA8888;
        }

        /**
         * Renders the node. This is the main method that is used to render the node.
         * <p> This is used to render the node. </p>
         *
         * @param textures  the textures.
         * @param camera    the camera.
         * @param deltaTime the delta time.
         */
        public abstract void render(ObjectMap<String, Texture> textures, GameCamera camera, float deltaTime);

        /**
         * Resizes the node. This is used to resize the node when the window is resized.
         * <p> This is used to resize the frame buffer. </p>
         *
         * @param width the width.
         * @param height the height.
         */
        public void resize(int width, int height) {
            if (this.fbo != null)
                this.fbo.dispose();
            this.fbo = createFrameBuffer();
        }

        /**
         * Returns the pool. This is used to obtain a pool of renderables.
         * <p> This is used to obtain a pool of renderables. </p>
         *
         * @return the pool.
         */
        protected RenderableFlushablePool pool() {
            return this.pool;
        }

        /**
         * Returns the frame buffer. This is used to obtain the frame buffer.
         * <p> This is used to obtain the frame buffer. </p>
         *
         * @return the frame buffer.
         */
        public FrameBuffer getFrameBuffer() {
            if (this.fbo == null) {
                this.fbo = createFrameBuffer();
            }
            return this.fbo;
        }

        /**
         * Creates the frame buffer. This is used to create the frame buffer.
         * <p> This is used to create the frame buffer. </p>
         *
         * @return the frame buffer.
         */
        protected FrameBuffer createFrameBuffer() {
            return new FrameBuffer(this.getFormat(), QuantumClient.get().getWidth(), QuantumClient.get().getHeight(), true);
        }

        /**
         * Disposes the frame buffer. This is used to dispose of the frame buffer.
         * <p> This is used to dispose of the frame buffer. </p>
         */
        public void dispose() {
            this.fbo.dispose();
        }

        /**
         * Flushes the pool. This is used to flush the pool.
         * <p> This is used to flush the pool. </p>
         */
        public void flush() {
            this.pool.flush();
        }

        @ApiStatus.Internal
        public void dumpInfo(PrintStream stream) {

        }

        /**
         * Dumps info about the node into a string. This is used to dump info about the node.
         * <p> This is used to dump info about the node. </p>
         *
         * @return the info about the node as a string.
         */
        public String dump() {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (PrintStream printStream = new PrintStream(stream)) {
                this.dumpInfo(printStream);
            }
            return stream.toString();
        }

        /**
         * Returns the time. This is used to obtain the time.
         * <p> This is used to obtain the time. </p>
         *
         * @return the time.
         */
        public float getTime() {
            return this.time;
        }

        /**
         * Returns whether the node requires a model. This is used to determine if the node requires a model.
         * <p> This is used to determine if the node requires a model. </p>
         * <p>
         * If the node requires a model, it will be rendered using a model.
         * If the node does not require a model, it will be rendered using a plain.
         * </p>
         *
         * @return whether the node requires a model.
         */
        public boolean requiresModel() {
            return false;
        }

        /**
         * The renderable flushable pool. This is used to obtain a pool of renderables.
         * <p>
         * A pool of renderables is a collection of renderables that are used to render the node.
         * Which can be reused to avoid creating new renderables every frame.
         * Great way to save performance, am I right? Garbage collector will thank you. :D
         * </p>
         *
         * @author <a href="https://github.com/XyperCode">Qubilux</a>
         */
        public static class RenderableFlushablePool extends FlushablePool<Renderable> {
            /**
             * Creates a new renderable. This is used to create a new renderable.
             *
             * @return the renderable.
             */
            @Override
            protected Renderable newObject() {
                return new Renderable();
            }

            /**
             * Obtains a renderable. This is used to obtain a renderable.
             * <p>
             * This is used to obtain a renderable.
             * </p>
             *
             * @return the renderable.
             */
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

            /**
             * Returns the number of obtained renderables. This is used to obtain the number of obtained renderables.
             * <p>
             * This is used to obtain the number of obtained renderables.
             * </p>
             *
             * @return the number of obtained renderables.
             */
            public int getObtained() {
                return this.obtained.size;
            }
        }
    }
}


