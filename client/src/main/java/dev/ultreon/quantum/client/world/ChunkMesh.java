package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderPass;

public class ChunkMesh implements Disposable {
    public final RenderPass pass;
    public final Mesh mesh;
    final Renderable instance;

    private boolean disposed = false;
    private final Vector3 tmp = new Vector3();

    public ChunkMesh(RenderPass pass, Mesh mesh, ClientChunk chunk) {
        this.pass = pass;
        this.mesh = mesh;
        this.instance = new Renderable();
        instance.meshPart.set(pass.name(), mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES);
        instance.userData = chunk;
    }

    public void dispose() {
        disposed = true;
        mesh.dispose();
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void render(GameCamera camera, RenderBufferSource bufferSource) {
        if (disposed) return;

        instance.worldTransform.idt().translate(camera.relative(camera.getCamPos(), tmp));
        bufferSource.getBuffer(this.pass).render(this.instance);
    }
}
