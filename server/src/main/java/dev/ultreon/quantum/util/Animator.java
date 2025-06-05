package dev.ultreon.quantum.util;

import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import dev.ultreon.quantum.component.GameComponent;

/**
 * @deprecated use {@link AnimationController} instead
 */
@Deprecated(since = "0.2.0", forRemoval = true)
public class Animator extends GameComponent {
    private final AnimationController controller;

    public Animator(AnimationController controller) {
        this.controller = controller;
    }

    public AnimationController getController() {
        return controller;
    }
}
