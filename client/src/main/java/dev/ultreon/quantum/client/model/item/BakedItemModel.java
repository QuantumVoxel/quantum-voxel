package dev.ultreon.quantum.client.model.item;

import com.badlogic.gdx.graphics.g3d.Model;
import dev.ultreon.quantum.client.model.BakedModel;
import dev.ultreon.quantum.util.Identifier;

public class BakedItemModel extends BakedModel {
    private final ItemModel source;

    public BakedItemModel(Model model, ItemModel source) {
        super(source.resourceId().mapPath(path -> "item/" + path), model);
        this.source = source;
    }

    public ItemModel getSource() {
        return source;
    }
}
