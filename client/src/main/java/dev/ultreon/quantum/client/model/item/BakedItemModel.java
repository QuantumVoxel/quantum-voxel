package dev.ultreon.quantum.client.model.item;

import com.badlogic.gdx.graphics.g3d.Model;
import dev.ultreon.quantum.client.model.BakedModel;

public class BakedItemModel extends BakedModel {
    private final ItemModel source;

    public BakedItemModel(Model model, ItemModel source) {
        super(model);
        this.source = source;
    }

    public ItemModel getSource() {
        return source;
    }
}
