package com.ultreon.quantum.client.cs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.ultreon.quantum.cs.Component;

public abstract class RenderComp implements Component {

    @Override
    public void onTick() {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    public abstract void onRender(SpriteBatch batch, ModelBatch modelBatch);
}
