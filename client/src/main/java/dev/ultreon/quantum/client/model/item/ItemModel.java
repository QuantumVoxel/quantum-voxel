package dev.ultreon.quantum.client.model.item;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.resources.LoadableResource;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Collection;

public interface ItemModel extends LoadableResource {

    Vector3 DEFAULT_SCALE = new Vector3(1, 1, 1);

    Model getModel();

    default Vector3 getScale() {
        return DEFAULT_SCALE;
    }

    default Vector3 getOffset() {
        return Vector3.Zero;
    }

    Collection<NamespaceID> getAllTextures();

    void renderItem(Renderer renderer, ModelBatch batch, OrthographicCamera itemCam, Environment environment, int x, int y);
}
