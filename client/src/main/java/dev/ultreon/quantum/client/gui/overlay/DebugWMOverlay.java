package dev.ultreon.quantum.client.gui.overlay;

import com.badlogic.gdx.Gdx;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.overlay.wm.WindowManager;

public class DebugWMOverlay extends Overlay {

    @Override
    protected void render(Renderer renderer, float deltaTime) {
        WindowManager.render(renderer, (int) (Gdx.input.getX() / getGuiScale()), (int) (Gdx.input.getY() / getGuiScale()), deltaTime);
    }

    private float getGuiScale() {
        return this.client.getGuiScale();
    }
}
