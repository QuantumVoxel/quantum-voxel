package dev.ultreon.quantum.client.model.item;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.util.NamespaceID;

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
        Texture texture = client.itemTextureAtlas.getTexture();
        Texture emissiveTexture = client.itemTextureAtlas.getEmissiveTexture();
        if (texture != null) material.set(TextureAttribute.createDiffuse(texture));
        if (emissiveTexture != null) material.set(TextureAttribute.createEmissive(emissiveTexture));

        material.set(IntAttribute.createCullFace(0));

        modelBuilder.begin();
        MeshPartBuilder item1 = modelBuilder.part("item", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, material);
        TextureRegion r = client.itemTextureAtlas.getDiffuse(item.getId().mapPath(path -> "textures/items/" + path + ".png"));
        if (r == null) {
            QuantumClient.LOGGER.warn("Missing item texture {}", item.getId().mapPath(path -> "textures/items/" + path + ".png"));
        }
        item1.setUVRange(r);

        var v1 = new VertexInfo();
        item1.rect(
                v1.setPos(0, 0, 0).setNor(1, 0, 0).setUV(0, 0),
                v1.setPos(1, 0, 0).setNor(1, 0, 0).setUV(1, 0),
                v1.setPos(1, 1, 0).setNor(1, 0, 0).setUV(1, 1),
                v1.setPos(0, 1, 0).setNor(1, 0, 0).setUV(0, 1)
        );

        item1.rect(
                v1.setPos(1, 1, 0).setNor(1, 0, 0).setUV(1, 1),
                v1.setPos(0, 1, 0).setNor(1, 0, 0).setUV(0, 1),
                v1.setPos(0, 0, 0).setNor(1, 0, 0).setUV(0, 0),
                v1.setPos(1, 0, 0).setNor(1, 0, 0).setUV(1, 0)
        );

        QuantumClient.invokeAndWait(() -> this.model = modelBuilder.end());
    }

    @Override
    public NamespaceID resourceId() {
        return item.getId();
    }

    @Override
    public Model getModel() {
        return model;
    }

    public Item getItem() {
        return item;
    }
}
