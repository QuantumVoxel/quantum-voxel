package com.ultreon.quantum.client.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Disposable;

public class BakedModel implements Disposable {
    private final Model model;

    public BakedModel(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    @Override
    public void dispose() {
        this.model.dispose();
    }
}
