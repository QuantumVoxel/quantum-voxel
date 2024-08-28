package dev.ultreon.quantum.client.input.dyn;

import com.badlogic.gdx.math.Vector2;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.client.input.controller.ControllerVec2;

public interface Vec2Convertible<T> {
    default Pair<ControllerVec2, Vector2> asVec2(T value) {
        return this.asVec2(value, new Vector2());
    }

    Pair<ControllerVec2, Vector2> asVec2(T value, Vector2 result);
}
