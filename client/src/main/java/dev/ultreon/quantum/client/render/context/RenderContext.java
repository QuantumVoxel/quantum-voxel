package dev.ultreon.quantum.client.render.context;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.pass.RenderPass;
import dev.ultreon.quantum.client.render.world.ChunkRenderState;
import dev.ultreon.quantum.client.render.world.WorldRenderer;
import dev.ultreon.quantum.client.world.*;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RenderContext {
    public final QuantumClient client;
    public final WorldRenderer worldRenderer;
    public final Skybox skybox;
    public final Color fogColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    public final ClientWorld world;
    public int loadedChunks;
    public int visibleChunks;
    public final PerspectiveCamera camera;

    public RenderContext(QuantumClient client, WorldRenderer worldRenderer, ClientWorld world) {
        this.client = client;
        this.worldRenderer = worldRenderer;
        this.camera = worldRenderer.getCamera();
        this.skybox = worldRenderer.getSkybox();
        this.world = world;
    }

    public List<ClientChunk> chunksInViewSorted(Collection<ClientChunk> chunks, Player player) {
        List<ClientChunk> list = new ArrayList<>(chunks);
//        list = list.stream().sorted((o1, o2) -> {
//            Vec3d mid1 = WorldRenderer.TMP_3D_A.set(o1.getOffset().x + (float) CS, o1.getOffset().y + (float) CS, o1.getOffset().z + (float) CS);
//            Vec3d mid2 = WorldRenderer.TMp_3D_B.set(o2.getOffset().x + (float) CS, o2.getOffset().y + (float) CS, o2.getOffset().z + (float) CS);
//            return Double.compare(mid1.dst(player.getPosition(TMP_3D1)), mid2.dst(player.getPosition(TMP_3D2)));
//        }).collect(Collectors.toList());
        return list;
    }

    public void pushInfo() {
        worldRenderer.consumeInfo(loadedChunks, visibleChunks);
    }

    public void renderTerrain(RenderBufferSource bufferSource, List<ClientChunk> chunks, LocalPlayer player, ChunkRenderState ref, RenderPass pass) {
        worldRenderer.renderTerrain(bufferSource, chunks, player, ref, pass);
    }

    public void renderEntity(RenderBufferSource bufferSource, RenderPass pass, @Nullable Entity player) {
        worldRenderer.renderEntity(bufferSource, pass, player);
    }

    public void renderGizmos(float deltaTime) {
        worldRenderer.renderGizmos(deltaTime);
    }
}
