package dev.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import dev.ultreon.libs.commons.v0.vector.Vec2f;
import dev.ultreon.libs.commons.v0.vector.Vec3f;

public record BBModelVertex(Vec3f vertex) {
    public void write(MeshPartBuilder builder, Vec2f resolution) {
        builder.vertex(vertex.x / 16f, vertex.y / 16f, vertex.z / 16f);
    }
}
