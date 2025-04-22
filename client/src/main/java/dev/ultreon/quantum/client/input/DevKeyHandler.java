package dev.ultreon.quantum.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;

public class DevKeyHandler {

    void handleViewMode(KeyAndMouseInput keyAndMouseInput) {
        if (KeyAndMouseInput.isAltDown() && GamePlatform.get().isDevEnvironment())
            if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) keyAndMouseInput.client.viewMode = 0;
            else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) keyAndMouseInput.client.viewMode = 1;
            else if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) keyAndMouseInput.client.viewMode = 2;
            else if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) keyAndMouseInput.client.viewMode = 3;
            else if (Gdx.input.isKeyPressed(Input.Keys.NUM_5)) keyAndMouseInput.client.viewMode = 4;
            else if (Gdx.input.isKeyPressed(Input.Keys.NUM_6)) keyAndMouseInput.client.viewMode = 5;
    }

    void handleDevKeys(KeyAndMouseInput keyAndMouseInput) {
        if (KeyAndMouseInput.isCtrlDown() && GamePlatform.get().isDevEnvironment())
            if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
                TerrainRenderer worldRenderer = keyAndMouseInput.client.worldRenderer;
                if (worldRenderer != null) worldRenderer.reloadChunks();
            } else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
                ClientWorldAccess world = keyAndMouseInput.client.world;
                if (world instanceof ClientWorld) {
                    ClientWorld clientWorld = (ClientWorld) world;
                    clientWorld.toggleGizmoCategory("entity-bounds");
                }
            }
    }
}
