package dev.ultreon.quantum.client.model.block;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.model.model.Json5Model;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.client.resources.LoadableResource;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.BlockPos;

@SuppressWarnings("unused")
public interface BlockModel extends LoadableResource {

    Vector3 DEFAULT_ITEM_SCALE = new Vector3(1, 1, 1);

    boolean isCustom();

    default void render(Vector3 pos, RenderLayer renderLayer) {
        // Do nothing
    }

    default void loadInto(BlockPos pos, ClientChunk chunk) {
        // Do nothing
    }

    Model getModel();

    default void dispose() {
        ModelManager.INSTANCE.unloadModel(resourceId());
    }

    Identifier resourceId();

    default Vector3 getItemScale() {
        return new Vector3(0.0625f, 0.0625f, 0.0625f);
    }

    default Vector3 getItemOffset() {
        return new Vector3(0, -20, 0);
    }
}
