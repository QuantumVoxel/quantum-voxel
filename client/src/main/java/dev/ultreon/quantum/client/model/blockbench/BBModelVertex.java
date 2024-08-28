package dev.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import dev.ultreon.quantum.util.Vec2f;
import dev.ultreon.quantum.util.Vec3f;

import java.util.Objects;

public record BBModelVertex(Vec3f vertex) {
    public void write(MeshPartBuilder builder, Vec2f resolution) {
        builder.vertex(vertex.x / 16f, vertex.y / 16f, vertex.z / 16f);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBModelVertex) obj;
        return Objects.equals(this.vertex, that.vertex);
    }

    @Override
    public String toString() {
        return "BBModelVertex[" +
               "vertex=" + vertex + ']';
    }

}
