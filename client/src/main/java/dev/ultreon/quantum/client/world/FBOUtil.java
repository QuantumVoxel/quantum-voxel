package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.IntBuffer;

public class FBOUtil {
    private static final IntBuffer fbo = BufferUtils.newIntBuffer(1);

    public int getCurrentFBO() {
        Gdx.gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, fbo);
        return fbo.get(0);
    }

    public boolean hasFBO() {
        return getCurrentFBO() != 0;
    }

    public boolean isMainFBO() {
        return getCurrentFBO() == 0;
    }
}
