package dev.ultreon.quantum.client.model;

import com.badlogic.gdx.graphics.g3d.Model;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.util.NamespaceID;

/**
 * Base class for all baked models.
 * <p>
 * Baked models are created by the {@link ModelManager} based on {@link dev.ultreon.quantum.client.model.block.BlockModel}
 * and {@link dev.ultreon.quantum.client.model.item.ItemModel}.
 */
public abstract class BakedModel {

    /**
     * The 3D model.
     */
    private final Model model;

    /**
     * The resource identifier.
     */
    private final NamespaceID resourceId;

    /**
     * Creates a new baked model.
     *
     * @param resourceId the resource identifier
     * @param model      the 3D model
     */
    public BakedModel(NamespaceID resourceId, Model model) {
        this.resourceId = resourceId;
        this.model = model;

        // Register the model with the model manager
        ModelManager.INSTANCE.add(resourceId, model);
    }

    /**
     * Returns the 3D model.
     *
     * @return the 3D model
     */
    public Model getModel() {
        if (model == null) throw new IllegalStateException("Model not loaded: " + resourceId());
        return model;
    }

    /**
     * Returns the resource identifier.
     *
     * @return the resource identifier
     */
    public final NamespaceID resourceId() {
        return resourceId;
    }
}
