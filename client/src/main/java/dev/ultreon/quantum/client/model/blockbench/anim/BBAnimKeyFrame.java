package dev.ultreon.quantum.client.model.blockbench.anim;

import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.model.blockbench.BBColor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record BBAnimKeyFrame(BBAnimChannel channel, List<Vector3> dataPoints, UUID uuid, float time, BBColor color,
                             BBAnimInterpolation interpolation, boolean bezierLinked, Vector3 bezierLeftTime,
                             Vector3 bezierLeftValue, Vector3 bezierRightTime, Vector3 bezierRightValue) {

    @Override
    public List<Vector3> dataPoints() {
        return Collections.unmodifiableList(dataPoints);
    }
}
