package dev.ultreon.quantum.client.model.model;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.libs.collections.v0.tables.HashTable;
import dev.ultreon.libs.collections.v0.tables.Table;
import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.block.CubeModel;
import dev.ultreon.quantum.client.model.item.ItemModel;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Direction;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Json5Model implements BlockModel, ItemModel {
    public final Map<String, NamespaceID> textureElements;
    public final List<Json5ModelLoader.ModelElement> modelElements;
    public final boolean ambientOcclusion;
    public final Json5ModelLoader.Display display;
    private final NamespaceID id;
    private Model model;
    private final Table<String, BlockDataEntry<?>, Json5Model> overrides;
    private static final Vector3 SCALE = new Vector3(0.0625f, 0.0625f, 0.0625f);

    public Json5Model(NamespaceID id, Map<String, NamespaceID> textureElements, List<Json5ModelLoader.ModelElement> modelElements, boolean ambientOcclusion, Json5ModelLoader.Display display, Table<String, BlockDataEntry<?>, Json5Model> overrides) {
        this.textureElements = textureElements;
        this.modelElements = modelElements;
        this.ambientOcclusion = ambientOcclusion;
        this.display = display;
        this.overrides = overrides;
        this.id = id;
    }

    public static BlockModel cubeOf(CubeModel model) {
        return new Json5Model(
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
                        new Json5ModelLoader.ModelElement(
                                Map.of(
                                        Direction.UP, new Json5ModelLoader.FaceElement("#top", new Json5ModelLoader.UVs(0, 0, 16, 16), 0, 0, "up"),
                                        Direction.DOWN, new Json5ModelLoader.FaceElement("#bottom", new Json5ModelLoader.UVs(0, 0, 16, 16), 0, 0, "down"),
                                        Direction.NORTH, new Json5ModelLoader.FaceElement("#front", new Json5ModelLoader.UVs(0, 0, 16, 16), 0, 0, "north"),
                                        Direction.SOUTH, new Json5ModelLoader.FaceElement("#back", new Json5ModelLoader.UVs(0, 0, 16, 16), 0, 0, "south"),
                                        Direction.EAST, new Json5ModelLoader.FaceElement("#left", new Json5ModelLoader.UVs(0, 0, 16, 16), 0, 0, "east"),
                                        Direction.WEST, new Json5ModelLoader.FaceElement("#right", new Json5ModelLoader.UVs(0, 0, 16, 16), 0, 0, "west")
                                ),
                                true,
                                Json5ModelLoader.ElementRotation.ZERO,
                                BakedCubeModel.w_from,
                                BakedCubeModel.w_to
                        )
                ),
                true,
                new Json5ModelLoader.Display(model.pass() == null ? "opaque" : model.pass()),
                new HashTable<>()
        );
    }

    @Override
    public void bakeInto(MeshPartBuilder meshPartBuilder, int x, int y, int z, int cull, int[] ao, long light) {
        for (int i = 0, modelElementsSize = modelElements.size(); i < modelElementsSize; i++) {
            Json5ModelLoader.ModelElement modelElement = modelElements.get(i);
            modelElement.bake(i, meshPartBuilder, textureElements, x, y, z, cull, ao, light);
        }
    }

    public Model bake() {
        return ModelManager.INSTANCE.generateModel(id, modelBuilder -> {
            for (int i = 0, modelElementsSize = modelElements.size(); i < modelElementsSize; i++) {
                Json5ModelLoader.ModelElement modelElement = modelElements.get(i);
                modelElement.bake(i,
                        modelBuilder.part(String.valueOf(i),
                                GL20.GL_TRIANGLES,
                                new VertexAttributes(VertexAttribute.Position(),
                                        VertexAttribute.ColorPacked(),
                                        VertexAttribute.Normal(),
                                        VertexAttribute.TexCoords(0)),
                                getRenderPass().material().get()),
                        textureElements, 0, 0, 0, 0, new int[]{0,0,0,0,0,0}, 0xff);
            }
        });
    }

    @Override
    public void load(QuantumClient client) {
        this.model = bake();
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
    public Model getModel() {
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

    public Table<String, BlockDataEntry<?>, Json5Model> getOverrides() {
        return this.overrides;
    }
}
