package dev.ultreon.mixinprovider;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GdxRegistries {
    public static final GdxRegistry<Model> MODELS = new GdxRegistry<>();
    public static final GdxRegistry<ShaderProgram> SHADER_PROGRAMS = new GdxRegistry<>();
    public static final GdxRegistry<Shader> SHADERS = new GdxRegistry<>();
    public static final GdxRegistry<Texture> TEXTURES = new GdxRegistry<>();
    public static final GdxRegistry<SpriteBatch> SPRITE_BATCHES = new GdxRegistry<>();
    public static final GdxRegistry<TextureAtlas> TEXTURE_ATLASES = new GdxRegistry<>();

    private GdxRegistries() {
    }
}
