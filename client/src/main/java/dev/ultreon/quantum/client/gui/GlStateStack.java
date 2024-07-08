package dev.ultreon.quantum.client.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import dev.ultreon.quantum.util.RgbColor;

import java.util.ArrayDeque;
import java.util.Deque;

public class GlStateStack {
    private final Deque<GlState> states = new ArrayDeque<>();
    private GlState top = null;
    
    public void begin() {
        if (this.top != null)
            throw new IllegalStateException("Already in a state!");
        
        this.top = new GlState();
        this.states.push(this.top);
    }
    
    public void end() {
        if (this.top == null)
            throw new IllegalStateException("Not in a state!");

        this.top = null;
        this.states.pop();

        if (!this.states.isEmpty())
            throw new IllegalStateException("State stack isn't empty!");
    }
    
    public void push() {
        if (this.top == null)
            throw new IllegalStateException("Not in a state!");
        
        this.states.push(this.top);
        this.top = new GlState(this.top);
    }

    public void pop() {
        if (this.top == null)
            throw new IllegalStateException("Not in a state!");

        if (this.states.isEmpty())
            throw new IllegalStateException("State stack is empty!");

        this.top = this.states.pop();
    }

    public void enableBlending() {
        this.top.blend = true;
        RgbColor blendColor = this.top.blendColor;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(this.top.blendSrc, this.top.blendDst, this.top.blendSrcAlpha, this.top.blendDstAlpha);
        Gdx.gl.glBlendEquationSeparate(this.top.blendEquation, this.top.blendEquationAlpha);
        Gdx.gl.glBlendColor(blendColor.getRed(), blendColor.getGreen(), blendColor.getBlue(), blendColor.getAlpha());
    }

