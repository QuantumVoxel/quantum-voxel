package dev.ultreon.quantum.client.model.block;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.render.NodeCategory;
import dev.ultreon.quantum.client.resources.LoadableResource;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface BlockModel extends LoadableResource {

    Vector3 DEFAULT_ITEM_SCALE = new Vector3(1, 1, 1);

    boolean isCustom();

    default void render(Vector3 pos, NodeCategory nodeCategory) {
        // Do nothing
    }

    default void loadInto(BlockVec pos, ClientChunk chunk) {
        // Do nothing
    }

    Model getModel();

    default void dispose() {
        ModelManager.INSTANCE.unloadModel(resourceId());
    }

    NamespaceID resourceId();

    default Vector3 getItemScale() {
        return new Vector3(0.0625f, 0.0625f, 0.0625f);
    }

    default Vector3 getItemOffset() {
        return new Vector3(0, -20, 0);
    }

    default @Nullable TextureRegion getBuriedTexture() {
        return null;
    }

    default boolean hasAO() {
        return false;
    }

    RenderPass getRenderPass();
}
