package dev.ultreon.quantum.client.render.context;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.render.core.GameUniform;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GameShader extends Disposable {
    void begin(Camera camera, RenderContext context);

    void end();

    @Nullable GameUniform getUniform(String name);

    @NotNull GameUniform registerUniform(String name, boolean isGlobal);

    @Contract("_, _ -> param1")
    @NotNull GameUniform registerSetter(GameUniform uniform, UniformSetter setter);

    boolean set(int location, @NotNull Texture texture);

    boolean set(int location, float value);

    boolean set(int location, float x, float y);

    boolean set(int location, float x, float y, float z);

    boolean set(int location, float x, float y, float z, float w);

    boolean set(int location, int value);

    boolean set(int location, int x, int y);

    boolean set(int location, int x, int y, int z);

    boolean set(int location, int x, int y, int z, int w);

    boolean set(int location, boolean value);

    boolean set(int location, @NotNull Quaternion value);

    boolean set(int location, @NotNull Vector2 value);

    boolean set(int location, @NotNull Vector3 value);

    boolean set(int location, @NotNull Vector4 value);

    boolean set(int location, @NotNull GridPoint2 value);

    boolean set(int location, @NotNull GridPoint3 value);

    boolean set(int location, @NotNull Matrix4 value);

    boolean set(int location, @NotNull Matrix3 value);

    boolean set(int location, @NotNull Color value);

    @NotNull ShaderContext getContext();
}
