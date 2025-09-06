package dev.ultreon.quantum.client.render.meshing;

import dev.ultreon.quantum.client.render.world.ChunkModelBuilder;

public interface ChunkElement {

    @SuppressWarnings("unused")
    void bake(ChunkModelBuilder modelBuilder);
}
