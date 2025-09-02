package dev.ultreon.quantum.client.model.item;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;

import java.util.Collection;
import java.util.List;

public class FlatItemModel implements ItemModel {
    private final Item item;
    private Model model;

    public FlatItemModel(Item item) {
        this.item = item;
    }

    @Override
    public void load(QuantumClient client) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material(item.getId().toString());
        ResourceFileHandle file = new ResourceFileHandle(item.getId().mapPath(path -> "textures/items/" + path + ".png"));
        Texture texture = file.exists() ? new Texture(file) : TextureManager.DEFAULT_TEX_REG.getTexture();
        ResourceFileHandle emissiveFile = new ResourceFileHandle(item.getId().mapPath(path -> "textures/items/" + path + ".emissive.png"));
        Texture emissiveTexture = emissiveFile.exists() ? new Texture(emissiveFile) : TextureManager.DEFAULT_TEX_REG.getTexture();
        material.set(TextureAttribute.createDiffuse(texture));
        material.set(TextureAttribute.createEmissive(emissiveTexture));

        material.set(IntAttribute.createCullFace(0));

        modelBuilder.begin();
        MeshPartBuilder item1 = modelBuilder.part("item", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material);
        TextureRegion r = TextureManager.DEFAULT_TEX_REG.getTexture() == texture ? TextureManager.DEFAULT_TEX_REG : new TextureRegion(texture) ;
        item1.setUVRange(r);

        var v1 = new VertexInfo();
        item1.rect(
                v1.setPos(0, 0, 0).setNor(1, 0, 0).setUV(0, 0),
                v1.setPos(1, 0, 0).setNor(1, 0, 0).setUV(1, 0),
                v1.setPos(1, 1, 0).setNor(1, 0, 0).setUV(1, 1),
                v1.setPos(0, 1, 0).setNor(1, 0, 0).setUV(0, 1)
        );

        item1.rect(
                v1.setPos(0, 0, 0).setNor(-1, 0, 0).setUV(0, 0),
                v1.setPos(1, 0, 0).setNor(-1, 0, 0).setUV(1, 0),
                v1.setPos(1, 1, 0).setNor(-1, 0, 0).setUV(1, 1),
                v1.setPos(0, 1, 0).setNor(-1, 0, 0).setUV(0, 1)
        );

        this.model = modelBuilder.end();
    }

    @Override
    public NamespaceID resourceId() {
        return item.getId();
    }

    @Override
    public Model getModel() {
        if (model == null) throw new IllegalStateException("Model not loaded: " + resourceId());
        return model;
    }

    @Override
    public Collection<NamespaceID> getAllTextures() {
        return List.of(item.getId().mapPath(path -> "items/" + path));
    }

    @Override
    public void renderItem(Renderer renderer, ModelBatch batch, OrthographicCamera itemCam, Environment environment, int x, int y) {
        NamespaceID curKey = Registries.ITEM.getId(item);
        if (curKey == null) {
            renderer.blitColor(RgbColor.WHITE);
            renderer.blit((TextureRegion) null, x, y, 16, 16);
        } else {
            TextureRegion texture = renderer.client.itemTextureAtlas.getDiffuse(curKey.mapPath(path -> "textures/items/" + path + ".png"));
            renderer.blitColor(RgbColor.WHITE);
            renderer.blit(texture, x, y, 16, 16);
        }
    }

    public Item getItem() {
        return item;
    }
}
