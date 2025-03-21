package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;

/**
 * Enumeration representing different destination blending modes for OpenGL rendering.
 * Each enum constant maps to a specific OpenGL blending mode value.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public enum DestinationBlending {
    ONE(GL30.GL_ONE),
    ZERO(GL30.GL_ZERO),
    SRC_COLOR(GL30.GL_SRC_COLOR),
    ONE_MINUS_SRC_COLOR(GL30.GL_ONE_MINUS_SRC_COLOR),
    DST_COLOR(GL30.GL_DST_COLOR),
    ONE_MINUS_DST_COLOR(GL30.GL_ONE_MINUS_DST_COLOR),
    SRC_ALPHA(GL30.GL_SRC_ALPHA),
    ONE_MINUS_SRC_ALPHA(GL30.GL_ONE_MINUS_SRC_ALPHA),
    DST_ALPHA(GL30.GL_DST_ALPHA),
    ONE_MINUS_DST_ALPHA(GL30.GL_ONE_MINUS_DST_ALPHA),
    CONSTANT_COLOR(GL30.GL_CONSTANT_COLOR),
    ONE_MINUS_CONSTANT_COLOR(GL30.GL_ONE_MINUS_CONSTANT_COLOR),
    CONSTANT_ALPHA(GL30.GL_CONSTANT_ALPHA),
    ONE_MINUS_CONSTANT_ALPHA(GL30.GL_ONE_MINUS_CONSTANT_ALPHA),
    SRC_ALPHA_SATURATE(GL30.GL_SRC_ALPHA_SATURATE);

    /**
     * The OpenGL blending mode value associated with the specific destination blending mode.
     * <p>
     * This is used to set the blending mode for the destination.
     * </p>
     * 
     * @see GL20
     * @see GL30
     */
    public final int id;

    /**
     * Constructs a new destination blending mode with the specified OpenGL blending mode value.
     *
     * @param id The OpenGL blending mode value.
     */
    DestinationBlending(int id) {
        this.id = id;
    }
}
