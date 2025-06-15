package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.world.RenderablePool;
import dev.ultreon.quantum.util.GameObject;

import java.util.function.Consumer;

public class RenderBuffer extends GameObject implements Disposable {
    private static final Array<RenderBuffer> MANAGED = new Array<>();

    protected final ModelBatch modelBatch;
    public final String name;
    protected final ShaderProvider shader;
    protected final int primitiveType;
    private final VertexAttributes attributes;
    protected final Material instanceMaterial;
    protected boolean started = false;
    private final Array<Renderable> buffer = new Array<>(16);
    private final RenderablePool pool = new RenderablePool();
    protected int currentRenderCount;
    public int renderCount;
    public int lastRenderCount;
    public long timeSpan;
    public long timePerRender;
    private final MeshBuilder builder = new MeshBuilder();

    RenderBuffer(RenderPass pass) {
        this.shader = getShader(pass);
        this.modelBatch = new ModelBatch(shader);
        this.instanceMaterial = getMaterial(pass).copy();
        this.name = pass.name();
        this.primitiveType = pass.mode();
        this.attributes = pass.attributes();
        MANAGED.add(this);
    }

    public Material getMaterial(RenderPass pass) {
        return pass.createMaterial();
    }

    public ShaderProvider getShader(RenderPass pass) {
        return pass.createShader();
    }

    public void createMesh(Consumer<MeshPartBuilder> consumer) {
        builder.begin(attributes, primitiveType);
        consumer.accept(builder);
        builder.end();
    }

    public void render(Renderable instance) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        instance.shader = shader.getShader(instance);
        instance.meshPart.primitiveType = primitiveType;
        if (instance.material == null) instance.material = instanceMaterial;
        else instance.material.set(instanceMaterial);
        this.modelBatch.render(instance);
        this.currentRenderCount++;
    }

    public void render(Array<Renderable> renderables) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (Renderable renderable : renderables.toArray(Renderable.class)) {
            if (renderable == null) continue;
            render(renderable);
        }
    }

    public void render(Renderable[] renderables) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (Renderable renderable : renderables) {
            if (renderable == null) continue;
            render(renderable);
        }
    }

    public void render(Renderable[] renderables, int start, int end) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (int i = start; i < end; i++) {
            Renderable renderable = renderables[i];
            if (renderable == null) continue;
            render(renderable);
        }
    }

    public void render(Iterable<Renderable> renderables) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (Renderable renderable : renderables) {
            if (renderable == null) continue;
            render(renderable);
        }
    }

    public void render(RenderableProvider provider) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");

        provider.getRenderables(buffer, pool);
        render(buffer);
        buffer.clear();
    }

    public void render(RenderableProvider[] providers) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (RenderableProvider provider : providers) {
            provider.getRenderables(buffer, pool);
            render(buffer);
            buffer.clear();
        }
    }

    public void render(RenderableProvider[] providers, int start, int end) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (int i = start; i < end; i++) {
            RenderableProvider provider = providers[i];
            provider.getRenderables(buffer, pool);
            for (Renderable renderable : buffer) {
                if (renderable == null) continue;
                renderable.shader = shader.getShader(renderable);
                this.modelBatch.render(renderable);
            }
            this.currentRenderCount += buffer.size;
            buffer.clear();
        }
    }

    public void flush() {
        long startTime = System.nanoTime() / 1000;
        if (!started) throw new IllegalStateException("RenderBuffer not started: " + this.name);
        this.modelBatch.flush();
        this.timeSpan = System.nanoTime() / 1000 - startTime;
        this.lastRenderCount = this.renderCount;
        this.renderCount = currentRenderCount;
        if (renderCount == 0) this.timePerRender = 0;
        else this.timePerRender = this.timeSpan / this.renderCount;
        this.currentRenderCount = 0;
    }

    public void dispose() {
        this.modelBatch.dispose();
        MANAGED.removeValue(this, true);
    }

    public static void disposeAll() {
        for (RenderBuffer pass : MANAGED.items) {
            if (pass == null) continue;
            pass.dispose();
        }

        MANAGED.clear();
    }

    public static int getManagedCount() {
        return MANAGED.size;
    }

    public void begin(Camera cam) {
        if (started) return;
        this.modelBatch.begin(cam);
        started = true;
    }

    public void end() {
        if (!started) return;
        this.modelBatch.end();
        started = false;
    }

    public boolean isStarted() {
        return started;
    }
}
