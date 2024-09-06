package dev.ultreon.quantum.client.model.blockbench.anim;

import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.model.blockbench.BBColor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class BBAnimKeyFrame {
    private final BBAnimChannel channel;
    private final List<Vector3> dataPoints;
    private final UUID uuid;
    private final float time;
    private final BBColor color;
    private final BBAnimInterpolation interpolation;
    private final boolean bezierLinked;
    private final Vector3 bezierLeftTime;
    private final Vector3 bezierLeftValue;
    private final Vector3 bezierRightTime;
    private final Vector3 bezierRightValue;

    public BBAnimKeyFrame(BBAnimChannel channel, List<Vector3> dataPoints, UUID uuid, float time, BBColor color,
                          BBAnimInterpolation interpolation, boolean bezierLinked, Vector3 bezierLeftTime,
                          Vector3 bezierLeftValue, Vector3 bezierRightTime, Vector3 bezierRightValue) {
        this.channel = channel;
        this.dataPoints = dataPoints;
        this.uuid = uuid;
        this.time = time;
        this.color = color;
        this.interpolation = interpolation;
        this.bezierLinked = bezierLinked;
        this.bezierLeftTime = bezierLeftTime;
        this.bezierLeftValue = bezierLeftValue;
        this.bezierRightTime = bezierRightTime;
        this.bezierRightValue = bezierRightValue;
    }

    public List<Vector3> dataPoints() {
        return Collections.unmodifiableList(dataPoints);
    }

    public BBAnimChannel channel() {
        return channel;
    }

    public UUID uuid() {
        return uuid;
    }

    public float time() {
        return time;
    }

    public BBColor color() {
        return color;
    }

    public BBAnimInterpolation interpolation() {
        return interpolation;
    }

    public boolean bezierLinked() {
        return bezierLinked;
    }

    public Vector3 bezierLeftTime() {
        return bezierLeftTime;
    }

    public Vector3 bezierLeftValue() {
        return bezierLeftValue;
    }

    public Vector3 bezierRightTime() {
        return bezierRightTime;
    }

    public Vector3 bezierRightValue() {
        return bezierRightValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBAnimKeyFrame) obj;
        return Objects.equals(this.channel, that.channel) &&
               Objects.equals(this.dataPoints, that.dataPoints) &&
               Objects.equals(this.uuid, that.uuid) &&
               Float.floatToIntBits(this.time) == Float.floatToIntBits(that.time) &&
               Objects.equals(this.color, that.color) &&
               Objects.equals(this.interpolation, that.interpolation) &&
               this.bezierLinked == that.bezierLinked &&
               Objects.equals(this.bezierLeftTime, that.bezierLeftTime) &&
               Objects.equals(this.bezierLeftValue, that.bezierLeftValue) &&
               Objects.equals(this.bezierRightTime, that.bezierRightTime) &&
               Objects.equals(this.bezierRightValue, that.bezierRightValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, dataPoints, uuid, time, color, interpolation, bezierLinked, bezierLeftTime, bezierLeftValue, bezierRightTime, bezierRightValue);
    }

    @Override
    public String toString() {
        return "BBAnimKeyFrame[" +
               "channel=" + channel + ", " +
               "dataPoints=" + dataPoints + ", " +
               "uuid=" + uuid + ", " +
               "time=" + time + ", " +
               "color=" + color + ", " +
               "interpolation=" + interpolation + ", " +
               "bezierLinked=" + bezierLinked + ", " +
               "bezierLeftTime=" + bezierLeftTime + ", " +
               "bezierLeftValue=" + bezierLeftValue + ", " +
               "bezierRightTime=" + bezierRightTime + ", " +
               "bezierRightValue=" + bezierRightValue + ']';
    }

}
