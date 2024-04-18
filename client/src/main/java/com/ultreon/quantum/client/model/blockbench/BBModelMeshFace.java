package com.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.libs.commons.v0.vector.Vec2f;

import java.util.List;
import java.util.Map;

public record BBModelMeshFace(Map<String, Vec2f> uvs, List<BBModelVertex> vertices, int texture) {
    public void write(ModelBuilder builder, Map<Integer, BBTexture> texture2builder, Vec2f resolution) {
//        MeshBuilder meshBuilder = texture2builder.get(texture);
//        meshBuilder.setUVRange(0, 0, resolution.x, resolution.y); // FIXME is this right?
//        for (BBModelVertex vertex : vertices) {
//            vertex.write(meshBuilder, resolution);
//        }
    }
}
