package com.ultreon.quantum.client.model.blockbench.anim;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.google.common.annotations.Beta;
import com.ultreon.quantum.client.model.blockbench.BBModelLoader;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@Beta
@ApiStatus.Experimental
public record BBAnimation(UUID uuid, String name,
                          Loop loop, boolean override,
                          float length, int snapping, boolean selected, String animTimeUpdate, String blendWeight,
                          String startDelay, String loopDelay, Map<UUID, BBAnimator> animators) {

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

    public enum Loop {
        LOOP,
        ONCE,
    }
}
