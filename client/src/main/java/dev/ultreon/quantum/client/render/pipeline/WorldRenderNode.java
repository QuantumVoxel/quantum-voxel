package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.client.render.ShaderContext;
import dev.ultreon.quantum.client.shaders.provider.GameShaders;

import java.io.PrintStream;

/**
 * The world render node.
 * <p>
 * This node is responsible for rendering the world in some way.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public abstract class WorldRenderNode extends RenderPipeline.RenderNode {
    private Shader shader;

    /**
     * Dumps the info.
     *
     * @param stream The stream.
     */
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

    /**
     * Requires the model.
     *
     * @return True if the model is required, false otherwise. Generally true for world nodes.
     */
    @Override
    public boolean requiresModel() {
        return true;
    }

    /**
     * Gets the shader.
     *
     * @return The shader.
     */
    public Shader getShader() {
        return this.shader;
    }

    /**
     * Sets the shader.
     *
     * @param shader The shader.
     */
    public void setShader(Shader shader) {
        this.shader = shader;
    }
}
