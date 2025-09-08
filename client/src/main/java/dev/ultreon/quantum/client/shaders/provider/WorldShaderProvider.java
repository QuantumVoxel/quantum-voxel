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
import dev.ultreon.quantum.client.render.VisualGameObject;
import dev.ultreon.quantum.client.shaders.GeomShaderConfig;
import dev.ultreon.quantum.client.shaders.ModelViewShader;
import dev.ultreon.quantum.client.shaders.ShaderProviders;
import dev.ultreon.quantum.client.shaders.WorldShader;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.client.world.Skybox;
import org.jetbrains.annotations.Nullable;

public class WorldShaderProvider extends DefaultShaderProvider implements GameShaders {
    private final DefaultShader.Config config;
    @Nullable
    private final String version;
    private final String name;

    public WorldShaderProvider(final String vertexShader, final String fragmentShader, @Nullable String version, String name) {
        this(new DefaultShader.Config(vertexShader, fragmentShader), version, name);
    }

    public WorldShaderProvider(final FileHandle vertexShader, final FileHandle fragmentShader, @Nullable String version, String name) {
        this(vertexShader.readString(), fragmentShader.readString(), version, name);
    }

    public WorldShaderProvider(final DefaultShader.Config config, @Nullable String version, String name) {
        super(config);
        this.config = config;
        this.version = version;
        this.name = name;
    }

    public WorldShaderProvider(@Nullable String version, String name) {
        this(new GeomShaderConfig(), version, name);
    }

    @Override
    public Shader createShader(Renderable renderable) {
        if (renderable != null) {
            if (renderable.userData instanceof ClientChunk) {
                ClientChunk chunk = (ClientChunk) renderable.userData;
                WorldShader worldShader = new WorldShader(renderable, this.config, chunk.lod, version);
                ShaderProviders.checkShaderCompilation(worldShader.program, name);
                return worldShader;
            }

            WorldShader worldShader = new WorldShader(renderable, this.config, version != null ? "#version " + version + "\n" : "");
            ShaderProviders.checkShaderCompilation(worldShader.program, name);
            return worldShader;
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
        return new DefaultShader(renderable, new DefaultShader.Config());
    }

    public @Nullable String getVersion() {
        return version;
    }
}
