package dev.ultreon.quantum.client.render.pass;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderType;
import dev.ultreon.quantum.client.render.context.RenderContext;
import dev.ultreon.quantum.client.render.context.RenderMaterial;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import static dev.ultreon.quantum.client.QuantumClient.PROFILER;

public abstract class RenderPass implements Disposable {
    private final String name;
    private int width;
    private int height;

    protected RenderPass(String name) {
        this.name = name;
        this.width = QuantumClient.get().getWidth();
        this.height = QuantumClient.get().getHeight();
    }

    protected abstract void resize(int newWidth, int newHeight);

    protected abstract void create();

    public abstract void dispose();

    public abstract void render(RenderBufferSource bufferSource, RenderContext context);

    public Set<ClientChunk> getRayVisibleChunks(RenderContext context, ClientWorld world) {
        try {
            Set<ClientChunk> visible = new HashSet<>();
            Queue<ClientChunk> queue = new ArrayDeque<>();
            Set<ChunkVec> visited = new HashSet<>();

            LocalPlayer player = context.client.player;
            if (player == null) return visible;
            ClientChunk origin = world.getChunk(player.getChunkVec());
            if (origin == null) return visible;
            queue.add(origin);
            visited.add(origin.vec);

            while (!queue.isEmpty()) {
                ClientChunk chunk = queue.poll();
                if (chunk == null) continue;

                visible.add(chunk);

                for (Direction dir : Direction.values()) {
                    ClientChunk neighbor = chunk.relative(dir);
                    if (neighbor == null
                            || visited.contains(neighbor.vec)
                            || distance(context, origin, neighbor) > world.getRenderDistance())
                        continue;


                    if (hasVisiblePath(chunk, dir)) {
                        queue.add(neighbor);
                        visited.add(neighbor.vec);
                    }
                }
            }

            return visible;
        } finally {
            PROFILER.end();
        }
    }

    private double distance(RenderContext context, ClientChunk origin, ClientChunk neighborPos) {
        int renderDistance = context.world.getRenderDistance();
        int distanceSquared = origin.getDistanceSquared(neighborPos);
        return Math.max(0, renderDistance - Math.sqrt(distanceSquared));
    }

    private boolean hasVisiblePath(ClientChunk from, Direction dir) {
        // We'll check transparency across the shared face between chunks
        int faceSize = 16;

        for (int i = 0; i < faceSize; i++) {
            for (int j = 0; j < faceSize; j++) {
                int x = dir == Direction.EAST ? (ClientWorld.CS - 1) : dir == Direction.WEST ? 0 : i;
                int y = dir == Direction.UP ? (ClientWorld.CS - 1) : dir == Direction.DOWN ? 0 : j;
                int z = dir == Direction.SOUTH ? (ClientWorld.CS - 1) : dir == Direction.NORTH ? 0 : j;

                if (dir.getOffsetX() != 0) z = i;
                if (dir.getOffsetY() != 0) x = i;
                if (dir.getOffsetZ() != 0) x = i;

                BlockState state = from.get(x, y, z);
                if (state.isTransparent()) return true;
            }
        }

        return false;
    }

    public abstract @Nullable RenderType renderTypeFor(@Nullable RenderMaterial renderMaterial);

    public String getName() {
        return name;
    }

    public abstract boolean isTransparent();

    public abstract Texture[] getTextures();

    protected final RuntimeException notEnabled() {
        return new RuntimeException(name + " render pass is not enabled!");
    }

    public void verify() {

    }

    public final void setSize(int width, int height) {
        if (width == this.width && height == this.height) return;

        this.width = width;
        this.height = height;

        resize(width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
