package dev.ultreon.quantum.client.model.blockbench.anim;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.client.model.blockbench.BBModelLoader;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class BBAnimator {
    private final String name;
    private final Type type;
    private final List<BBAnimKeyFrame> keyFrames;

    public BBAnimator(String name, Type type,
                      List<BBAnimKeyFrame> keyFrames) {
        this.name = name;
        this.type = type;
        this.keyFrames = keyFrames;
    }

    public Animation build(Node node, BBModelLoader modelData, Animation animation, Map<Node, NodeAnimation> nodeAnimationMap) {
        for (BBAnimKeyFrame keyFrame : keyFrames) {
            NodeAnimation nodeAnimation = nodeAnimationMap.computeIfAbsent(node, k -> {
                NodeAnimation created = new NodeAnimation();
                animation.nodeAnimations.add(created);
                return created;
            });
            nodeAnimation.node = node;

            switch (keyFrame.channel()) {
                case POSITION -> keyFrame.dataPoints().stream().map(vector3 -> new NodeKeyframe<>(keyFrame.time(), vector3)).collect(Collectors.toList()).forEach(nodeFrame -> {
                    if (nodeAnimation.translation == null) nodeAnimation.translation = new Array<>();
                    nodeAnimation.translation.add(nodeFrame);
                });
                case SCALE -> keyFrame.dataPoints().stream().map(vector3 -> new NodeKeyframe<>(keyFrame.time(), vector3)).collect(Collectors.toList()).forEach(nodeFrame -> {
                    if (nodeAnimation.scaling == null) nodeAnimation.scaling = new Array<>();
                    nodeAnimation.scaling.add(nodeFrame);
                });
                case ROTATION -> keyFrame.dataPoints().stream().map(vector3 -> {
                    Quaternion q = new Quaternion();
                    q.setEulerAngles(vector3.y, vector3.x, vector3.z);
                    return new NodeKeyframe<>(keyFrame.time(), q);
                }).collect(Collectors.toList()).forEach(nodeFrame -> {
                    if (nodeAnimation.rotation == null) nodeAnimation.rotation = new Array<>();
                    nodeAnimation.rotation.add(nodeFrame);
                });
            }
        }

        nodeAnimationMap.clear();
        return animation;
    }

    public String name() {
        return name;
    }

    public Type type() {
        return type;
    }

    public List<BBAnimKeyFrame> keyFrames() {
        return keyFrames;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBAnimator) obj;
        return Objects.equals(this.name, that.name) &&
               Objects.equals(this.type, that.type) &&
               Objects.equals(this.keyFrames, that.keyFrames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, keyFrames);
    }

    @Override
    public String toString() {
        return "BBAnimator[" +
               "name=" + name + ", " +
               "type=" + type + ", " +
               "keyFrames=" + keyFrames + ']';
    }


    public enum Type {
        BONE,
        CUBE
    }
}
