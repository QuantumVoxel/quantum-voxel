package com.ultreon.quantum.client.model.blockbench.anim;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.ultreon.quantum.client.model.blockbench.BBModelLoader;

import java.util.*;

public record BBAnimator(String name, Type type,
                         List<BBAnimKeyFrame> keyFrames) {

    public Animation build(Node node, BBModelLoader modelData, Animation animation, Map<Node, NodeAnimation> nodeAnimationMap) {
        for (BBAnimKeyFrame keyFrame : keyFrames) {
            NodeAnimation nodeAnimation = nodeAnimationMap.computeIfAbsent(node, k -> {
                NodeAnimation created = new NodeAnimation();
                animation.nodeAnimations.add(created);
                return created;
            });
            nodeAnimation.node = node;

            switch (keyFrame.channel()) {
                case POSITION -> keyFrame.dataPoints().stream().map(vector3 -> new NodeKeyframe<>(keyFrame.time(), vector3)).toList().forEach(nodeFrame -> {
                    if (nodeAnimation.translation == null) nodeAnimation.translation = new Array<>();
                    nodeAnimation.translation.add(nodeFrame);
                });
                case SCALE -> keyFrame.dataPoints().stream().map(vector3 -> new NodeKeyframe<>(keyFrame.time(), vector3)).toList().forEach(nodeFrame -> {
                    if (nodeAnimation.scaling == null) nodeAnimation.scaling = new Array<>();
                    nodeAnimation.scaling.add(nodeFrame);
                });
                case ROTATION -> keyFrame.dataPoints().stream().map(vector3 -> {
                    Quaternion q = new Quaternion();
                    q.setEulerAngles(vector3.y, vector3.x, vector3.z);
                    return new NodeKeyframe<>(keyFrame.time(), q);
                }).toList().forEach(nodeFrame -> {
                    if (nodeAnimation.rotation == null) nodeAnimation.rotation = new Array<>();
                    nodeAnimation.rotation.add(nodeFrame);
                });
            }
        }

        nodeAnimationMap.clear();
        return animation;
    }

    public enum Type {
        BONE,
        CUBE
    }
}
