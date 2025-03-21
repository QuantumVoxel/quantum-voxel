package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;

/**
 * The SourceBlending enum represents various blending options for source factors in OpenGL rendering.
 * <p>
 * This is a part of the render pipeline. It is used to manage the source blending.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public enum SourceBlending {
    /**
     * The one blending option.
     */
    ONE(GL20.GL_ONE),
    /**
     * The zero blending option.
     */
    ZERO(GL20.GL_ZERO),
    /**
     * The source color blending option.
     */
    SRC_COLOR(GL30.GL_SRC_COLOR),
    /**
     * The one minus source color blending option.
     */
    ONE_MINUS_SRC_COLOR(GL20.GL_ONE_MINUS_SRC_COLOR),
    /**
     * The destination color blending option.
     */
    DST_COLOR(GL20.GL_DST_COLOR),
    /**
     * The one minus destination color blending option.
     */
    ONE_MINUS_DST_COLOR(GL20.GL_ONE_MINUS_DST_COLOR),
    /**
     * The source alpha blending option.
     */
    SRC_ALPHA(GL30.GL_SRC_ALPHA),
    /**
     * The one minus source alpha blending option.
     */
    ONE_MINUS_SRC_ALPHA(GL20.GL_ONE_MINUS_SRC_ALPHA),
    /**
     * The destination alpha blending option.
     */
    DST_ALPHA(GL20.GL_DST_ALPHA),
    /**
     * The one minus constant color blending option.
     */
    ONE_MINUS_CONSTANT_COLOR(GL20.GL_ONE_MINUS_CONSTANT_COLOR),
    /**
     * The constant alpha blending option.
     */
    CONSTANT_ALPHA(GL30.GL_CONSTANT_ALPHA),
    /**
     * The one minus constant alpha blending option.
     */
    ONE_MINUS_CONSTANT_ALPHA(GL20.GL_ONE_MINUS_CONSTANT_ALPHA);
    /**
     * The identifier for the corresponding OpenGL constant in the SourceBlending enum.
     */
    public final int id;

    SourceBlending(int id) {
        this.id = id;
    }
}
