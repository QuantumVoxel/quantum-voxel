package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.util.GameCamera;
import dev.ultreon.quantum.util.GameObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.ultreon.quantum.client.QuantumClient.PROFILER;

@SuppressWarnings("GDXJavaUnsafeIterator")
public class RenderBufferSource extends GameObject implements Disposable {
    private static final Array<RenderBufferSource> MANAGED = new Array<>();

    private final ObjectMap<RenderPass, RenderBuffer> buffers = new ObjectMap<>();
    private final ObjectMap<RenderPass, RenderBuffer> backBuffers = new ObjectMap<>();
    private final List<RenderPass> buffersSorted = new ArrayList<>();
    private @Nullable GameCamera camera;
    private boolean started;
    public long timeSpan;

    public RenderBufferSource() {
        MANAGED.add(this);
    }

    public void begin(GameCamera camera) {
        try {
            if (this.started) throw new IllegalStateException("RenderBuffer already started");
            this.camera = camera;
            this.started = true;
        } finally {
            QuantumClient.PROFILER.end();
        }
    }

    @SuppressWarnings("GDXJavaFlushInsideLoop")
    public void flush() {
        PROFILER.begin("render-buffer-source@flush");
        try {
            long start = System.nanoTime() / 1000;
            for (RenderPass pass : this.buffersSorted) {
                RenderBuffer buffer = buffers.get(pass);
                if (buffer == null || !buffer.isStarted()) continue;
                buffer.flush();
            }
            this.timeSpan = System.nanoTime() / 1000 - start;
        } finally {
            PROFILER.end();
        }
    }

    public RenderBuffer getBuffer(RenderPass pass) {
        PROFILER.begin("render-buffer-source@get-buffer");
        try {
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
            if (!this.getChildren().contains(buffer))
                this.add("Source " + pass.name(), buffer);
            return buffer;
        } finally {
            PROFILER.end();
        }
    }

    @SuppressWarnings("GDXJavaFlushInsideLoop")
    public void end() {
        PROFILER.begin("render-buffer-source@end");
        try {
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
        } finally {
            PROFILER.end();
        }
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
