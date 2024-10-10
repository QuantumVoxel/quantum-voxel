package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import dev.ultreon.quantum.client.debug.Gizmo;
import dev.ultreon.quantum.client.model.QVModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.item.ItemModel;
import dev.ultreon.quantum.client.render.ModelObject;
import dev.ultreon.quantum.client.shaders.GeomShaderConfig;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.client.shaders.WorldShader;
import dev.ultreon.quantum.client.world.ClientChunk;

public class SceneShaders extends DefaultShaderProvider implements GameShaders {
    private final GeomShaderConfig config;

    public SceneShaders(final GeomShaderConfig config) {
        super(config);
        this.config = config;
    }

    public SceneShaders(final String vertexShader, final String fragmentShader, String geometryShader) {
        this(new GeomShaderConfig(vertexShader, fragmentShader, geometryShader));
    }

    public SceneShaders(final FileHandle vertexShader, final FileHandle fragmentShader, FileHandle geometryShader) {
        this(vertexShader.readString(), fragmentShader.readString(), geometryShader.readString());
    }

    public SceneShaders() {
        this(new GeomShaderConfig());
    }

    @Override
    public Shader createShader(Renderable renderable) {
        if (renderable != null && renderable.userData instanceof ClientChunk chunk) {
            WorldShader worldShader = new WorldShader(renderable, this.config, chunk.lod);
            Shaders.checkShaderCompilation(worldShader.program, "WorldShader");
            return worldShader;
        }

        if (renderable != null) {
            return getShaderFromUserData(renderable, renderable.userData);
        }

        throw new NullPointerException("Renderable cannot be null");
    }

    @Override
    public Shader getShader(Renderable renderable) {
        try {
            return super.getShader(renderable);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get shader from scene shader provider", e);
        }
    }

    private static Shader getShaderFromUserData(Renderable renderable, Object userData) {
        return switch (userData) {
            case Gizmo gizmo ->
                gizmo.outline ? Shaders.GIZMO_OUTLINE.get().createShader(renderable) : Shaders.GIZMO.get().createShader(renderable);
            case QVModel qvModel -> qvModel.getShaderProvider().createShader(renderable);
            case SkyboxShaders provider -> provider.createShader(renderable);
            case GameShaders provider -> provider.createShader(renderable);
            case ItemModel ignored -> Shaders.MODEL_VIEW.get().createShader(renderable);
            case BlockModel ignored -> Shaders.MODEL_VIEW.get().createShader(renderable);
            case Shader shader -> shader;
            case ModelObject modelObject -> modelObject.shaderProvider().createShader(renderable);
            case null, default -> new DefaultShader(renderable, new DefaultShader.Config());
        };
    }
}
