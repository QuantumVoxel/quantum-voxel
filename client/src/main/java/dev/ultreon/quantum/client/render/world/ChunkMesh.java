package dev.ultreon.quantum.client.render.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.util.GameCamera;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderType;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.util.GameObject;

import java.util.List;

public class ChunkMesh extends GameObject implements Disposable {
    public final RenderType pass;
    public final Mesh mesh;
    final Renderable instance;
    public final int numVertices;
    public final int numIndices;

    private boolean disposed = false;
    private final Vector3 tmp = new Vector3();
    private final ClientChunk chunk;

    public ChunkMesh(RenderType pass, Mesh mesh, ClientChunk chunk) {
        this.pass = pass;
        this.mesh = mesh;
        this.instance = new Renderable();
        instance.meshPart.set("chunk_" + chunk.getVec().x + "_" + chunk.getVec().y + "_" + chunk.getVec().z + "/" + pass.name(), mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES);
        numVertices = mesh.getNumVertices();
        numIndices = mesh.getNumIndices();
        instance.userData = chunk;

        this.chunk = chunk;
    }

    public void dispose() {
        disposed = true;
        mesh.dispose();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public List<GameObject> hit(Ray pickRay) {
        List<GameObject> hit = super.hit(pickRay);
        if (mesh.getNumVertices() == 0) return hit;
        BoundingBox bb = new BoundingBox();
        mesh.calculateBoundingBox(bb);
        if (Intersector.intersectRayBounds(pickRay, bb, tmp)) {
            hit.add(this.chunk);
        }
        return hit;
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
