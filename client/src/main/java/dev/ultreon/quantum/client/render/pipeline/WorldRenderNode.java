package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.client.render.ShaderContext;
import dev.ultreon.quantum.client.shaders.provider.GameShaders;

import java.io.PrintStream;

public abstract class WorldRenderNode extends RenderPipeline.RenderNode {
    private Shader shader;

    @Override
    public void dumpInfo(PrintStream stream) {
        super.dumpInfo(stream);
        Shader shader = this.shader;
        if (shader != null) {
            stream.println("Shader Hash Code: " + shader.hashCode());
            stream.println("Shader Classname: " + shader.getClass().getName());
            stream.println("Shader Superclass Classname: " + shader.getClass().getSuperclass().getName());
            stream.println("Shader String: " + shader);
        }
    }

    @Override
    public boolean requiresModel() {
        return true;
    }

}
