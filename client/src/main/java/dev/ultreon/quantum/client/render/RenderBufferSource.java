package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.util.GameCamera;
import dev.ultreon.quantum.util.GameObject;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("GDXJavaUnsafeIterator")
public class RenderBufferSource extends GameObject implements Disposable {
    private static final Array<RenderBufferSource> MANAGED = new Array<>();

    private final ObjectMap<RenderPass, RenderBuffer> buffers = new ObjectMap<>();
    private final ObjectMap<RenderPass, RenderBuffer> backBuffers = new ObjectMap<>();
    private final Array<RenderPass> buffersSorted = new Array<>(RenderPass.class);
    private @Nullable GameCamera camera;
    private boolean started;
    public long timeSpan;

    public RenderBufferSource() {
        MANAGED.add(this);
    }

    public void begin(GameCamera camera) {
        if (this.started) throw new IllegalStateException("RenderBuffer already started");        this.camera = camera;
        this.started = true;
    }

    @SuppressWarnings("GDXJavaFlushInsideLoop")
    public void flush() {
        long start = System.nanoTime() / 1000;
        for (RenderPass pass : this.buffersSorted.toArray(RenderPass.class)) {
            RenderBuffer buffer = buffers.get(pass);
            if (buffer == null || !buffer.isStarted()) continue;
            buffer.flush();
        }
        this.timeSpan = System.nanoTime() / 1000 - start;
    }

    public RenderBuffer getBuffer(RenderPass pass) {
        if (!started) throw new IllegalStateException("RenderBuffer not started");

        RenderBuffer buffer = buffers.get(pass);
        if (buffer == null) {
            if (backBuffers.containsKey(pass)) {
                buffer = backBuffers.get(pass);
                backBuffers.remove(pass);
            } else {
                buffer = new RenderBuffer(pass);
            }
        }

        buffer.begin(camera);
        buffers.put(pass, buffer);
        buffersSorted.add(pass);
        if (!this.getChildren().contains(buffer, true))
            this.add("Source " + pass.name(), buffer);
        return buffer;
    }

    @SuppressWarnings("GDXJavaFlushInsideLoop")
    public void end() {
        long start = System.nanoTime() / 1000;
        for (RenderPass pass : this.buffersSorted) {
            RenderBuffer buffer = buffers.get(pass);
            if (buffer == null || !buffer.isStarted()) continue;
            buffer.flush();
            buffer.end();
            this.backBuffers.put(pass, buffer);
        }

        this.buffers.clear();
        this.buffersSorted.clear();

        this.camera = null;
        this.started = false;

        this.timeSpan = System.nanoTime() / 1000 - start;
    }

    @Override
    public void dispose() {
        for (RenderBuffer pass : this.buffers.values()) {
            if (pass == null) continue;
            pass.dispose();
        }
        this.buffers.clear();

        MANAGED.removeValue(this, true);
    }

    public static void disposeAll() {
        for (RenderBufferSource batch : MANAGED.items) {
            if (batch == null) continue;
            batch.dispose();
        }
        MANAGED.clear();
    }

    public static int getManagedCount() {
        return MANAGED.size;
    }
}
