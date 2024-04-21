package com.ultreon.quantum.client.model.block;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.quantum.client.render.Scene3D;
import com.ultreon.quantum.client.resources.LoadableResource;
import com.ultreon.quantum.client.world.ClientChunk;
import com.ultreon.quantum.world.BlockPos;

@SuppressWarnings("unused")
public interface BlockModel extends Disposable, LoadableResource {

    Vector3 DEFAULT_ITEM_SCALE = new Vector3(1, 1, 1);

    boolean isCustom();

    default void render(Vector3 pos, Scene3D scene3D) {
        // Do nothing
    }

    default void loadInto(BlockPos pos, ClientChunk chunk) {
        // Do nothing
    }

    Model getModel();

    default Vector3 getItemScale() {
        return DEFAULT_ITEM_SCALE;
    }

    default Vector3 getItemOffset() {
        return Vector3.Zero;
    }
}
