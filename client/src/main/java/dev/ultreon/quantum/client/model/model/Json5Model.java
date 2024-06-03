package dev.ultreon.quantum.client.model.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.Table;
import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.item.ItemModel;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.Identifier;

import java.util.List;
import java.util.Map;

public class Json5Model implements BlockModel, ItemModel {
    public final Map<String, Identifier> textureElements;
    public final List<Json5ModelLoader.ModelElement> modelElements;
    public final boolean ambientOcclusion;
    public final Json5ModelLoader.Display display;
    private final RegistryKey<?> key;
    private final Identifier id;
    private Model model;
    private final Table<String, BlockDataEntry<?>, Json5Model> overrides;
    private static final Vector3 SCALE = new Vector3(-0.0625f, -0.0625f, 0.0625f);

    public Json5Model(RegistryKey<?> key, Map<String, Identifier> textureElements, List<Json5ModelLoader.ModelElement> modelElements, boolean ambientOcclusion, Json5ModelLoader.Display display, Table<String, BlockDataEntry<?>, Json5Model> overrides) {
        this.key = key;
        this.textureElements = textureElements;
        this.modelElements = modelElements;
        this.ambientOcclusion = ambientOcclusion;
        this.display = display;
        this.overrides = overrides;
        id = key.parent().element().mapPath(s -> s + "/" + key.element().namespace() + "." + key.element().path());
    }

    public Model bake() {
        return ModelManager.INSTANCE.generateModel(id, modelBuilder -> {
            for (int i = 0, modelElementsSize = modelElements.size(); i < modelElementsSize; i++) {
                Json5ModelLoader.ModelElement modelElement = modelElements.get(i);
                modelElement.bake(i, modelBuilder, textureElements);
            }
        });
    }

    @Override
    public void load(QuantumClient client) {
        this.model = bake();
    }

    @Override
    public Identifier resourceId() {
        return key.element();
    }

    public RegistryKey<?> getKey() {
        return key;
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
    public Vector3 getItemOffset() {
        return new Vector3(-18, -30,0);
    }

    public Table<String, BlockDataEntry<?>, Json5Model> getOverrides() {
        return this.overrides;
    }
}
