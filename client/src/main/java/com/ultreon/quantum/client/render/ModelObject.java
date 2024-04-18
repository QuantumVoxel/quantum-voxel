package com.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.quantum.client.render.shader.OpenShaderProvider;
import com.ultreon.quantum.client.util.RenderableArray;

public record ModelObject(OpenShaderProvider shaderProvider, ModelInstance model, RenderableArray renderables) implements Disposable {
    public void dispose() {
        for (Renderable renderable : renderables) {
            renderable.meshPart.mesh = null;
            renderable.userData = null;
        }
        renderables.clear();
    }

    @Override
    public String toString() {
        return "ModelObject[" +
                "shaderProvider=" + shaderProvider + ", " +
                "renderables=" + renderables + ']';
    }

}
