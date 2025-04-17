package dev.ultreon.quantum.client.render.meshing;

import dev.ultreon.quantum.client.world.ChunkModelBuilder;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class NewGreedyMesher implements Mesher {
    @Override
    public boolean buildMesh(UseCondition condition, ChunkModelBuilder builder) {
        return false;
    }
}
