/////////////////////
//     Package     //
/////////////////////
package dev.ultreon.quantum.client.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Disposable;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.GaussianBlurEffect;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import dev.ultreon.libs.commons.v0.vector.Vec4i;
import dev.ultreon.quantum.util.Color;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.font.Font;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.client.util.InvalidValueException;
import dev.ultreon.quantum.client.world.RenderablePool;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.text.FormattedText;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.util.Identifier;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.ultreon.quantum.client.QuantumClient.id;

/**
 * Renderer class.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
@SuppressWarnings("unused")
public class Renderer implements Disposable {
    private static final int TAB_WIDTH = 32;
    ////////////////////
    //     Fields     //
    ////////////////////
    private final QuantumClient client = QuantumClient.get();
    private final Deque<Vector3> globalTranslation = new ArrayDeque<>();
    private final Batch batch;
    private final ShapeDrawer shapes;
    private final TextureManager textureManager;
    private final VfxManager vfxManager;
    private final ShaderProgram blurShader;
    private final ShaderProgram gridShader;
    private float strokeWidth = 1;
    private Font font;
    private final Matrices matrices;
    private Color blitColor = RgbColor.rgb(0xffffff);
    private final Vector2 tmp2A = new Vector2();
    private final Vector3 tmp3A = new Vector3();
    private final GlStateStack glState = new GlStateStack();
    private int width;
    private int height;
    private boolean blurred;

    public static final int FBO_SIZE = 1024;

    private FrameBuffer grid;
    private float iTime;
    private final RenderablePool renderablePool = new RenderablePool();

    /**
     * @param shapes shape drawer instance from {@link QuantumClient}
     */
    public Renderer(ShapeDrawer shapes) {
        this(shapes, new Matrices());
    }

    /**
     * @param shapes      shape drawer instance from {@link QuantumClient}
     * @param matrices current matrix stack.
     */
    public Renderer(ShapeDrawer shapes, Matrices matrices) {
        this.globalTranslation.push(new Vector3());
        this.font = this.client.font;
        GL30 gl30 = Gdx.gl30;
        this.batch = shapes.getBatch();
        this.shapes = shapes;
        this.matrices = matrices;
        this.textureManager = this.client.getTextureManager();
        if (this.textureManager == null) throw new IllegalArgumentException("Texture manager isn't initialized yet!");

        // Projection matrix.
        this.matrices.onEdit = matrix -> shapes.getBatch().setTransformMatrix(matrix);


        // VfxManager is a host for the effects.
        // It captures rendering into internal off-screen buffer and applies a chain of defined effects.
        // Off-screen buffers may have any pixel format; for this example, we will use RGBA8888.
        vfxManager = new VfxManager(Format.RGBA8888);

        // Create and add an effect.
        // VfxEffect derivative classes serve as controllers for the effects.
        // They provide public properties to configure and control them.
        GaussianBlurEffect vfxBlur = new GaussianBlurEffect(GaussianBlurEffect.BlurType.Gaussian5x5b);

        blurShader = new ShaderProgram(VERT, FRAG);
        if (!blurShader.isCompiled()) {
            System.err.println(blurShader.getLog());
            QuantumClient.crash(new IllegalStateException("Failed to compile blur shader!"));
        }
        if (!blurShader.getLog().isEmpty())
            System.out.println(blurShader.getLog());

        //setup uniforms for our shader
        blurShader.bind();
        blurShader.setUniformf("iBlurDirection", 0f, 0f);
        blurShader.setUniformf("radius", 1f);

        gridShader = new ShaderProgram(VERT, GRID_FRAG);
        if (!gridShader.isCompiled()) {
            System.err.println(gridShader.getLog());
            QuantumClient.crash(new IllegalStateException("Failed to compile grid shader!"));
        }
        if (!gridShader.getLog().isEmpty())
            System.out.println(gridShader.getLog());
    }

    public Matrices getMatrices() {
        return this.matrices;
    }

    @CanIgnoreReturnValue
    public Renderer setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer setColor(Color c) {
        if (c == null) return this;
        if (this.font != null)
            this.font.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
        this.shapes.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer setColor(int r, int g, int b) {
        this.setColor(RgbColor.rgb(r, g, b));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer setColor(float r, float g, float b) {
        this.setColor(RgbColor.rgb(r, g, b));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer setColor(int r, int g, int b, int a) {
        this.setColor(RgbColor.rgba(r, g, b, a));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer setColor(float r, float g, float b, float a) {
        this.setColor(RgbColor.rgba(r, g, b, a));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer setColor(int argb) {
        this.setColor(RgbColor.argb(argb));
        return this;
    }

    /**
     * Sets current color from a color hex.
     * Examples:
     * <code>
     * color("#f70")
     * color("#fff7")
     * color("#ffd500")
     * color("#aab70077")
     * </code>
     *
     * @param hex a color hex.
     */
    @CanIgnoreReturnValue
    public Renderer setColor(String hex) {
        this.setColor(RgbColor.hex(hex));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer clearColor(Color color) {
        Gdx.gl.glClearColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer clearColor(int red, int green, int blue) {
        this.clearColor(RgbColor.rgb(red, green, blue));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer clearColor(float red, float green, float blue) {
        this.clearColor(RgbColor.rgb(red, green, blue));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer clearColor(int red, int green, int blue, int alpha) {
        this.clearColor(RgbColor.rgba(red, green, blue, alpha));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer clearColor(float red, float green, float blue, float alpha) {
        this.clearColor(RgbColor.rgba(red, green, blue, alpha));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer clearColor(int argb) {
        this.clearColor(RgbColor.argb(argb));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer clearColor(String hex) {
        this.clearColor(RgbColor.hex(hex));
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer outline(Rectangle2D rect) {
        this.rectLine((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(), (float) rect.getHeight());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer outline(Ellipse2D ellipse) {
        this.ovalLine((float) ellipse.getX(), (float) ellipse.getY(), (float) ellipse.getWidth(), (float) ellipse.getHeight());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer outline(Line2D s) {
        this.line((float) s.getX1(), (float) s.getY1(), (float) s.getX2(), (float) s.getY2());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer circle(float x, float y, float radius) {
        this.shapes.filledCircle(x, y, radius);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer circleLine(float x, float y, float radius) {
        this.shapes.circle(x, y, radius);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer fill(Rectangle2D rect) {
        this.rect((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(), (float) rect.getHeight());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer fill(Ellipse2D ellipse) {
        this.oval((float) ellipse.getX(), (float) ellipse.getY(), (float) ellipse.getWidth(), (float) ellipse.getHeight());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer fill(Line2D line) {
        this.line((float) line.getX1(), (float) line.getY1(), (float) line.getX2(), (float) line.getY2());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer fill(Vec4i r) {
        this.rect(r.x, r.y, r.z, r.w);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer line(int x1, int y1, int x2, int y2) {
        this.shapes.line(x1, y1, x2, y2);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer line(float x1, float y1, float x2, float y2) {
        this.shapes.line(x1, y1, x2, y2);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer rectLine(int x, int y, int width, int height) {
        this.shapes.rectangle(x + this.strokeWidth / 2f, y + this.strokeWidth / 2f, width - this.strokeWidth, height - this.strokeWidth, this.strokeWidth);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer rectLine(float x, float y, float width, float height) {
        this.shapes.rectangle(x + this.strokeWidth / 2f, y + this.strokeWidth / 2f, width - this.strokeWidth, height - this.strokeWidth, this.strokeWidth);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer rect(int x, int y, int width, int height) {
        this.shapes.filledRectangle(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer rect(float x, float y, float width, float height) {
        this.shapes.filledRectangle(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer roundRectLine(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        this.shapes.rectangle(x, y, width, height, this.strokeWidth);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer roundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        this.shapes.rectangle(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer rect3DLine(int x, int y, int width, int height, boolean raised) {
        this.shapes.rectangle(x, y, width, height, this.strokeWidth);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer rect3D(int x, int y, int width, int height, boolean raised) {
        this.shapes.filledRectangle(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer ovalLine(int x, int y, int width, int height) {
        this.shapes.ellipse(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer oval(int x, int y, int width, int height) {
        this.shapes.filledEllipse(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer ovalLine(float x, float y, float width, float height) {
        this.shapes.ellipse(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer oval(float x, float y, float width, float height) {
        this.shapes.filledEllipse(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer arcLine(int x, int y, int width, int height, int startAngle, int arcAngle) {
        this.shapes.arc(x, y, width, startAngle, arcAngle);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer arc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        this.shapes.arc(x, y, width, startAngle, arcAngle);
        return this;
    }

    ///////////////////
    //     Image     //
    ///////////////////
    @CanIgnoreReturnValue
    public Renderer blit(TextureRegion tex, float x, float y) {
        if (tex == null) tex = TextureManager.DEFAULT_TEX_REG;
        this.batch.setColor(this.blitColor.toGdx());
        this.batch.draw(tex, x, y + tex.getRegionHeight(), tex.getRegionWidth(), -tex.getRegionHeight());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer blit(TextureRegion tex, float x, float y, float width, float height) {
        if (tex == null) tex = TextureManager.DEFAULT_TEX_REG;
        this.batch.setColor(this.blitColor.toGdx());
        this.batch.draw(tex, x, y + tex.getRegionHeight(), tex.getRegionWidth(), -tex.getRegionHeight());
        return this;
    }

    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y) {
        this.batch.setColor(this.blitColor.toGdx());
        this.batch.draw(tex, x, y + tex.getHeight(), tex.getWidth(), -tex.getHeight());
        return this;
    }


    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, Color backgroundColor) {
        this.setColor(backgroundColor);
        this.rect(x, y, tex.getWidth(), tex.getHeight());
        this.batch.setColor(this.blitColor.toGdx());
        this.batch.draw(tex, x, y + tex.getHeight(), tex.getWidth(), -tex.getHeight());
        return this;
    }

    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, Color backgroundColor) {
        this.blit(tex, x, y, width, height, 0.0F, 0.0F, backgroundColor);
        return this;
    }

    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v, Color backgroundColor) {
        this.blit(tex, x, y, width, height, u, v, width, height, backgroundColor);
        return this;
    }

    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, Color backgroundColor) {
        this.blit(tex, x, y, width, height, u, v, uWidth, vHeight, 256, 256, backgroundColor);
        return this;
    }

    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight, Color backgroundColor) {
        this.setColor(backgroundColor);
        this.rect(x, y, width, height);
        this.batch.setColor(this.blitColor.toGdx());
        TextureRegion textureRegion = new TextureRegion(tex, texWidth / u, texHeight / v, texWidth / (u + uWidth), texHeight / (v + vHeight));
        this.batch.draw(textureRegion, x, y + height, width, -height);
        return this;
    }

    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height) {
        this.blit(tex, x, y, width, height, 0.0F, 0.0F);
        return this;
    }

    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v) {
        this.blit(tex, x, y, width, height, u, v, width, height);
        return this;
    }

    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight) {
        this.blit(tex, x, y, width, height, u, v, uWidth, vHeight, 256, 256);
        return this;
    }

    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight) {
        this.batch.setColor(this.blitColor.toGdx());
        TextureRegion textureRegion = new TextureRegion(tex, 1 * u / texWidth, 1 * v / texHeight, 1 * (u + uWidth) / texWidth, 1 * (v + vHeight) / texHeight);
        this.batch.draw(textureRegion, x, y + height, width, -height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer blit(Identifier id, float x, float y, float width, float height, Color backgroundColor) {
        this.blit(id, x, y, width, height, 0.0F, 0.0F, backgroundColor);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer blit(Identifier id, float x, float y, float width, float height, float u, float v, Color backgroundColor) {
        Texture texture = this.textureManager.getTexture(id);
        this.blit(id, x, y, width, height, u, v, 256, 256, backgroundColor);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer blit(Identifier id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, Color backgroundColor) {
        Texture texture = this.textureManager.getTexture(id);
        this.blit(id, x, y, width, height, u, v, uWidth, vHeight, 256, 256, backgroundColor);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer blit(Identifier id, float x, float y, float width, float height) {
        this.blit(id, x, y, width, height, 0.0F, 0.0F);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer blit(Identifier id, float x, float y, float width, float height, float u, float v) {
        this.blit(id, x, y, width, height, u, v, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer blit(Identifier id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight) {
        this.blit(id, x, y, width, height, u, v, uWidth, vHeight, 256, 256);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer blit(Identifier id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight) {
        this.batch.setColor(this.blitColor.toGdx());
        Texture tex = this.textureManager.getTexture(id);
        TextureRegion textureRegion = new TextureRegion(tex, 1 * u / texWidth, 1 * v / texHeight, 1 * (u + uWidth) / texWidth, 1 * (v + vHeight) / texHeight);
        this.batch.draw(textureRegion, x, y + height, width, -height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer blit(Identifier id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight, Color backgroundColor) {
        this.setColor(backgroundColor);
        this.rect(x, y, width, height);
        Texture tex = this.textureManager.getTexture(id);
        this.batch.setColor(this.blitColor.toGdx());
        TextureRegion textureRegion = new TextureRegion(tex, texWidth / u, texHeight / v, texWidth / (u + uWidth), texHeight / (v + vHeight));
        this.batch.draw(textureRegion, x, y + height, width, -height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer drawSprite(Sprite sprite, int x, int y) {
        drawSprite(sprite, x, y, sprite.getWidth(), sprite.getHeight());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer drawSprite(Sprite sprite, int x, int y, int width, int height) {
        sprite.render(this, x, y, width, height);
        return this;
    }

    //////////////////
    //     Text     //

    //////////////////
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y, ColorCode color) {
        this.textLeft(text, x, y, RgbColor.of(color), true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y, Color color, boolean shadow) {
        this.font.drawText(this, text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y, ColorCode color, boolean shadow) {
        this.font.drawText(this, text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color) {
        this.textLeft(text, x, y, RgbColor.of(color), true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color, boolean shadow) {
        this.font.drawText(this, text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color, boolean shadow) {
        this.font.drawText(this, text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, float maxWidth, String truncate) {
        this.textLeft(text, x, y, RgbColor.WHITE, maxWidth, truncate);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color, float maxWidth, String truncate) {
        this.textLeft(text, x, y, color, true, maxWidth, truncate);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color, float maxWidth, String truncate) {
        this.textLeft(text, x, y, RgbColor.of(color), true, maxWidth, truncate);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, boolean shadow, float maxWidth, String truncate) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow, maxWidth, truncate);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color, boolean shadow, float maxWidth, String truncate) {
        this.font.drawText(this, text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color, boolean shadow, float maxWidth, String truncate) {
        this.font.drawText(this, text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, float maxWidth, boolean wrap, String truncate) {
        this.textLeft(text, x, y, RgbColor.WHITE, maxWidth, wrap, truncate);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color, float maxWidth, boolean wrap, String truncate) {
        this.textLeft(text, x, y, color, true, maxWidth, wrap, truncate);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color, float maxWidth, boolean wrap, String truncate) {
        this.textLeft(text, x, y, RgbColor.of(color), true, maxWidth, wrap, truncate);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, boolean shadow, float maxWidth, boolean wrap, String truncate) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow, maxWidth, wrap, truncate);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color, boolean shadow, float maxWidth, boolean wrap, String truncate) {
        this.font.drawText(this, text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color, boolean shadow, float maxWidth, boolean wrap, String truncate) {
        this.font.drawText(this, text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, int x, int y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, int x, int y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, int x, int y, ColorCode color) {
        this.textLeft(String.valueOf(text), x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, int x, int y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, int x, int y, Color color, boolean shadow) {
        this.font.drawText(this, text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, int x, int y, ColorCode color, boolean shadow) {
        this.font.drawText(this, text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float x, float y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float x, float y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float x, float y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float x, float y, Color color, boolean shadow) {
        this.font.drawText(this, text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y, ColorCode color) {
        this.textLeft(String.valueOf(text), x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y, Color color, boolean shadow) {
        this.font.drawText(this, text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y, ColorCode color, boolean shadow) {
        this.font.drawText(this, text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, float x, float y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, float x, float y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, float x, float y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, float x, float y, Color color, boolean shadow) {
        this.font.drawText(this, text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y, ColorCode color) {
        this.textLeft(String.valueOf(text), x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y, Color color, boolean shadow) {
        this.font.drawText(this, text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y, ColorCode color, boolean shadow) {
        this.font.drawText(this, text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, float x, float y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, float x, float y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, float x, float y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, float x, float y, Color color, boolean shadow) {
        this.font.drawText(this, text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, int x, int y) {
        this.textLeft(text, x - this.font.width(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, int x, int y, Color color) {
        this.textLeft(text, x - this.font.width(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float x, float y) {
        this.textLeft(text, x - this.font.width(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float x, float y, Color color) {
        this.textLeft(text, x - this.font.width(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float x, float y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, int x, int y) {
        this.textLeft(text, x - this.font.width(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, int x, int y, Color color) {
        this.textLeft(text, x - this.font.width(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float x, float y) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float x, float y, Color color) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float x, float y, boolean shadow) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float x, float y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float x, float y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float x, float y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, int x, int y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, int x, int y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, int x, int y) {
        this.textLeft(text, x - this.font.width(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, int x, int y, Color color) {
        this.textLeft(text, x - this.font.width(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, float x, float y) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, float x, float y, Color color) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, float x, float y, boolean shadow) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, float x, float y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, float x, float y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, float x, float y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, int x, int y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, int x, int y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, int x, int y) {
        this.textLeft(text, x - this.font.width(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, int x, int y, Color color) {
        this.textLeft(text, x - this.font.width(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, float x, float y) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, float x, float y, Color color) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, float x, float y, boolean shadow) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - (float) this.font.width(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, float x, float y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, float x, float y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, float x, float y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, int x, int y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, int x, int y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float x, float y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float x, float y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float x, float y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, int x, int y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, int x, int y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.font.width(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float scale, int x, int y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float scale, int x, int y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float scale, int x, int y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float scale, int x, int y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float scale, float x, float y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float scale, float x, float y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float scale, float x, float y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float scale, float x, float y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float scale, int x, int y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float scale, int x, int y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float scale, int x, int y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float scale, int x, int y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float scale, float x, float y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float scale, float x, float y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float scale, float x, float y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, float scale, float x, float y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, int x, int y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.font.width(text) / 2, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, int x, int y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.font.width(text) / 2, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, int x, int y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.font.width(text) / 2, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, int x, int y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.font.width(text) / 2, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, float x, float y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.font.width(text) / 2, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, float x, float y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.font.width(text) / 2, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, float x, float y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.font.width(text) / 2, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, float x, float y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.font.width(text) / 2, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, int x, int y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.font.width(text) / 2, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, int x, int y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.font.width(text) / 2, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, int x, int y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.font.width(text) / 2, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, int x, int y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.font.width(text) / 2, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, float x, float y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.font.width(text) / 2, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, float x, float y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.font.width(text) / 2, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, float x, float y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.font.width(text) / 2, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, float x, float y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.font.width(text) / 2, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float scale, float x, float value) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.font.width(text), value / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float scale, float x, float value, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.font.width(text), value / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float scale, float x, float value, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.font.width(text), value / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float scale, float x, float value, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.font.width(text), value / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float scale, float x, float value) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.font.width(text), value / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float scale, float x, float value, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.font.width(text), value / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float scale, float x, float value, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.font.width(text), value / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float scale, float x, float value, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.font.width(text), value / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textMultiline(@NotNull String text, int x, int y) {
        this.textMultiline(text, x, y, RgbColor.WHITE);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textMultiline(@NotNull String text, int x, int y, Color color) {
        this.textMultiline(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textMultiline(@NotNull String text, int x, int y, boolean shadow) {
        this.textMultiline(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textMultiline(@NotNull String text, int x, int y, Color color, boolean shadow) {
        y -= this.font.lineHeight;

        for (String line : text.split("\n")) {
            y += this.font.lineHeight;
            this.textLeft(line, x, y, color, shadow);
        }

        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textTabbed(@NotNull String text, int x, int y) {
        this.textTabbed(text, x, y, RgbColor.WHITE);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textTabbed(@NotNull String text, int x, int y, Color color) {
        this.textTabbed(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textTabbed(@NotNull String text, int x, int y, boolean shadow) {
        this.textTabbed(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textTabbed(@NotNull String text, int x, int y, Color color, boolean shadow) {
        for (String line : text.split("\t")) {
            this.textLeft(line, x, y, color, shadow);
            x += Renderer.TAB_WIDTH;
        }

        return this;
    }

    @CanIgnoreReturnValue
    public Renderer clear() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        return this;
    }

    ////////////////////////////
    //     Transformation     //
    ////////////////////////////
    @CanIgnoreReturnValue
    public Renderer translate(float x, float y) {
        var translation = this.globalTranslation.peek();
        if (translation != null) {
            translation.add(x, y, 0);
        }
        this.matrices.translate(x, y);
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer translate(int x, int y) {
        var translation = this.globalTranslation.peek();
        if (translation != null) {
            translation.add(x, y, 0);
        }
        this.matrices.translate(x, y);
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer translate(float x, float y, float z) {
        var translation = this.globalTranslation.peek();
        if (translation != null) {
            translation.add(x, y, z);
        }
        this.matrices.translate(x, y, z);
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer translate(int x, int y, int z) {
        var translation = this.globalTranslation.peek();
        if (translation != null) {
            translation.add(x, y, z);
        }
        this.matrices.translate(x, y, z);
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer rotate(double x, double y) {
        this.matrices.rotate(new Quaternion(1, 0, 0, (float) x));
        this.matrices.rotate(new Quaternion(0, 1, 0, (float) y));
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer scale(double sx, double sy) {
        this.matrices.scale((float) sx, (float) sy);
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    public Matrix4 getTransform() {
        return this.matrices.last();
    }

    public float getStrokeWidth() {
        return this.strokeWidth;
    }

    public Color getColor() {
        com.badlogic.gdx.graphics.Color color = new com.badlogic.gdx.graphics.Color();
        com.badlogic.gdx.graphics.Color.abgr8888ToColor(color, this.shapes.getPackedColor());
        return Color.fromGdx(color);
    }

    public Font getFont() {
        return this.font;
    }

    ///////////////////////////
    //     Miscellaneous     //
    ///////////////////////////
    @ApiStatus.Experimental
    @CanIgnoreReturnValue
    public Renderer drawRegion(int x, int y, int width, int height, Consumer<Renderer> consumer) {
        this.pushMatrix();
        this.translate(x, y);
        if (this.pushScissors(x, y, width, height)) {
            consumer.accept(this);
            this.popScissors();
        }
        this.popMatrix();
        return this;
    }

    @ApiStatus.Internal
    public boolean pushScissorsRaw(int x, int y, int width, int height) {
        return this.pushScissorsInternal(new Rectangle(x, y, width, height));
    }

    @CheckReturnValue
    private boolean pushScissorsInternal(Rectangle rect) {
        rect.getPosition(this.tmp2A);
        this.tmp3A.set(this.globalTranslation.peek());
        rect.setPosition(this.tmp2A.add(this.tmp3A.x, this.tmp3A.y));

        if (rect.x < 0) {
            rect.width = Math.max(rect.width + rect.x, 0);
            rect.x = 0;
        }

        if (rect.y < 0) {
            rect.height = Math.max(rect.height + rect.y, 0);
            rect.y = 0;
        }

        if (rect.width < 1) return false;
        if (rect.height < 1) return false;

        rect.y = this.client.getHeight() - rect.y - rect.height;

        this.flush();
        return ScissorStack.pushScissors(rect);
    }

    @CheckReturnValue
    public boolean pushScissors(int x, int y, int width, int height) {
        this.flush();
        return this.pushScissorsInternal(new Rectangle(
                x * this.client.getGuiScale(), y * this.client.getGuiScale(),
                width * this.client.getGuiScale(), height * this.client.getGuiScale())
        );
    }

    @CheckReturnValue
    public boolean pushScissors(float x, float y, float width, float height) {
        this.flush();
        return this.pushScissorsInternal(new Rectangle(
                x * this.client.getGuiScale(), y * this.client.getGuiScale(),
                width * this.client.getGuiScale(), height * this.client.getGuiScale())
        );
    }

    @CheckReturnValue
    public boolean pushScissors(Rectangle rect) {
        this.flush();
        return this.pushScissorsInternal(new Rectangle(
                rect.x * this.client.getGuiScale(), rect.y * this.client.getGuiScale(),
                rect.width * this.client.getGuiScale(), rect.height * this.client.getGuiScale())
        );
    }

    @CanIgnoreReturnValue
    public Rectangle popScissors() {
        this.flush();
        return ScissorStack.popScissors();
    }

    @CanIgnoreReturnValue
    public Renderer flush() {
        this.batch.flush();
        Gdx.gl.glFlush();
        return this;
    }

    @ApiStatus.Experimental
    @CanIgnoreReturnValue
    public Renderer clearScissors() {
        while (ScissorStack.peekScissors() != null) {
            ScissorStack.popScissors();
        }
        return this;
    }

    @Override
    public String toString() {
        return "Renderer{}";
    }

    @CanIgnoreReturnValue
    public Renderer font(Font font) {
        this.font = font;
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer blitColor(Color color) {
        this.blitColor = color;
        return this;
    }

    public Color getBlitColor() {
        return this.blitColor;
    }

    public void setBlitColor(Color blitColor) {
        this.batch.setColor(this.blitColor.toGdx());
        this.blitColor = blitColor;
    }

    @CanIgnoreReturnValue
    public Renderer pushMatrix() {
        Vector3 peek = this.globalTranslation.peek();
        if (this.globalTranslation.peek() == null)
            throw new IllegalStateException("Global translation is null");

        this.globalTranslation.push(peek.cpy());
        this.matrices.push();
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer popMatrix() {
        this.globalTranslation.pop();
        this.matrices.pop();
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    public Batch getBatch() {
        return this.batch;
    }

    public Vector3 getGlobalTranslation() {
        return Objects.requireNonNull(this.globalTranslation.peek()).cpy();
    }

    @CanIgnoreReturnValue
    public Renderer fill(int x, int y, int width, int height, Color rgb) {
        this.setColor(rgb);
        this.rect(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer box(int x, int y, int width, int height, Color rgb) {
        this.setColor(rgb);
        this.rectLine(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer draw9PatchTexture(Texture texture, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int texWidth, int texHeight) {
        this.blit(texture, x, y + height - vHeight, uWidth, vHeight, u, v, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x + width - uWidth, y + height - vHeight, uWidth, vHeight, u + uWidth * 2, v, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x, y, uWidth, vHeight, u, v + vHeight * 2, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x + width - uWidth, y, uWidth, vHeight, u + uWidth * 2, v + vHeight * 2, uWidth, vHeight, texWidth, texHeight);
        for (int dx = x + uWidth; dx < width - uWidth; dx += uWidth) {
            int maxX = Math.min(dx + uWidth, width - uWidth);
            int uW = maxX - dx;
            this.blit(texture, dx, y + height - vHeight, uW, vHeight, u + uWidth, v, uW, vHeight, texWidth, texHeight);
            this.blit(texture, dx, y, uW, vHeight, u + uWidth, v + vHeight * 2, uW, vHeight, texWidth, texHeight);
        }

        for (int dy = y + vHeight; dy < height - vHeight; dy += vHeight) {
            int maxX = Math.min(dy + vHeight, height - vHeight);
            int vH = maxX - dy;
            this.blit(texture, x, dy, uWidth, vH, u, v + uWidth, uWidth, vH, texWidth, texHeight);
            this.blit(texture, x + width - uWidth, dy, uWidth, vH, u + uWidth * 2, u + uWidth, uWidth, vH, texWidth, texHeight);
        }

        return this;
    }

    @CanIgnoreReturnValue
    public Renderer draw9PatchTexture(Identifier id, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int texWidth, int texHeight) {
        Texture texture = this.client.getTextureManager().getTexture(id);

        this.blit(texture, x, y + height - vHeight, uWidth, vHeight, u, v, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x + width - uWidth, y + height - vHeight, uWidth, vHeight, u + uWidth * 2, v, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x, y, uWidth, vHeight, u, v + vHeight * 2, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x + width - uWidth, y, uWidth, vHeight, u + uWidth * 2, v + vHeight * 2, uWidth, vHeight, texWidth, texHeight);
        for (int dx = x + uWidth; dx < width - uWidth; dx += uWidth) {
            int maxX = Math.min(dx + uWidth, width - uWidth);
            int uW = maxX - dx;
            this.blit(texture, dx, y + height - vHeight, uW, vHeight, u + uWidth, v, uW, vHeight, texWidth, texHeight);
            this.blit(texture, dx, y, uW, vHeight, u + uWidth, v + vHeight * 2, uW, vHeight, texWidth, texHeight);
        }

        for (int dy = y + vHeight; dy < height - vHeight; dy += vHeight) {
            int maxX = Math.min(dy + vHeight, height - vHeight);
            int vH = maxX - dy;
            this.blit(texture, x, dy, uWidth, vH, u, v + uWidth, uWidth, vH, texWidth, texHeight);
            this.blit(texture, x + width - uWidth, dy, uWidth, vH, u + uWidth * 2, u + uWidth, uWidth, vH, texWidth, texHeight);
        }

        return this;
    }

    @CanIgnoreReturnValue
    public Renderer setShader(ShaderProgram program) {
        this.batch.setShader(program);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer unsetShader() {
        this.batch.setShader(null);
        return this;
    }

    /**
     * @deprecated Use {@link #external(Runnable)} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    @CanIgnoreReturnValue
    public Renderer model(Runnable block) {
        return this.external(block);
    }

    @CanIgnoreReturnValue
    public Renderer external(Runnable block) {
        boolean drawing = this.batch.isDrawing();
        if (drawing) this.batch.end();
        try {
            block.run();
        } catch (Exception e) {
            QuantumClient.LOGGER.warn("Failed to render model", e);
        }
        if (drawing) this.batch.begin();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer invertOn() {
        this.flush();
        this.batch.setBlendFunctionSeparate(GL20.GL_ONE_MINUS_DST_COLOR, GL20.GL_ONE_MINUS_SRC_COLOR, GL20.GL_ONE, GL20.GL_ZERO);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer invertOff() {
        this.flush();
        this.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer line(float x1, float y1, float x2, float y2, Color color) {
        this.shapes.line(x1, y1, x2, y2, color.toGdx(), this.strokeWidth);
        return this;
    }

    public boolean pushScissors(Bounds bounds) {
        return this.pushScissors(bounds.pos().x, bounds.pos().y, bounds.size().width, bounds.size().height);
    }

    public void polygon(float[] vertices, Color color, int thickness) {
        this.shapes.setColor(color.toGdx());
        this.shapes.polygon(vertices, thickness, JoinType.POINTY);
    }

    public void renderFrame(int x, int y, int w, int h) {
        renderFrame(id("textures/gui/frame.png"), x, y, w, h, 0, 0, 4, 4, 12, 12);
    }

    public void renderFrame(@NotNull Identifier texture, int x, int y, int w, int h, int u, int v, int uvW, int uvH, int texWidth, int texHeight) {
        renderFrame(texture, x, y, w, h, u, v, uvW, uvH, texWidth, texHeight, RgbColor.WHITE);
    }

    public void renderFrame(@NotNull Identifier texture, int x, int y, int w, int h, int u, int v, int uvW, int uvH, int texWidth, int texHeight, @NotNull Color color) {
        Texture handle = this.client.getTextureManager().getTexture(texture);

        w = Math.max(w, uvW * 2);
        h = Math.max(h, uvH * 2);

        int midV = uvH + v;
        int endV = uvH * 2 + v;
        int midU = uvW + u;
        int endU = uvW * 2 + u;
        this.blitColor(RgbColor.WHITE)
                .blit(handle, x, y, uvW, uvH, u, v, uvW, uvH, texWidth, texHeight)
                .blit(handle, x + uvW, y, w - uvW, uvH, midU, v, uvW, uvH, texWidth, texHeight)
                .blit(handle, x + w - uvW, y, uvW, uvH, endU, v, uvW, uvH, texWidth, texHeight)

                .blit(handle, x, y + uvH, uvW, h - uvH * 2, u, midV, uvW, uvH, texWidth, texHeight)
                .blit(handle, x + uvW, y + uvH, w - uvW * 2, h - uvH * 2, midU, midV, uvW, uvH, texWidth, texHeight)
                .blit(handle, x + w - uvW, y + uvH, uvW, h - uvH * 2, endU, midV, uvW, uvH, texWidth, texHeight)

                .blit(handle, x, y + h - uvH, uvW, uvH, u, endV, uvW, uvH, texWidth, texHeight)
                .blit(handle, x + uvW, y + h - uvH, w - uvW * 2, uvH, midU, endV, uvW, uvH, texWidth, texHeight)
                .blit(handle, x + w - uvW, y + h - uvH, uvW, uvH, endU, endV, uvW, uvH, texWidth, texHeight);

    }

    public void draw9Slice(Texture texture, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int inset, int texWidth, int texHeight) {
        this
                // top
                .blit(texture, x, y, inset, inset, u, v, inset, inset, texWidth, texHeight) // left
                .blit(texture, x + inset, y, width - inset * 2, inset, inset + u, v, uWidth - inset * 2, inset, texWidth, texHeight) // center
                .blit(texture, x + width - inset, y, inset, inset, uWidth - inset + u, v, inset, inset, texWidth, texHeight) // right

                // center
                .blit(texture, x, y + inset, inset, height - inset * 2, u, inset + v, inset, vHeight - inset * 2, texWidth, texHeight) // left
                .blit(texture, x + inset, y + inset, width - inset * 2, height - inset * 2, inset + u, inset + v, uWidth - inset * 2, vHeight - inset * 2, texWidth, texHeight) // center
                .blit(texture, x + width - inset, y + inset, inset, height - inset * 2, uWidth - inset + u, inset + v, inset, vHeight - inset * 2, texWidth, texHeight) // right

                // bottom
                .blit(texture, x, y + height - inset, inset, inset, u, vHeight - inset + v, inset, inset, texWidth, texHeight) // left
                .blit(texture, x + inset, y + height - inset, width - inset * 2, inset, inset + u, vHeight - inset + v, uWidth - inset * 2, inset, texWidth, texHeight) // center
                .blit(texture, x + width - inset, y + height - inset, inset, inset, uWidth - inset + u, vHeight - inset + v, inset, inset, texWidth, texHeight); // right
    }

    public void draw9Slice(Identifier texture, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int inset, int texWidth, int texHeight) {
        this
                // top
                .blit(texture, x, y, inset, inset, u, v, inset, inset, texWidth, texHeight) // left
                .blit(texture, x + inset, y, width - inset * 2, inset, inset + u, v, uWidth - inset * 2, inset, texWidth, texHeight) // center
                .blit(texture, x + width - inset, y, inset, inset, uWidth - inset + u, v, inset, inset, texWidth, texHeight) // right

                // center
                .blit(texture, x, y + inset, inset, height - inset * 2, u, inset + v, inset, vHeight - inset * 2, texWidth, texHeight) // left
                .blit(texture, x + inset, y + inset, width - inset * 2, height - inset * 2, inset + u, inset + v, uWidth - inset * 2, vHeight - inset * 2, texWidth, texHeight) // center
                .blit(texture, x + width - inset, y + inset, inset, height - inset * 2, uWidth - inset + u, inset + v, inset, vHeight - inset * 2, texWidth, texHeight) // right

                // bottom
                .blit(texture, x, y + height - inset, inset, inset, u, vHeight - inset + v, inset, inset, texWidth, texHeight) // left
                .blit(texture, x + inset, y + height - inset, width - inset * 2, inset, inset + u, vHeight - inset + v, uWidth - inset * 2, inset, texWidth, texHeight) // center
                .blit(texture, x + width - inset, y + height - inset, inset, inset, uWidth - inset + u, vHeight - inset + v, inset, inset, texWidth, texHeight); // right
    }

    public void begin() {
        if (this.batch.isDrawing()) {
            QuantumClient.LOGGER.warn("Batch still drawing", new Exception());
            this.batch.end();
        }
        this.batch.begin();

        this.iTime = System.currentTimeMillis() / 1000f;
    }

    public void end() {
        if (!this.batch.isDrawing()) {
            QuantumClient.LOGGER.warn("Batch not drawing!", new Exception());
            return;
        }
        this.batch.end();
    }

    public void actuallyEnd() {
        if (this.batch.isDrawing()) {
            QuantumClient.LOGGER.warn("Batch still drawing");
            this.batch.end();
        }
    }

    @Language("GLSL")
    final String VERT =
            """
                    attribute vec4 a_position;
                    attribute vec4 a_color;
                    attribute vec2 a_texCoord0;
                    uniform mat4 u_projTrans;
                    
                    varying vec4 vColor;
                    varying vec2 vTexCoord;
                    
                    void main() {
                    	vColor = a_color;
                    	vTexCoord = a_texCoord0;
                    	gl_Position =  u_projTrans * a_position;
                    }
                    """;

    @Language("GLSL")
    final String FRAG =
            """
                    // Fragment shader
                    #ifdef GL_ES
                    precision mediump float;
                    #endif
                               \s
                    varying vec4 vColor;
                    varying vec2 vTexCoord;
                               \s
                    uniform sampler2D u_texture;
                    uniform vec2 iResolution;
                    uniform float iBlurRadius; // Radius of the blur
                    uniform vec2 iBlurDirection; // Direction of the blur
                               \s
                    void main() {
                      float Pi = 6.28318530718; // Pi*2
                               \s
                      // GAUSSIAN BLUR SETTINGS {{{
                      float Directions = 16.0; // BLUR DIRECTIONS (Default 16.0 - More is better but slower)
                      float Quality = 4.0; // BLUR QUALITY (Default 4.0 - More is better but slower)
                      float Size = iBlurRadius; // BLUR SIZE (Radius)
                      // GAUSSIAN BLUR SETTINGS }}}
                               \s
                      vec2 Radius = Size/iResolution.xy;
                               \s
                      // Normalized pixel coordinates (from 0 to 1)
                      vec2 uv = gl_FragCoord.xy/iResolution.xy;
                      // Pixel colour
                      vec4 color = texture2D(u_texture, uv);
                               \s
                      // Blur calculations
                      for( float d=0.0; d<Pi; d+=Pi/Directions)
                      {
                        for(float i=1.0/Quality; i<=1.0; i+=1.0/Quality)
                        {
                          color += texture2D(u_texture, uv+vec2(cos(d),sin(d))*Radius*i);
                        }
                      }
                     \s
                      // Gamma correction
                      float Gamma = 1.05;
                      color.rgba = pow(color.rgba, vec4(1.0/Gamma));
                               \s
                      // Output to screen
                      color /= Quality * Directions;
                      gl_FragColor = color;
                    }
                    """;


//    @Language("GLSL")
    final String GRID_FRAG =
                    """
                    varying vec2 vTexCoord;
                    varying vec4 vColor;
                    uniform sampler2D u_texture;
                    uniform vec2 iResolution;
                    uniform vec3 hexagonColor;
                    uniform float hexagonTransparency;
                    
                    float rng( in vec2 pos )
                    {
                        return fract(sin( pos.y + pos.x*78.233 )*43758.5453)*2.0 - 1.0;
                    }
                    
                    float simplexValue1DPart(vec2 uv, float ix) {
                        float x = uv.x - ix;
                        float f = 1.0 - x * x;
                        float f2 = f * f;
                        float f3 = f * f2;
                        return f3;
                    }
                    
                    float simplexValue1D(vec2 uv) {
                        vec2 iuv = floor(uv);   \s
                        float n = simplexValue1DPart(uv, iuv.x);
                        n += simplexValue1DPart(uv, iuv.x + 1.0);
                        return rng(vec2(n * 2.0 - 1.0, 0.0));
                    }
                    
                    float perlin( in float pos )
                    {
                        // Get node values
                       \s
                        float a = rng( vec2(floor(pos), 1.0) );
                        float b = rng( vec2(ceil( pos), 1.0) );
                       \s
                        float a_x = rng( vec2(floor(pos), 2.0) );
                        float b_x = rng( vec2(ceil( pos), 2.0) );
                       \s
                        a += a_x*fract(pos);
                        b += b_x*(fract(pos)-1.0);
                       \s
                       \s
                       \s
                        // Interpolate values
                       \s
                        return a + (b-a)*smoothstep(0.0,1.0,fract(pos));
                    }
                    
                    void main() {\s
                      vec2 uv = gl_FragCoord.xy;
                      uv /= 24.0;
                    
                      vec4 color = texture2D(u_texture, vTexCoord);
                      const float A = 0.0;
                      const float B = 0.15;
                    
                      float x = uv.x;
                      float y = (uv.y) * (1.5 / 3.0);
                    
                      float val = (0.5 + 0.5 * x + 0.5 * y);
                    
                      float noise = perlin(val);
                      if (noise > 0.1) {
                          noise = -1.0;
                      }
                    
                      noise = 1.0 - (noise + 1.0) / 2.0;
                    
                      color.rgb = vec3(1.0);
                      color.a = color.a * (noise * (B - A)) + A;
                    
                      gl_FragColor = color;
                    }
                    """;

    @ApiStatus.Experimental
    public void blurred(Runnable block) {
        blurred(true, block);
    }

    @ApiStatus.Experimental
    public void blurred(float radius, Runnable block) {
        blurred(radius, true, block);
    }

    @ApiStatus.Experimental
    public void blurred(boolean grid, Runnable block) {
        blurred(grid, 1, block);
    }

    @ApiStatus.Experimental
    public void blurred(float radius, boolean grid, Runnable block) {
        blurred(radius, grid, 1, block);
    }

    @ApiStatus.Experimental
    public void blurred(boolean grid, int guiScale, Runnable block) {
        blurred(ClientConfig.blurRadius, grid, guiScale, block);
    }

    @ApiStatus.Experimental
    public void blurred(float radius, boolean grid, int guiScale, Runnable block) {
        blurred(1.0F, radius, grid, guiScale, block);
    }

    @ApiStatus.Experimental
    public void blurred(float overlayOpacity, float radius, boolean grid, int guiScale, Runnable block) {
        if (this.blurred) {
            block.run();
            return;
        }

        this.blurred = true;
        try {
            FrameBuffer blurTargetA = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
            FrameBuffer blurTargetB = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
            TextureRegion fboRegion = new TextureRegion(blurTargetA.getColorBufferTexture());

            //Start rendering to an offscreen color buffer
            blurTargetA.begin();

            //before rendering, ensure we are using the default shader
            batch.setShader(null);

            batch.flush();

            //render the batch contents to the offscreen buffer
            this.flush();

            block.run();

            //finish rendering to the offscreen buffer
            batch.flush();

            //finish rendering to the offscreen buffer
            blurTargetA.end();

            //now let's start blurring the offscreen image
            batch.setShader(blurShader);

            //since we never called batch.end(), we should still be drawing
            //which means are blurShader should now be in use

            // set the shader uniforms
            blurShader.setUniformf("iBlurDirection", 1f, 0f);
            blurShader.setUniformf("iResolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            blurShader.setUniformf("iBlurRadius", radius / guiScale);
            blurShader.setUniformf("iTime", iTime);

            //our first blur pass goes to target B
            blurTargetB.begin();

            //we want to render FBO target A into target B
            fboRegion.setTexture(blurTargetA.getColorBufferTexture());

            //draw the scene to target B with a horizontal blur effect
            this.batch.setColor(1f, 1f, 1f, overlayOpacity);
            batch.draw(fboRegion, 0, 0);

            //flush the batch before ending the FBO
            batch.flush();

            //finish rendering target B
            blurTargetB.end();

            //now we can render to the screen using the vertical blur shader

            //update the blur only along Y-axis
            blurShader.setUniformf("iBlurDirection", 0f, 1f);

            //update the resolution of the blur along Y-axis
            blurShader.setUniformf("iResolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            //update the Y-axis blur radius
            blurShader.setUniformf("radius", radius);

            //draw target B to the screen with a vertical blur effect
            fboRegion.setTexture(blurTargetB.getColorBufferTexture());
            this.batch.setColor(1f, 1f, 1f, overlayOpacity);
            batch.draw(fboRegion, 0, 0);

            //reset to default shader without blurs
            batch.setShader(null);

            this.flush();

            if (grid) {
                //getConfig the texture for the hexagon grid
                Texture colorBufferTexture = this.grid.getColorBufferTexture();

                //render the grid to the screen
                this.batch.setColor(1, 1, 1, 1);
                blurred(32, false, 1, () -> {
                    this.batch.setColor(1f, 1f, 1f, overlayOpacity);
                    this.batch.draw(colorBufferTexture, 0, 0, (float) Gdx.graphics.getWidth() / guiScale, (float) Gdx.graphics.getHeight() / guiScale);
                });
            }

            //dispose of the FBOs
            blurTargetA.dispose();
            blurTargetB.dispose();
        } finally {
            this.blurred = false;
        }
    }

    public void blurred(Texture texture) {
        if (this.blurred) {
            return;
        }

        vfxManager.useAsInput(texture);
        vfxManager.applyEffects();
        vfxManager.renderToScreen(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.flush();
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        // VfxManager manages internal off-screen buffers,
        // which should always match the required viewport (whole screen in our case).
        vfxManager.resize(width, height);

        this.resizeGrid(width, height);
    }

    public void resetGrid() {
        this.resizeGrid(width, height);
    }

    private void resizeGrid(int width, int height) {
        if (width == 0 || height == 0) return;

        if (grid != null) this.grid.dispose();

        this.grid = new FrameBuffer(Format.RGBA8888, width, height, false);
        this.grid.begin();
        Gdx.gl.glClearColor(1, 1, 1, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Color hexagonColor;
        try {
            String hexagonColorHex = ClientConfig.hexagonColor;
            if (hexagonColorHex.length() > 7) hexagonColorHex = hexagonColorHex.substring(0, 7);
            if (hexagonColorHex.length() < 7 && hexagonColorHex.length() > 4)
                hexagonColorHex = hexagonColorHex.substring(0, 4);
            if (hexagonColorHex.length() < 4) hexagonColorHex = "#ffffff";
            hexagonColor = RgbColor.hex(hexagonColorHex);
        } catch (InvalidValueException e) {
            hexagonColor = RgbColor.WHITE;
        }

        float hexagonTransparency = ClientConfig.hexagonTransparency;

        this.batch.begin();
        this.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        this.batch.setShader(gridShader);
        this.gridShader.setUniformf("iResolution", width, height);
//        this.gridShader.setUniformf("iColor", hexagonColor.getRed() / 255f, hexagonColor.getGreen() / 255f, hexagonColor.getBlue() / 255f, hexagonTransparency);

        this.shapes.filledRectangle(0, 0, width, height, RgbColor.WHITE.toGdx());

        this.batch.setShader(null);
        this.batch.end();
        this.grid.end();

        var old = this.grid;

//        this.grid = new FrameBuffer(Format.RGBA8888, width, height, false);
//        this.grid.begin();
//        this.batch.begin();
//        this.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//
//        Gdx.gl.glClearColor(0, 0, 0, 0);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//        this.batch.draw(old.getColorBufferTexture(), 0, 0, width, height);
//        this.batch.flush();
//
//        this.batch.setShader(null);
//        this.batch.end();
//        this.grid.end();
//
//        old.dispose();
    }

    @Override
    public void dispose() {
        vfxManager.dispose();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isBlurred() {
        return blurred;
    }

    public void finish() {
        if (this.batch.isDrawing()) {
            QuantumClient.LOGGER.warn("Batch still drawing!");
            this.batch.end();
        }
        this.matrices.reset();
        this.renderablePool.flush();
    }

    public Renderable obtainRenderable() {
        return this.renderablePool.obtain();
    }
}
