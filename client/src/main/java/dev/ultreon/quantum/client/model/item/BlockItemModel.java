package dev.ultreon.quantum.client.model.item;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Collection;
import java.util.function.Supplier;

public class BlockItemModel implements ItemModel {
    private final Item item;
    private final Supplier<BlockModel> blockModel;
    private ModelInstance modelInstance;

    public BlockItemModel(Item item, Supplier<BlockModel> blockModel) {
        this.item = item;
        this.blockModel = blockModel;
    }

    @Override
    public void load(QuantumClient client) {
        // Block models are loaded externally.
    }

    @Override
    public NamespaceID resourceId() {
        return this.blockModel.get().resourceId();
    }

    @Override
    public Model getModel() {
        return this.blockModel.get().getModel();
    }

    @Override
    public Vector3 getScale() {
        return this.blockModel.get().getItemScale();
    }

    @Override
    public Vector3 getOffset() {
        return this.blockModel.get().getItemOffset();
    }

    @Override
    public Collection<NamespaceID> getAllTextures() {
        return blockModel.get().getAllTextures();
    }

    @Override
    public void renderItem(Renderer renderer, ModelBatch batch, OrthographicCamera itemCam, Environment environment, int x, int y) {
        renderer.external(() -> {
            float guiScale = QuantumClient.get().getGuiScale();
            itemCam.zoom = 4.0f / guiScale;
            itemCam.far = 100000;
            itemCam.update();

            if (modelInstance == null) {
                BlockModel blockModel = this.blockModel.get();
                modelInstance = new ModelInstance(blockModel.getModel());
                modelInstance.transform.scale(1 / 16f, 1 / 16f, 1 / 16f);
            }
            batch.render(modelInstance, environment);

        });
    }
}
