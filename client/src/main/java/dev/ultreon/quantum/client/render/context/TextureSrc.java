package dev.ultreon.quantum.client.render.context;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.render.core.GameUniform;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TextureSrc implements ColorSource {
    private final Supplier<Texture> texture;
    private final String name;
    private final boolean isGlobal;

    public TextureSrc(Supplier<Texture> texture, String name, boolean isGlobal) {
        this.texture = texture;
        this.name = name;
        this.isGlobal = isGlobal;
    }

    public static TextureSrc of(@NotNull NamespaceID of) {
        return new TextureSrc(() -> QuantumClient.get().getTextureManager().getTexture(of), of.toString(), false);
    }

    @Override
    public boolean isTexture() {
        return true;
    }

    @Override
    public boolean isColor() {
        return false;
    }

    @Override
    public void assign(ShaderContext context, Renderable renderable, GameUniform uniform) {
        if (uniform.getName().equals("u_" + name + "Texture")) {
            uniform.getShader().set(uniform.getLocation(), texture.get());
        }
        if (uniform.getName().equals("u_" + name + "TextureUV")) {
            uniform.getShader().set(uniform.getLocation(), 0f, 0f, 1f, 1f);
        }
    }

    @Override
    public boolean validate(ShaderContext context, Renderable renderable, GameUniform uniform) {
        return true;
    }

    @Override
    public GameUniform[] createUniforms(GameShader shader) {
        return new GameUniform[]{shader.registerUniform("u_" + name + "Texture", isGlobal), shader.registerUniform("u_" + name + "TextureUV", isGlobal)};
    }
}
