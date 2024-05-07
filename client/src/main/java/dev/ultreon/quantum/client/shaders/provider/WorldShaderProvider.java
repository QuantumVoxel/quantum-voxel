package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import dev.ultreon.quantum.client.render.shader.Shaders;
import dev.ultreon.quantum.client.model.QVModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.item.ItemModel;
import dev.ultreon.quantum.client.render.ModelObject;
import dev.ultreon.quantum.client.render.shader.OpenShaderProvider;
import dev.ultreon.quantum.client.shaders.WorldShader;
import dev.ultreon.quantum.client.world.ClientChunk;

public class WorldShaderProvider extends DefaultShaderProvider implements OpenShaderProvider {
    public WorldShaderProvider(final DefaultShader.Config config) {
        super(config);
    }

    public WorldShaderProvider(final String vertexShader, final String fragmentShader) {
        this(new DefaultShader.Config(vertexShader, fragmentShader));
    }

    public WorldShaderProvider(final FileHandle vertexShader, final FileHandle fragmentShader) {
        this(vertexShader.readString(), fragmentShader.readString());
    }

    public WorldShaderProvider() {
        this(null);
    }

    @Override
    public Shader createShader(Renderable renderable) {
        if (renderable != null && renderable.userData instanceof ClientChunk) {
            WorldShader worldShader = new WorldShader(renderable, this.config);
            Shaders.checkShaderCompilation(worldShader.program, "WorldShader");
            return worldShader;
        }

        if (renderable != null) {
            return getShaderFromUserData(renderable, renderable.userData);
        }

        throw new NullPointerException("Renderable cannot be null");
    }

    private static Shader getShaderFromUserData(Renderable renderable, Object userData) {
        return switch (userData) {
            case QVModel qvModel -> qvModel.getShaderProvider().createShader(renderable);
            case OpenShaderProvider provider -> provider.createShader(renderable);
            case ItemModel ignoredItemModel -> Shaders.MODEL_VIEW.get().createShader(renderable);
            case BlockModel ignoredBlockModel -> Shaders.MODEL_VIEW.get().createShader(renderable);
            case Shader shader -> shader;
            case ModelObject modelObject -> modelObject.shaderProvider().createShader(renderable);
            case null, default -> new DefaultShader(renderable, new DefaultShader.Config());
        };
    }
}