    public void disableBlending() {
        this.top.blend = false;
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void blendFunc(int srcFactor, int dstFactor) {
        this.top.blendSrc = srcFactor;
        this.top.blendDst = dstFactor;
        this.top.blendSrcAlpha = srcFactor;
        this.top.blendDstAlpha = dstFactor;

        Gdx.gl.glBlendFunc(this.top.blendSrc, this.top.blendDst);
    }

    public void blendFuncSeparate(int srcFactor, int dstFactor, int srcAlpha, int dstAlpha) {
        this.top.blendSrc = srcFactor;
        this.top.blendDst = dstFactor;
        this.top.blendSrcAlpha = srcAlpha;
        this.top.blendDstAlpha = dstAlpha;

        Gdx.gl.glBlendFuncSeparate(this.top.blendSrc, this.top.blendDst, this.top.blendSrcAlpha, this.top.blendDstAlpha);
    }

    public void blendEquationSeparate(int modeRGB, int modeAlpha) {
        this.top.blendEquation = modeRGB;
        this.top.blendEquationAlpha = modeAlpha;

        Gdx.gl.glBlendEquationSeparate(this.top.blendEquation, this.top.blendEquationAlpha);
    }

    public void enableDepthTest() {
        this.top.depthTest = true;
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    public void disableDepthTest() {
        this.top.depthTest = false;
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    public void depthMask(boolean mask) {
        this.top.depthMask = mask;
        Gdx.gl.glDepthMask(mask);
    }

    public void depthFunc(int func) {
        this.top.depthFunc = func;
        Gdx.gl.glDepthFunc(func);
    }

    public void depthRange(float near, float far) {
        this.top.depthNear = near;
        this.top.depthFar = far;
        Gdx.gl.glDepthRangef(near, far);
    }

    public void enableCulling() {
        this.top.cullFace = true;
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    }

    public void disableCulling() {
        this.top.cullFace = false;
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
    }

    public void cullFace(int mode) {
        this.top.cullFaceMode = mode;
        Gdx.gl.glCullFace(mode);
    }

    public void lineWidth(int width) {
        this.top.lineWidth = width;
        Gdx.gl.glLineWidth(width);
    }

    public void lineWidth(float width) {
        this.top.lineWidth = width;
        Gdx.gl.glLineWidth(width);
    }

    public void enableScissor() {
        this.top.scissor = true;
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
    }

    public void disableScissor() {
        this.top.scissor = false;
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    public void scissor(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (!this.top.scissor) return;

        this.top.scissorX = x;
        this.top.scissorY = y;
        this.top.scissorWidth = width;
        this.top.scissorHeight = height;

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(x, y, width, height);
    }

    public static class GlState {
        public float lineWidth;

        public boolean blend;
        public int blendSrc;
        public int blendDst;
        public int blendEquation;
        public RgbColor blendColor;
        public int blendSrcAlpha;
        public int blendDstAlpha;
        public int blendEquationAlpha;

        public boolean depthMask;
        public boolean depthTest;
        public int depthFunc;
        public float depthNear;
        public float depthFar;

        public boolean cullFace;
        public int cullFaceMode;

        public boolean scissor;
        public int scissorX;
        public int scissorY;
        public int scissorWidth;
        public int scissorHeight;
        
        public GlState(GlState original) {
            this.lineWidth = original.lineWidth;

            this.blend = original.blend;
            this.blendSrc = original.blendSrc;
            this.blendDst = original.blendDst;
            this.blendEquation = original.blendEquation;
            this.blendColor = original.blendColor;
            this.blendSrcAlpha = original.blendSrcAlpha;
            this.blendDstAlpha = original.blendDstAlpha;
            this.blendEquationAlpha = original.blendEquationAlpha;

            this.depthMask = original.depthMask;
            this.depthTest = original.depthTest;
            this.depthFunc = original.depthFunc;
            this.depthNear = original.depthNear;
            this.depthFar = original.depthFar;

            this.cullFace = original.cullFace;
            this.cullFaceMode = original.cullFaceMode;

            this.scissor = original.scissor;
            this.scissorX = original.scissorX;
            this.scissorY = original.scissorY;
            this.scissorWidth = original.scissorWidth;
            this.scissorHeight = original.scissorHeight;
        }
        
        public GlState() {
            this.lineWidth = 1.0F;

            this.blend = true;
            this.blendSrc = GL20.GL_ONE;
            this.blendDst = GL20.GL_ONE;
            this.blendEquation = GL20.GL_FUNC_ADD;
            this.blendColor = RgbColor.WHITE;
            this.blendSrcAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;
            this.blendDstAlpha = GL20.GL_SRC_ALPHA;
            this.blendEquationAlpha = GL20.GL_FUNC_ADD;

            this.depthMask = false;
            this.depthTest = true;
            this.depthFunc = GL20.GL_LESS;
            this.depthNear = 0.0F;
            this.depthFar = 10000.0F;

            this.cullFaceMode = GL20.GL_BACK;
            this.cullFace = false;

            this.scissor = false;
            this.scissorX = 0;
            this.scissorY = 0;
            this.scissorWidth = 0;
            this.scissorHeight = 0;

            this.apply();
        }
        
        public void apply() {
            Gdx.gl.glLineWidth(this.lineWidth);
            
            if (this.blend) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(this.blendSrc, this.blendDst);
                Gdx.gl.glBlendEquationSeparate(this.blendEquation, this.blendEquationAlpha);
                Gdx.gl.glBlendColor(this.blendColor.getRed(), this.blendColor.getGreen(), this.blendColor.getBlue(), this.blendColor.getAlpha());
            } else
                Gdx.gl.glDisable(GL20.GL_BLEND);
            
            if (this.depthTest) {
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthMask(this.depthMask);
                Gdx.gl.glDepthFunc(this.depthFunc);
                Gdx.gl.glDepthRangef(this.depthNear, this.depthFar);
            } else
                Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            
            if (this.cullFace) {
                Gdx.gl.glEnable(GL20.GL_CULL_FACE);
                Gdx.gl.glCullFace(this.cullFaceMode);
            } else
                Gdx.gl.glDisable(GL20.GL_CULL_FACE);

            if (this.scissor) {
                Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
                Gdx.gl.glScissor(this.scissorX, this.scissorY, this.scissorWidth, this.scissorHeight);
            } else
                Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        }
    }
}
