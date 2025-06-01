package dev.ultreon.quantum.client.model.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.libs.collections.v0.tables.HashTable;
import dev.ultreon.libs.collections.v0.tables.Table;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.block.property.BlockDataEntry;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.block.CubeModel;
import dev.ultreon.quantum.client.model.item.ItemModel;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JsonModel implements BlockModel, ItemModel {
    public final Map<String, NamespaceID> textureElements;
    public final List<ModelElement> modelElements;
    public final boolean ambientOcclusion;
    public final Display display;
    private final NamespaceID id;
    private Model model;
    private final Table<String, BlockDataEntry<?>, JsonModel> overrides;
    private static final Vector3 SCALE = new Vector3(0.0625f, 0.0625f, 0.0625f);
    private ModelInstance modelInstance;
    private BlockState block;
    private final Vector3 rotation = new Vector3(15, 0, 0);
    private final Vector3 position = new Vector3(0, 4, 0);
    private final Vector3 scale = new Vector3(1, 1.155f, 1);
    private ModelInstance instance;

    public JsonModel(NamespaceID id, Map<String, NamespaceID> textureElements, List<ModelElement> modelElements, boolean ambientOcclusion, Display display, Table<String, BlockDataEntry<?>, JsonModel> overrides) {
        this.textureElements = textureElements;
        this.modelElements = modelElements;
        this.ambientOcclusion = ambientOcclusion;
        this.display = display;
        this.overrides = overrides;
        this.id = id;
    }

    public static JsonModel cubeOf(CubeModel model) {
        return new JsonModel(
                model.resourceId(),
                Map.of(
                        "top", model.top(),
                        "bottom", model.bottom(),
                        "left", model.left(),
                        "right", model.right(),
                        "front", model.front(),
                        "back", model.back()
                ),
                List.of(
                        new ModelElement(
                                Map.of(
                                        Direction.UP, new FaceElement("#top", new UVs(0, 0, 16, 16), 0, 0, "up"),
                                        Direction.DOWN, new FaceElement("#bottom", new UVs(0, 0, 16, 16), 0, 0, "down"),
                                        Direction.NORTH, new FaceElement("#front", new UVs(0, 0, 16, 16), 0, 0, "north"),
                                        Direction.SOUTH, new FaceElement("#back", new UVs(0, 0, 16, 16), 0, 0, "south"),
                                        Direction.EAST, new FaceElement("#left", new UVs(0, 0, 16, 16), 0, 0, "east"),
                                        Direction.WEST, new FaceElement("#right", new UVs(0, 0, 16, 16), 0, 0, "west")
                                ),
                                true,
                                ElementRotation.ZERO,
                                BakedCubeModel.w_from,
                                BakedCubeModel.w_to
                        )
                ),
                true,
                new Display(model.pass() == null ? "opaque" : model.pass()),
                new HashTable<>()
        );
    }

    @Override
    public void bakeInto(MeshPartBuilder meshPartBuilder, int x, int y, int z, int cull, int[] ao, long light) {
        for (int i = 0, modelElementsSize = modelElements.size(); i < modelElementsSize; i++) {
            ModelElement modelElement = modelElements.get(i);
            modelElement.bakeInto(i, meshPartBuilder, textureElements, x, y, z, cull, ao, light);
        }
    }

    @Override
    public void load(QuantumClient client) {
        if (model != null) return;
        this.model = ModelManager.INSTANCE.generateModel(id, modelBuilder -> {
            for (int i = 0, modelElementsSize = modelElements.size(); i < modelElementsSize; i++) {
                ModelElement modelElement = modelElements.get(i);
                modelElement.bake(i,
                        modelBuilder,
                        textureElements);
            }
        });
    }

    @Override
    public NamespaceID resourceId() {
        return id;
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    @Override
    public @Nullable BlockState getBlock() {
        return block;
    }

    @Override
    public Model getModel() {
        if (model == null) throw new IllegalStateException("Model not loaded: " + resourceId());
        return model;
    }

    @Override
    public void dispose() {
        ModelManager.INSTANCE.unloadModel(id);
    }

    @Override
    public Vector3 getItemScale() {
        return SCALE;
    }

    @Override
    public boolean hasAO() {
        return ambientOcclusion;
    }

    @Override
    public RenderPass getRenderPass() {
        return RenderPass.byName(display.renderPass);
    }

    @Override
    public Collection<NamespaceID> getAllTextures() {
        return textureElements.values();
    }

    @Override
    public void renderItem(Renderer renderer, ModelBatch batch, OrthographicCamera itemCam, Environment environment, int x, int y) {
        if (model == null) return;
        y = renderer.getHeight() - y;
        x += 8;
        y -= 16;
        int finalX = x;
        int finalY = y;
        renderer.external(() -> {
            if (instance == null)
                instance = new ModelInstance(model);

            float guiScale = QuantumClient.get().getGuiScale();
            itemCam.zoom = guiScale;
            itemCam.far = 100000;
            itemCam.update();
            scale.set(16, 16, 16).scl(guiScale);
            Vector3 scl = scale.cpy().scl(getScale());
            instance.transform.idt()
                    .translate(this.position.cpy()
                            .sub(0, 0, 1000)
                            .add(
                                    ((finalX - 8) - (int) (QuantumClient.get().getScaledWidth() / 2.0F)) * guiScale,
                                    ((finalY + 6) - (int) (QuantumClient.get().getScaledHeight() / 2.0F)) * guiScale,
                                    100
                            ))
                    .translate(getOffset()
                            .scl(-1 / scl.x, 1 / scl.y, 1 / scl.z))
                    .scale(-scl.x, -scl.y, scl.z)
                    .rotate(Vector3.X, -this.rotation.x);
            instance.transform.rotate(Vector3.Y, this.rotation.y);
            instance.transform.rotate(Vector3.Y, 0);
            instance.transform.rotate(Vector3.Z, 180);
            instance.transform.scale(1 / 16f, 1 / 16f, 1 / 16f);

            batch.begin(itemCam);
            Gdx.gl.glDepthMask(false);
            batch.render(instance, environment);
            batch.end();
            Gdx.gl.glDepthMask(true);
        });
    }

    public Table<String, BlockDataEntry<?>, JsonModel> getOverrides() {
        return this.overrides;
    }

    public void setBlock(BlockState block) {
        this.block = block;
    }
}
