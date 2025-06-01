package dev.ultreon.quantum.client.item;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.item.BlockItemModel;
import dev.ultreon.quantum.client.model.item.ItemModel;
import dev.ultreon.quantum.client.model.item.ItemModelRegistry;
import dev.ultreon.quantum.item.BlockItem;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemRenderer implements Disposable {
    private final QuantumClient client;
    private Environment environment;
    private final ModelBatch batch;
    private OrthographicCamera itemCam;
    private final Quaternion quaternion = new Quaternion();
    private final Vector3 rotation = new Vector3(-30, 45, 0);
    private final Vector3 position = new Vector3(0, 0, -1000);
    private final Vector3 scale = new Vector3(20, 20, 20);
    protected final Vector3 tmp = new Vector3();
    private final Map<BlockState, ModelInstance> blockModelCache = new HashMap<>();
    private final Map<Item, ModelInstance> itemModelCache = new HashMap<>();

    public ItemRenderer(QuantumClient client) {
        this.client = client;
        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.0f, 0.0f, 0.0f, 1f));
        this.environment.add(new DirectionalLight().set(.8f, .8f, .8f, this.tmp.set(.8f, 0, -.6f).rotate(Vector3.Y, 45)));
        this.environment.add(new DirectionalLight().set(.8f, .8f, .8f, this.tmp.set(-.8f, 0, .6f).rotate(Vector3.Y, 45)));
        this.environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, this.tmp.set(0, -1, 0).rotate(Vector3.Y, 45)));
        this.environment.add(new DirectionalLight().set(0.17f, .17f, .17f, this.tmp.set(0, 1, 0).rotate(Vector3.Y, 45)));
        this.batch = new ModelBatch();
        this.itemCam = new OrthographicCamera(client.getScaledWidth(), client.getScaledHeight());
    }

    public void render(Item item, Renderer renderer, int x, int y) {
        render(item, renderer, x, y, 0);
    }

    public void render(Item item, Renderer renderer, int x, int y, int offset) {
        if (item == null || item == Items.AIR) {
            return;
        }

        renderItem(item, renderer, x, y);
    }

    private void renderBlockItem(Item item, BlockState block, Renderer renderer, int x, int y) {
        renderer.external(() -> {
            float guiScale = this.client.getGuiScale();
            this.itemCam.zoom = 4.0f / guiScale;
            this.itemCam.far = 100000;
            this.itemCam.update();
            @NotNull BlockModel blockModel = this.client.getBlockModel(block);

            ItemModel itemModel = client.getItemModel(item);
            ModelInstance modelInstance;
            if (itemModel != null) {
                modelInstance = new ModelInstance(itemModel.getModel());
                this.blockModelCache.put(block, modelInstance);
            } else {
                Model model = blockModel.getModel();
                modelInstance = new ModelInstance(model == null ? BakedCubeModel.defaultModel().getModel() : model);
                this.blockModelCache.put(block, modelInstance);
            }
            this.batch.render(modelInstance, this.environment);
        });
    }

    private void renderItem(Item item, Renderer renderer, int x, int y) {
        @Nullable ItemModel itemModel = this.client.getItemModel(item);
        if (itemModel != null) {
            itemModel.renderItem(renderer, batch, itemCam, environment, x, y);
            return;
        }

        BakedCubeModel.defaultModel().renderItem(renderer, batch, itemCam, environment, x, y);
    }

    public OrthographicCamera getItemCam() {
        return this.itemCam;
    }

    public void resize(int width, int height) {
        this.itemCam.viewportWidth = width / this.client.getGuiScale();
        this.itemCam.viewportHeight = height / this.client.getGuiScale();
        this.itemCam.update(true);
    }

    public ModelInstance createModelInstance(ItemStack stack) {
        ItemModel itemModel = ItemModelRegistry.get().get(stack.getItem());
        if (itemModel == null) {
            ModelInstance modelInstance = new ModelInstance(BakedCubeModel.defaultModel().getModel());
            modelInstance.userData = new BlockItemModel(stack.getItem(), BakedCubeModel::defaultModel);
            return modelInstance;
        }
        Model model = itemModel.getModel();
        if (model == null) model = BakedCubeModel.defaultModel().getModel();
        ModelInstance modelInstance = new ModelInstance(model);
        modelInstance.userData = itemModel;
        return modelInstance;
    }

    @Override
    public void dispose() {
        this.blockModelCache.clear();
        this.batch.dispose();

        this.itemCam = null;
        this.environment = null;
    }
}
