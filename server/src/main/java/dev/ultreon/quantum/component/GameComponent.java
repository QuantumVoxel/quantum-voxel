package dev.ultreon.quantum.component;

import dev.ultreon.quantum.util.GameObject;

public abstract class GameComponent extends Component<GameObject> {
    protected GameComponent() {
        super(GameObject.class);
    }

    public void update(float deltaTime) {

    }
}
