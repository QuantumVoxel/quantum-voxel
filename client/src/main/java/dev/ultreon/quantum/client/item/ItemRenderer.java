package dev.ultreon.quantum.client.item;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.util.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.model.item.BlockItemModel;
import dev.ultreon.quantum.client.model.item.FlatItemModel;
import dev.ultreon.quantum.client.model.item.ItemModel;
import dev.ultreon.quantum.client.model.model.Json5ModelLoader;
import dev.ultreon.quantum.item.BlockItem;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ItemRenderer implements Disposable {
    private final QuantumClient client;
    private Environment environment;
    private final ModelBatch batch;
    private OrthographicCamera itemCam;
    private Material material;
    private final Quaternion quaternion = new Quaternion();
    private final Vector3 rotation = new Vector3(-30, 45, 0);
    private final Vector3 position = new Vector3(0, 0, -1000);
    private final Vector3 scale = new Vector3(20, 20, 20);
    protected final Vector3 tmp = new Vector3();
    private final Map<Item, ItemModel> models = new HashMap<>();
    private final Map<Item, ModelInstance> modelsInstances = new HashMap<>();
    private Cache<BlockProperties, ModelInstance> blockModelCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();

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
        this.material = new Material(new TextureAttribute(TextureAttribute.Diffuse, this.client.blocksTextureAtlas.getTexture()));
        this.material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        this.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
    }

    public void render(Item item, Renderer renderer, int x, int y) {
        if (item == null || item == Items.AIR) {
            return;
        }

        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) item;
            ModelInstance modelInstance = modelsInstances.get(item);
            if (modelInstance != null) {
                this.renderModel(modelInstance, models.get(item), renderer, x + 8, this.client.getScaledHeight() - y - 16);
                return;
            }

            this.renderBlockItem(blockItem,blockItem.createBlockMeta(), renderer, x + 8, this.client.getScaledHeight() - y - 16);
            return;
        }

        Identifier curKey = Registries.ITEM.getId(item);
        if (curKey == null) {
            renderer.blitColor(RgbColor.WHITE);
            renderer.blit((TextureRegion) null, x, y, 16, 16);
        } else {
            TextureRegion texture = this.client.itemTextureAtlas.get(curKey.mapPath(path -> "textures/items/" + path + ".png"));
            renderer.blitColor(RgbColor.WHITE);
            renderer.blit(texture, x, y, 16, 16);
        }
    }

    private void renderModel(ModelInstance instance, ItemModel itemModel, Renderer renderer, int x, int y) {
        if (instance != null) {
            renderer.external(() -> {
                float guiScale = this.client.getGuiScale();
                this.itemCam.zoom = 4.0f / guiScale;
                this.itemCam.far = 100000;
                this.itemCam.update();
                this.batch.begin(this.itemCam);
                Vector3 scl = this.scale.cpy().scl(itemModel.getScale());
                instance.transform.idt().translate(this.position.cpy().add((x - (int) (this.client.getScaledWidth() / 2.0F)) * guiScale, (y - (int) (this.client.getScaledHeight() / 2.0F)) * guiScale, 100)).translate(itemModel.getOffset().scl(1 / scl.x, 1 / scl.y, 1 / scl.z)).scale(scl.x, scl.y, scl.z);
                instance.transform.rotate(Vector3.X, this.rotation.x);
                instance.transform.rotate(Vector3.Y, this.rotation.y);
                this.batch.render(instance, environment);
                this.batch.end();
            });
        }
    }

    private void renderBlockItem(Item item, BlockProperties block, Renderer renderer, int x, int y) {
        renderer.external(() -> {
            float guiScale = this.client.getGuiScale();
            this.itemCam.zoom = 4.0f / guiScale;
            this.itemCam.far = 100000;
            this.itemCam.update();
            @NotNull BlockModel blockModel = this.client.getBlockModel(block);
            if (blockModel == BakedCubeModel.defaultModel()) {
                renderCustomBlock(item, block, renderer, x, y);
                return;
            }
            if (blockModel instanceof BakedCubeModel) {
                BakedCubeModel bakedModel = (BakedCubeModel) blockModel;
                this.batch.begin(this.itemCam);
                Mesh mesh = bakedModel.getMesh();
                Renderable renderable = renderer.obtainRenderable();
                renderable.meshPart.mesh = mesh;
                renderable.meshPart.center.set(0F, 0F, 0F);
                renderable.meshPart.offset = 0;
                renderable.meshPart.size = mesh.getMaxVertices();
                renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
                renderable.material = this.material;
                renderable.environment = this.environment;
                renderable.worldTransform.set(this.position.cpy().add((x - (int) (this.client.getScaledWidth() / 2.0F)) * guiScale, -(-y + (int) (this.client.getScaledHeight() / 2.0F)) * guiScale, 100), this.quaternion, this.scale);
                renderable.worldTransform.rotate(Vector3.X, this.rotation.x);
                renderable.worldTransform.rotate(Vector3.Y, this.rotation.y);
                this.batch.render(renderable);
                this.batch.end();
            } else {
                try {
                    ModelInstance modelInstance = this.blockModelCache.get(block, () -> new ModelInstance(blockModel.getModel()));
                    this.batch.render(modelInstance, this.environment);
                } catch (ExecutionException e) {
                    QuantumClient.LOGGER.warn("Error occurred while caching block model:", e);
                }
            }
        });
    }

    private void renderCustomBlock(Item item, BlockProperties block, Renderer renderer, int x, int y) {
        ModelInstance modelInstance = new ModelInstance(getModel(block));
        this.modelsInstances.put(item, modelInstance);

        renderModel(modelInstance, models.get(item), renderer, x, y);
    }

    private static Model getModel(BlockProperties block) {
        BlockModel blockModel = BlockModelRegistry.get(block);
        Model defaultModel = BakedCubeModel.defaultModel().getModel();
        if (blockModel == null) return defaultModel;
        Model model = blockModel.getModel();
        return model == null ? defaultModel : model;
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
        ItemModel itemModel = models.get(stack.getItem());
        if (itemModel == null) {
            ModelInstance modelInstance = new ModelInstance(BakedCubeModel.defaultModel().getModel());
            modelInstance.userData = new BlockItemModel(() -> BakedCubeModel.defaultModel());
            return modelInstance;
        }
        ModelInstance modelInstance = new ModelInstance(itemModel.getModel());
        modelInstance.userData = itemModel;
        return modelInstance;
    }

    public void registerModel(Item item, ItemModel model) {
        models.put(item, model);
    }

    public void registerBlockModel(BlockItem blockItem, Supplier<BlockModel> model) {
        models.put(blockItem, new BlockItemModel(Suppliers.memoize(model::get)));
        ItemModel itemModel = models.get(blockItem);
        Vector3 scale = itemModel.getScale();
        ModelInstance value = new ModelInstance(itemModel.getModel());
        value.transform.scale(scale.x, scale.y, scale.z);
        value.calculateTransforms();
        this.modelsInstances.put(blockItem, value);
    }

    public void loadModels(QuantumClient client) {
        for (Map.Entry<Item, ItemModel> e : models.entrySet()) {
            Item item = e.getKey();
            ItemModel value = e.getValue();
            value.load(client);

            Model model = value.getModel();
            this.modelsInstances.put(item, new ModelInstance(model));
        }
    }

    public void registerModels(Json5ModelLoader loader) {
        Registries.ITEM.values().forEach((e) -> {
            try {
                if (e instanceof BlockItem) {
                    BlockItem blockItem = (BlockItem) e;
                    this.registerBlockModel(blockItem, () -> this.client.getBlockModel(blockItem.createBlockMeta()));
                    return;
                }

                ItemModel load = loader.load(e);
                if (load == null) {
                    load = new FlatItemModel(e);
                }

                this.registerModel(e, Objects.requireNonNullElseGet(load, () -> new FlatItemModel(e)));
            } catch (IOException ex) {
                QuantumClient.LOGGER.error("Failed to load item model for %s", e, ex);
                fallbackModel(e);
            }
        });
    }

    private void fallbackModel(Item e) {
        if (e instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) e;
//            this.registerBlockModel(blockItem, () -> this.client.getBakedBlockModel(blockItem.createBlockMeta()));
        }
    }

    public void reload() {
        this.modelsInstances.clear();
    }

    @Override
    public void dispose() {
        this.modelsInstances.clear();
        this.models.clear();
        this.blockModelCache.invalidateAll();
        this.blockModelCache.cleanUp();

        this.batch.dispose();

        this.itemCam = null;
        this.environment = null;
        this.material = null;
    }
}
