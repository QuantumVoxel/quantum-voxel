package dev.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.math.Matrix4;
import dev.ultreon.libs.commons.v0.vector.Vec3f;

import java.util.UUID;

public interface BBModelNode {
    Matrix4 rotationMatrix();

    Vec3f origin();

    UUID uuid();

    Vec3f rotation();

    BBModelNode parent();
}
