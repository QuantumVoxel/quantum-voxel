package dev.ultreon.quantum.client.render.meshing;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.world.AOUtils;
import dev.ultreon.quantum.client.world.ChunkModelBuilder;
import dev.ultreon.quantum.client.world.ClientChunk;

import static dev.ultreon.quantum.world.World.CS;

public class FaceCullMesher implements Mesher {
    private final ClientChunk chunk;

    public FaceCullMesher(ClientChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public boolean buildMesh(UseCondition condition, ChunkModelBuilder builder1) {
        // Part 1: Default world
        boolean flag = false;
        for (int x = 0; x < CS; x++) {
            for (int y = 0; y < CS; y++) {
                for (int z = 0; z < CS; z++) {
                    flag |= loadBlockInto(builder1, x, y, z);
                }
            }
        }

        return flag;
    }

    private boolean loadBlockInto(
            ChunkModelBuilder meshPartBuilder,
            int x,
            int y,
            int z
    ) {
        final var block = chunk.getSafe(x, y, z);
        if (!block.isAir()) {
            final var model = QuantumClient.get().getBlockModel(block);
            BlockState back = chunk.getSafe(x, y, z - 1);
            BlockState front = chunk.getSafe(x, y, z + 1);
            BlockState left = chunk.getSafe(x - 1, y, z);
            BlockState right = chunk.getSafe(x + 1, y, z);
            BlockState top = chunk.getSafe(x, y + 1, z);
            BlockState bottom = chunk.getSafe(x, y - 1, z);

            long light = Light.of(
                    (byte) chunk.getLight(x, y - 1, z),
                    (byte) chunk.getLight(x, y + 1, z),
                    (byte) chunk.getLight(x, y, z - 1),
                    (byte) chunk.getLight(x - 1, y, z),
                    (byte) chunk.getLight(x, y, z + 1),
                    (byte) chunk.getLight(x + 1, y, z)
            );

            model.bakeInto(
                    meshPartBuilder.get(model.getRenderPass()), x, y, z, FaceCull.of(
                            !top.isTransparent() && top.hasCollider(),
                            !bottom.isTransparent() && bottom.hasCollider(),
                            !front.isTransparent() && front.hasCollider(),
                            !right.isTransparent() && right.hasCollider(),
                            !back.isTransparent() && back.hasCollider(),
                            !left.isTransparent() && left.hasCollider()
                    ), AOUtils.calculate(chunk, x, y, z), light);
            return true;
        }
        return false;
    }

}
