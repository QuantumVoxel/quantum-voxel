package dev.ultreon.quantum.client.model.blockbench.anim;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.google.common.annotations.Beta;
import dev.ultreon.quantum.client.model.blockbench.BBModelLoader;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Beta
@ApiStatus.Experimental
public final class BBAnimation {
    private final UUID uuid;
    private final String name;
    private final Loop loop;
    private final boolean override;
    private final float length;
    private final int snapping;
    private final boolean selected;
    private final String animTimeUpdate;
    private final String blendWeight;
    private final String startDelay;
    private final String loopDelay;
    private final Map<UUID, BBAnimator> animators;

    public BBAnimation(UUID uuid, String name,
                       Loop loop, boolean override,
                       float length, int snapping, boolean selected, String animTimeUpdate, String blendWeight,
                       String startDelay, String loopDelay, Map<UUID, BBAnimator> animators) {
        this.uuid = uuid;
        this.name = name;
        this.loop = loop;
        this.override = override;
        this.length = length;
        this.snapping = snapping;
        this.selected = selected;
        this.animTimeUpdate = animTimeUpdate;
        this.blendWeight = blendWeight;
        this.startDelay = startDelay;
        this.loopDelay = loopDelay;
        this.animators = animators;
    }

    public Animation create(Map<UUID, Node> nodes, BBModelLoader modelData) {
        Map<Node, NodeAnimation> nodeAnimationMap = new HashMap<>();

        var animation = new Animation();
        for (Map.Entry<UUID, BBAnimator> entry : animators.entrySet()) {
            entry.getValue().build(nodes.get(entry.getKey()), modelData, animation, nodeAnimationMap);
        }

        animation.id = name;
        animation.duration = length;
        return animation;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public Loop loop() {
        return loop;
    }

    public boolean override() {
        return override;
    }

    public float length() {
        return length;
    }

    public int snapping() {
        return snapping;
    }

    public boolean selected() {
        return selected;
    }

    public String animTimeUpdate() {
        return animTimeUpdate;
    }

    public String blendWeight() {
        return blendWeight;
    }

    public String startDelay() {
        return startDelay;
    }

    public String loopDelay() {
        return loopDelay;
    }

    public Map<UUID, BBAnimator> animators() {
        return animators;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBAnimation) obj;
        return Objects.equals(this.uuid, that.uuid) &&
               Objects.equals(this.name, that.name) &&
               Objects.equals(this.loop, that.loop) &&
               this.override == that.override &&
               Float.floatToIntBits(this.length) == Float.floatToIntBits(that.length) &&
               this.snapping == that.snapping &&
               this.selected == that.selected &&
               Objects.equals(this.animTimeUpdate, that.animTimeUpdate) &&
               Objects.equals(this.blendWeight, that.blendWeight) &&
               Objects.equals(this.startDelay, that.startDelay) &&
               Objects.equals(this.loopDelay, that.loopDelay) &&
               Objects.equals(this.animators, that.animators);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, loop, override, length, snapping, selected, animTimeUpdate, blendWeight, startDelay, loopDelay, animators);
    }

    @Override
    public String toString() {
        return "BBAnimation[" +
               "uuid=" + uuid + ", " +
               "name=" + name + ", " +
               "loop=" + loop + ", " +
               "override=" + override + ", " +
               "length=" + length + ", " +
               "snapping=" + snapping + ", " +
               "selected=" + selected + ", " +
               "animTimeUpdate=" + animTimeUpdate + ", " +
               "blendWeight=" + blendWeight + ", " +
               "startDelay=" + startDelay + ", " +
               "loopDelay=" + loopDelay + ", " +
               "animators=" + animators + ']';
    }


    public enum Loop {
        LOOP,
        ONCE,
    }
}
