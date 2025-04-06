package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.world.RenderablePool;
import dev.ultreon.quantum.util.GameObject;

public class RenderBuffer extends GameObject implements Disposable {
    private static final Array<RenderBuffer> MANAGED = new Array<>();

    private final ModelBatch modelBatch;
    public final Material material;
    public final String name;
    private final ShaderProvider shader;
    private boolean started = false;
    private final Array<Renderable> buffer = new Array<>(16);
    private final RenderablePool pool = new RenderablePool();
    private int currentRenderCount;
    public int renderCount;
    public int lastRenderCount;
    public long timeSpan;
    public long timePerRender;

    public RenderBuffer(RenderPass pass) {
        this.shader = pass.createShader();
        this.modelBatch = new ModelBatch(shader);
        this.material = pass.createMaterial();
        this.name = pass.name();
        MANAGED.add(this);
    }

    public void render(Renderable instance) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        instance.shader = shader.getShader(instance);
        this.modelBatch.render(instance);
        this.currentRenderCount++;
    }

    public void render(Array<Renderable> renderables) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (Renderable renderable : renderables.toArray(Renderable.class)) {
            if (renderable == null) continue;
            renderable.shader = shader.getShader(renderable);
            this.modelBatch.render(renderable);
            this.currentRenderCount++;
        }
    }

    public void render(Renderable[] renderables) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (Renderable renderable : renderables) {
            if (renderable == null) continue;
            renderable.shader = shader.getShader(renderable);
            this.modelBatch.render(renderable);
            this.currentRenderCount++;
        }
    }

    public void render(Renderable[] renderables, int start, int end) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (int i = start; i < end; i++) {
            Renderable renderable = renderables[i];
            if (renderable == null) continue;
            renderable.shader = shader.getShader(renderable);
            this.modelBatch.render(renderable);
            this.currentRenderCount++;
        }
    }

    public void render(Iterable<Renderable> renderables) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (Renderable renderable : renderables) {
            if (renderable == null) continue;
            renderable.shader = shader.getShader(renderable);
            this.modelBatch.render(renderable);
            this.currentRenderCount++;
        }
    }

    public void render(RenderableProvider provider) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");

        provider.getRenderables(buffer, pool);
        render(buffer);
        this.currentRenderCount += buffer.size;
        buffer.clear();
    }

    public void render(RenderableProvider[] providers) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        for (RenderableProvider provider : providers) {
            provider.getRenderables(buffer, pool);
            render(buffer);
            this.currentRenderCount += buffer.size;
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
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        this.modelBatch.flush();
        this.timeSpan = System.nanoTime() / 1000 - startTime;
        this.lastRenderCount = this.renderCount;
        this.renderCount = currentRenderCount;
        this.timePerRender = this.timeSpan / this.renderCount;
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
