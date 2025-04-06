package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.component.GameComponent;
import dev.ultreon.quantum.util.GameObject;

public class ChunkMesh extends GameObject implements Disposable {
    public final RenderPass pass;
    public final Mesh mesh;
    final Renderable instance;
    public final int numVertices;
    public final int numIndices;

    private boolean disposed = false;
    private final Vector3 tmp = new Vector3();
    private final ClientChunk chunk;

    public ChunkMesh(RenderPass pass, Mesh mesh, ClientChunk chunk) {
        this.pass = pass;
        this.mesh = mesh;
        this.instance = new Renderable();
        instance.meshPart.set("chunk_" + chunk.getVec().x + "_" + chunk.getVec().y + "_" + chunk.getVec().z + "/" + pass.name(), mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES);
        numVertices = mesh.getNumVertices();
        numIndices = mesh.getNumIndices();
        instance.userData = chunk;
        instance.worldTransform.idt();
        instance.material = pass.createMaterial();
        instance.shader = pass.createShader().getShader(instance);

        this.chunk = chunk;

        chunk.addMesh(this);
    }

    public void dispose() {
        disposed = true;
        mesh.dispose();

        chunk.removeMesh(this);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void render(GameCamera camera, RenderBufferSource bufferSource) {
        if (disposed) return;

        instance.worldTransform.set(combined).translate(camera.relative(camera.getCamPos(), tmp));
        bufferSource.getBuffer(this.pass).render(this.instance);
    }
}
