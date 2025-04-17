package dev.ultreon.quantum.client.render.meshing;

import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.registry.BlockRenderPassRegistry;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.world.ChunkModelBuilder;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;

import static dev.ultreon.quantum.client.world.AOUtils.*;

public class CulledMesher implements Mesher {
    private final ClientChunk chunk;

    public CulledMesher(ClientChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public boolean buildMesh(UseCondition condition, ChunkModelBuilder builder) {
        for (int x = 0; x < World.CS; x++)
            for (int y = 0; y < World.CS; y++)
                for (int z = 0; z < World.CS; z++) bakeBlock(condition, builder, x, y, z);
        return true;
    }

    private void bakeBlock(UseCondition condition, ChunkModelBuilder builder, int x, int y, int z) {
        BlockState state = this.chunk.get(x, y, z);
        BlockModel model = QuantumClient.get().getBlockModel(state);
        RenderPass renderPass = BlockRenderPassRegistry.get(state.getBlock());
        if (state.isInvisible()) return;
        if (condition.shouldUse(state.getBlock(), model, renderPass)) {
            // bits: up,down,north,east,south,west
            int[][] data = createRenderData(chunk, x, y, z);
            int[] ao = data[1];
            int cullface = data[0][0];
            if (cullface == 0b111111) return; // Fully culled out
            model.bakeInto(x, y, z, cullface, renderPass, this.chunk, builder, ao);
        }
    }

    private int[][] createRenderData(ClientChunk chunk, int x, int y, int z) {
        int cullface = 0;
        int[] ao = new int[]{16, 16, 16, 16, 16, 16};
        boolean isAllFull = true;
        for (Direction dir : Direction.values()) {
            BlockVec neighbour = new BlockVec(x, y, z).relative(dir);
            if (!chunk.getSafe(neighbour.x, neighbour.y, neighbour.z).isInvisible()) {
                cullface |= 1 << dir.ordinal();
            } else {
                isAllFull = false;
            }

            Vector3 point = new Vector3(x + dir.getNormal().x, y + dir.getNormal().y, z + dir.getNormal().z);
            BlockModel block = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z);
            if (!block.hasAO()) {
                int aoValue = calcAoForDir(chunk, dir, point);
                ao[dir.ordinal()] = aoValue;
            }
        }
        if (isAllFull) cullface = 0b111111;
        return new int[][]{new int[]{cullface}, ao};
    }

    private static int calcAoForDir(ClientChunk chunk, Direction dir, Vector3 point) {
        return switch (dir.getAxis()) {
            case Y -> calcAoForY(chunk, dir, point);
            case X -> calcAoForX(chunk, dir, point);
            case Z -> calcAoForZ(chunk, point);
        };
    }

    private static int calcAoForZ(ClientChunk chunk, Vector3 point) {
        BlockModel westUp = getModelAt(chunk, (int) point.x - 1, (int) point.y + 1, (int) point.z);
        BlockModel up = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z);
        BlockModel west = getModelAt(chunk, (int) point.x - 1, (int) point.y, (int) point.z);
        BlockModel eastUp = getModelAt(chunk, (int) point.x + 1, (int) point.y + 1, (int) point.z);
        BlockModel east = getModelAt(chunk, (int) point.x + 1, (int) point.y, (int) point.z);
        BlockModel eastDown = getModelAt(chunk, (int) point.x + 1, (int) point.y - 1, (int) point.z);
        BlockModel down = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z);
        BlockModel westDown = getModelAt(chunk, (int) point.x - 1, (int) point.y - 1, (int) point.z);
        return createAO(
                westDown.hasAO() || down.hasAO() || west.hasAO(),
                westUp.hasAO() || up.hasAO() || west.hasAO(),
                eastDown.hasAO() || down.hasAO() || east.hasAO(),
                eastUp.hasAO() || up.hasAO() || east.hasAO()
        );
    }

    @SuppressWarnings("t")
    private static int calcAoForX(ClientChunk chunk, Direction dir, Vector3 point) {
        BlockModel northUp = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z + 1);
        BlockModel up = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z);
        BlockModel north = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z + 1);
        BlockModel southUp = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z - 1);
        BlockModel south = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z - 1);
        BlockModel southDown = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z - 1);
        BlockModel down = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z);
        BlockModel northDown = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z + 1);
        if (dir.isNegative()) return createAO(
                southDown.hasAO() || down.hasAO() || south.hasAO(),
                southUp.hasAO() || up.hasAO() || south.hasAO(),
                northDown.hasAO() || down.hasAO() || north.hasAO(),
                northUp.hasAO() || up.hasAO() || north.hasAO()
        );
        return createAO(
                northDown.hasAO() || down.hasAO() || north.hasAO(),
                northUp.hasAO() || up.hasAO() || north.hasAO(),
                southDown.hasAO() || down.hasAO() || south.hasAO(),
                southUp.hasAO() || up.hasAO() || south.hasAO()
        );
    }

    @SuppressWarnings("t")
    private static int calcAoForY(ClientChunk chunk, Direction dir, Vector3 point) {
        BlockModel northWest = getModelAt(chunk, (int) point.x - 1, (int) point.y, (int) point.z - 1);
        BlockModel west = getModelAt(chunk, (int) point.x - 1, (int) point.y, (int) point.z);
        BlockModel north = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z - 1);
        BlockModel southWest = getModelAt(chunk, (int) point.x - 1, (int) point.y, (int) point.z + 1);
        BlockModel south = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z + 1);
        BlockModel southEast = getModelAt(chunk, (int) point.x + 1, (int) point.y, (int) point.z + 1);
        BlockModel east = getModelAt(chunk, (int) point.x + 1, (int) point.y, (int) point.z);
        BlockModel northEast = getModelAt(chunk, (int) point.x + 1, (int) point.y, (int) point.z - 1);
        if (dir.isNegative()) return createAO(
                northWest.hasAO() || west.hasAO() || north.hasAO(),
                southWest.hasAO() || west.hasAO() || south.hasAO(),
                northEast.hasAO() || east.hasAO() || north.hasAO(),
                southEast.hasAO() || east.hasAO() || south.hasAO()
        );
        return flipped(createAO(
                northEast.hasAO() || east.hasAO() || north.hasAO(),
                southEast.hasAO() || east.hasAO() || south.hasAO(),
                northWest.hasAO() || west.hasAO() || north.hasAO(),
                southWest.hasAO() || west.hasAO() || south.hasAO()
        ));
    }
}
