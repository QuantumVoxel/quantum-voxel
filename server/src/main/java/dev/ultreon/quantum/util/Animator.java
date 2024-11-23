package dev.ultreon.quantum.util;

import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import dev.ultreon.quantum.component.GameComponent;

@Deprecated
public class Animator extends GameComponent {
    private final AnimationController controller;

    public Animator(AnimationController controller) {
        this.controller = controller;
    }

    public AnimationController getController() {
        return controller;
    }
}
