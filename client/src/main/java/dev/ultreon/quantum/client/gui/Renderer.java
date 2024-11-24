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
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Layout;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import dev.ultreon.libs.commons.v0.Anchor;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.GameFont;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.client.world.RenderablePool;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.text.FormattedText;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Color;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.util.Vec4i;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The Renderer class is responsible for rendering shapes, textures, and various graphics elements.
 * It provides methods to set properties such as color and stroke width, and draw shapes like circles, rectangles,
 * lines, and textures. The rendering context can be manipulated using the provided matrices.
 */
@SuppressWarnings({"unused", "IntegerDivisionInFloatingPointContext"})
public class Renderer implements Disposable {
    private static final int TAB_WIDTH = 32;
    public static final float OVERLAY_ZINDEX = 2000;
    public static final int TOOLTIP_ZINDEX = 100;
    public static final @NotNull RgbColor TRANSPARENT_BLACK = RgbColor.BLACK.withAlpha(0x80);

    private final QuantumClient client = QuantumClient.get();
    private final Deque<Vector3> globalTranslation = new ArrayDeque<>();
    private final Deque<Vector3> globalScale = new ArrayDeque<>();
    private final Batch batch;
    private final ShapeDrawer shapes;
    private final TextureManager textureManager;
    private final VfxManager vfxManager;
    private final ShaderProgram blurShader;
    private float strokeWidth = 1;
    private GameFont font;
    private final Matrices matrices;
    private Color blitColor = RgbColor.rgb(0xffffff);
    private final Vector2 tmp2A = new Vector2();
    private final Vector3 tmp3A = new Vector3();
    private final GlStateStack glState = new GlStateStack();
    private final TextureRegion tmpUv = new TextureRegion();
    private final Quaternion tmpQ = new Quaternion();
    private int width;
    private int height;
    private int scissorOffsetX;
    private int scissorOffsetY;
    private boolean blurred;

    private FrameBuffer blurTargetA = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
    private FrameBuffer blurTargetB = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

    public static final int FBO_SIZE = 1024;

    private final RenderablePool renderablePool = new RenderablePool();
    private final com.badlogic.gdx.graphics.Color tmpC = new com.badlogic.gdx.graphics.Color();
    private float iTime;
    private boolean hexItems;
    private long shouldCheckMathDay;
    private com.badlogic.gdx.graphics.Color fontColor = new com.badlogic.gdx.graphics.Color();
    private final Layout layout = new Layout();
    private String layoutText = "";

    /**
     * Constructs a new Renderer object with the specified ShapeDrawer.
     *
     * @param shapes The ShapeDrawer instance used for drawing shapes.
     */
    public Renderer(ShapeDrawer shapes) {
        this(shapes, new Matrices());
    }

    /**
     * Constructs a new Renderer object responsible for managing and drawing shapes,
     * applying matrix transformations, handling textures, and managing visual effects.
     *
     * @param shapes shape drawer used for rendering various shapes
     * @param matrices matrix manager responsible for handling matrix operations and transformations
     */
    public Renderer(ShapeDrawer shapes, Matrices matrices) {
        // Create a new global translation.
        this.globalTranslation.push(new Vector3());

        // Set the default font.
        this.font = this.client.font;

        // Initialize the matrices, the shape drawer, and the batch.
        GL30 gl30 = Gdx.gl30;
        this.batch = shapes.getBatch();
        this.shapes = shapes;
        this.matrices = matrices;

        // Receive the texture manager from the client.
        this.textureManager = this.client.getTextureManager();
        if (this.textureManager == null) throw new IllegalArgumentException("Texture manager isn't initialized yet!");

        // Projection matrix.
        this.matrices.onEdit = matrix -> shapes.getBatch().setTransformMatrix(matrix);


        // VfxManager is a host for the effects.
        // It captures rendering into internal off-screen buffer and applies a chain of defined effects.
        // Off-screen buffers may have any pixel format; for this example, we will use RGBA8888.
        vfxManager = new VfxManager(Format.RGBA8888);

        // Create our blur shader and grid shader
        // The grid shader is used to draw the grid on the screen, and only once per resize.
        // The blur shader is used to blur behind the grid, and is drawn every frame.
        blurShader = new ShaderProgram(VERT, FRAG);
        String log = blurShader.getLog();
        if (!blurShader.isCompiled()) {
            for (String line : log.lines().toList()) {
                CommonConstants.LOGGER.error(line);
            }
            QuantumClient.crash(new IllegalStateException("Failed to compile blur shader!"));
        }
        if (!log.isEmpty()) {
            for (String line : log.lines().toList()) {
                CommonConstants.LOGGER.warn(line);
            }
        }

        //setup uniforms for our shader
        blurShader.bind();
        blurShader.setUniformf("iBlurDirection", 0f, 0f);
        blurShader.setUniformf("radius", 1f);
    }

    /**
     * Retrieves the current Matrices object.
     *
     * @return the Matrices object associated with this instance.
     */
    public Matrices getMatrices() {
        return this.matrices;
    }

    /**
     * Sets the width of the stroke for the renderer.
     *
     * @param strokeWidth the width to set for the stroke
     * @return the current instance of the Renderer for method chaining
     */
    @CanIgnoreReturnValue
    public Renderer setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    /**
     * Sets the color of the renderer. If the provided color is null, the method will return the current instance
     * without making any changes.
     *
     * @param c the Color object to set; if null, no changes will be made
     * @return the current instance of Renderer after setting the color*/
    @CanIgnoreReturnValue
    public Renderer setColor(Color c) {
        if (c == null) return this;
        if (this.font != null)
            this.fontColor.set(c.toGdx());
        this.shapes.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
        return this;
    }


    /**
     * Sets the color for the renderer, affecting both the font and shapes.
     *
     * @param c the color to be set. If null, the method will return without modifying*/
    @CanIgnoreReturnValue
    public Renderer setColor(com.badlogic.gdx.graphics.Color c) {
        if (c == null) return this;
        if (this.font != null)
            this.fontColor.set(c);
        this.shapes.setColor(c);
        return this;
    }

    /**
     * Sets the color of the renderer using RGB values.
     *
     * @param r the red component of the color (0-255)
     * @param g the green component of the color (0-255)
     * @param b the blue component of the color (0-255)
     * @return the Renderer instance with the updated color
     */
    @CanIgnoreReturnValue
    public Renderer setColor(int r, int g, int b) {
        this.setColor(this.tmpC.set(r / 255f, g / 255f, b / 255f, 1f));
        return this;
    }

    /**
     * Sets the color of the renderer using the specified red, green, and blue values.
     *
     * @param r the red component of the color, typically between 0 and 1
     * @param g the green component of the color*/
    @CanIgnoreReturnValue
    public Renderer setColor(float r, float g, float b) {
        this.setColor(this.tmpC.set(r, g, b, 1f));
        return this;
    }

    /**
     * Sets the color using RGBA values.
     *
     * @param r The red component of the color, in the range 0-255.
     * @param g The green component of the color, in the range 0-255.
     * @param b The blue component of the color, in the range 0-255.
     * @param a The alpha (transparency) component of the*/
    @CanIgnoreReturnValue
    public Renderer setColor(int r, int g, int b, int a) {
        this.setColor(this.tmpC.set(r / 255f, g / 255f, b / 255f, a / 255f));
        return this;
    }

    /**
     * Sets the color of the renderer using the specified red, green, blue, and alpha values.
     *
     * @param r the red component of the color
     * @param g the green component of the color
     * @param b the blue component of the color
     * @param a the alpha component of the color
     * @return the current instance of the*/
    @CanIgnoreReturnValue
    public Renderer setColor(float r, float g, float b, float a) {
        this.setColor(this.tmpC.set(r, g, b, a));
        return this;
    }

    /**
     * Sets the color of the renderer using an ARGB integer value.
     *
     * @param argb An integer representing the color with alpha, red, green, and blue components.
     *             The format should be 0xAARRGGBB where AA is alpha, RR is red, GG is green, and BB is blue.
     * @return The Renderer instance with*/
    @CanIgnoreReturnValue
    public Renderer setColor(int argb) {
        this.setColor(this.tmpC.set((argb >> 16 & 0xFF) / 255f, (argb >> 8 & 0xFF) / 255f, (argb & 0xFF) / 255f, (argb >> 24 & 0xFF) / 255f));
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
        this.enableBlend();
        this.shapes.filledRectangle(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer rect(float x, float y, float width, float height) {
        this.enableBlend();
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

    /**
     * Draws a texture at the specified coordinates.
     * This assumes that the texture is 256×256.
     *
     * @param tex the texture to draw
     * @param x   the setX coordinate
     * @param y   the setY coordinate
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float, float, float)
     */
    @CanIgnoreReturnValue
    public Renderer blit(TextureRegion tex, float x, float y) {
        if (tex == null) tex = TextureManager.DEFAULT_TEX_REG;
        this.batch.setColor(this.blitColor.toGdx());
        this.batch.draw(tex, x, y + tex.getRegionHeight(), tex.getRegionWidth(), -tex.getRegionHeight());
        return this;
    }

    /**
     * Draws a texture at the specified coordinates with the specified width and height.
     * This assumes that the texture is 256×256.
     *
     * @param tex    the texture to draw
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param width  the width
     * @param height the height
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float, float, float)
     */
    @CanIgnoreReturnValue
    public Renderer blit(TextureRegion tex, float x, float y, float width, float height) {
        if (tex == null) tex = TextureManager.DEFAULT_TEX_REG;
        this.batch.setColor(this.blitColor.toGdx());
        this.batch.draw(tex, x, y + height, width, -height);
        return this;
    }

    /**
     * Draws a texture at the specified coordinates.
     * This assumes that the texture is 256×256.
     *
     * @param tex the texture to draw
     * @param x   the setX coordinate
     * @param y   the setY coordinate
     * @return this
     * @see #blit(NamespaceID, float, float, float, float)
     */
    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y) {
        this.batch.setColor(this.blitColor.toGdx());
        this.batch.draw(tex, x, y + tex.getHeight(), tex.getWidth(), -tex.getHeight());
        return this;
    }

    /**
     * Draws a texture at the specified coordinates with a background color.
     * This assumes that the texture is 256×256.
     *
     * @param tex             the texture to draw
     * @param x               the setX coordinate
     * @param y               the setY coordinate
     * @param backgroundColor the background color to use
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float, Color)
     */
    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, Color backgroundColor) {
        this.setColor(backgroundColor);
        this.rect(x, y, tex.getWidth(), tex.getHeight());
        this.batch.setColor(this.blitColor.toGdx());
        this.batch.draw(tex, x, y + tex.getHeight(), tex.getWidth(), -tex.getHeight());
        return this;
    }

    /**
     * Draws a texture at the specified coordinates with a background color and the specified width and height.
     * This assumes that the texture is 256×256.
     *
     * @param tex             the texture to draw
     * @param x               the setX coordinate
     * @param y               the setY coordinate
     * @param width           the width
     * @param height          the height
     * @param backgroundColor the background color to use
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float, Color)
     */
    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, Color backgroundColor) {
        this.blit(tex, x, y, width, height, 0.0F, 0.0F, backgroundColor);
        return this;
    }

    /**
     * Draws a texture at the specified coordinates with a background color and the specified width and height.
     * This also allows you to specify the UV coordinates of the texture.
     * This assumes that the texture is 256×256.
     *
     * @param x               the setX coordinate
     * @param y               the setY coordinate
     * @param width           the width
     * @param height          the height
     * @param u               the texture u coordinate of the region
     * @param v               the texture v coordinate of the region
     * @param backgroundColor the background color to use
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float, float, float, Color)
     */
    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v, Color backgroundColor) {
        this.blit(tex, x, y, width, height, u, v, width, height, backgroundColor);
        return this;
    }

    /**
     * Draws a texture at the specified coordinates with a background color and the specified width and height.
     * This also allows you to specify the UV coordinates of the texture.
     * And the UV width and height.
     * This assumes that the texture is 256×256.
     *
     * @param tex             the texture to draw
     * @param x               the setX coordinate
     * @param y               the setY coordinate
     * @param width           the width
     * @param height          the height
     * @param u               the texture u coordinate of the region
     * @param v               the texture v coordinate of the region
     * @param uWidth          the texture uv width
     * @param vHeight         the texture uv height
     * @param backgroundColor the background color to use
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float, float, float, int, int, Color)
     */
    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, Color backgroundColor) {
        this.blit(tex, x, y, width, height, u, v, uWidth, vHeight, 256, 256, backgroundColor);
        return this;
    }

    /**
     * Draws a texture at the specified coordinates with a background color and the specified width and height.
     * This also allows you to specify the UV coordinates of the texture.
     * And the UV width and height.
     *
     * @param tex             the texture to draw
     * @param x               the setX coordinate
     * @param y               the setY coordinate
     * @param width           the width
     * @param height          the height
     * @param u               the texture u coordinate of the region
     * @param v               the texture v coordinate of the region
     * @param uWidth          the texture uv width
     * @param vHeight         the texture uv height
     * @param texWidth        the texture width
     * @param texHeight       the texture height
     * @param backgroundColor the background color to use
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float, float, float, int, int, Color)
     */
    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight, Color backgroundColor) {
        this.setColor(backgroundColor);
        this.rect(x, y, width, height);
        this.batch.setColor(this.blitColor.toGdx());
        tmpUv.setTexture(tex);
        tmpUv.setRegion(texWidth / u, texHeight / v, texWidth / (u + uWidth), texHeight / (v + vHeight));
        this.batch.draw(tmpUv, x, y + height, width, -height);
        return this;
    }

    /**
     * Draws a texture at the specified coordinates
     * This assumes that the texture is 256×256.
     *
     * @param tex    the texture to draw
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param width  the width
     * @param height the height
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float)
     */
    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height) {
        this.blit(tex, x, y, width, height, 0.0F, 0.0F);
        return this;
    }

    /**
     * Draws a texture at the specified coordinates
     *
     * @param tex    the texture to draw
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param width  the width
     * @param height the height
     * @param u      the texture u coordinate of the region
     * @param v      the texture v coordinate of the region
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float)
     */
    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v) {
        this.blit(tex, x, y, width, height, u, v, width, height);
        return this;
    }

    /**
     * Draws a texture at the specified coordinates
     *
     * @param tex     the texture to draw
     * @param x       the setX coordinate
     * @param y       the setY coordinate
     * @param width   the width
     * @param height  the height
     * @param u       the texture u coordinate of the region
     * @param v       the texture v coordinate of the region
     * @param uWidth  the texture uv width
     * @param vHeight the texture uv height
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float, float, float)
     */
    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight) {
        this.blit(tex, x, y, width, height, u, v, uWidth, vHeight, 256, 256);
        return this;
    }

    /**
     * Draws a texture at the specified coordinates
     *
     * @param tex       the texture to draw
     * @param x         the setX coordinate
     * @param y         the setY coordinate
     * @param width     the width
     * @param height    the height
     * @param u         the texture u coordinate of the region
     * @param v         the texture v coordinate of the region
     * @param uWidth    the texture uv width
     * @param vHeight   the texture uv height
     * @param texWidth  the texture width
     * @param texHeight the texture height
     * @return this
     * @see #blit(NamespaceID, float, float, float, float, float, float, float, float, int, int)
     */
    @ApiStatus.Internal
    @CanIgnoreReturnValue
    public Renderer blit(Texture tex, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight) {
        this.batch.setColor(this.blitColor.toGdx());
        this.tmpUv.setTexture(tex);
        this.tmpUv.setRegion(1 * u / texWidth, 1 * v / texHeight, 1 * (u + uWidth) / texWidth, 1 * (v + vHeight) / texHeight);
        this.batch.draw(tmpUv, x, y + height, width, -height);
        return this;
    }

    /**
     * Draws a texture by id at the specified coordinates
     *
     * @param id              the texture identifier
     * @param x               the setX coordinate
     * @param y               the setY coordinate
     * @param width           the width
     * @param height          the height
     * @param backgroundColor the background color
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer blit(NamespaceID id, float x, float y, float width, float height, Color backgroundColor) {
        this.blit(id, x, y, width, height, 0.0F, 0.0F, backgroundColor);
        return this;
    }

    /**
     * Draws a texture by id at the specified coordinates
     *
     * @param id              the texture identifier
     * @param x               the setX coordinate
     * @param y               the setY coordinate
     * @param width           the width
     * @param height          the height
     * @param u               the texture u coordinate of the region
     * @param v               the texture v coordinate of the region
     * @param backgroundColor the background color
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer blit(NamespaceID id, float x, float y, float width, float height, float u, float v, Color backgroundColor) {
        Texture texture = this.textureManager.getTexture(id);
        this.blit(id, x, y, width, height, u, v, 256, 256, backgroundColor);
        return this;
    }

    /**
     * Draws a texture by id at the specified coordinates
     *
     * @param id              the texture identifier
     * @param x               the setX coordinate
     * @param y               the setY coordinate
     * @param width           the width
     * @param height          the height
     * @param u               the texture u coordinate of the region
     * @param v               the texture v coordinate of the region
     * @param uWidth          the texture uv width
     * @param vHeight         the texture uv height
     * @param backgroundColor the background color
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer blit(NamespaceID id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, Color backgroundColor) {
        Texture texture = this.textureManager.getTexture(id);
        this.blit(id, x, y, width, height, u, v, uWidth, vHeight, 256, 256, backgroundColor);
        return this;
    }

    /**
     * Draws a texture by id at the specified coordinates
     *
     * @param id     the texture identifier
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param width  the width
     * @param height the height
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer blit(NamespaceID id, float x, float y, float width, float height) {
        this.blit(id, x, y, width, height, 0.0F, 0.0F);
        return this;
    }

    /**
     * Draws a texture by id at the specified coordinates
     *
     * @param id     the texture identifier
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param width  the width
     * @param height the height
     * @param u      the texture u coordinate of the region
     * @param v      the texture v coordinate of the region
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer blit(NamespaceID id, float x, float y, float width, float height, float u, float v) {
        this.blit(id, x, y, width, height, u, v, width, height);
        return this;
    }

    /**
     * Draws a texture by id at the specified coordinates
     *
     * @param id      the texture identifier
     * @param x       the setX coordinate
     * @param y       the setY coordinate
     * @param width   the width
     * @param height  the height
     * @param u       the texture u coordinate of the region
     * @param v       the texture v coordinate of the region
     * @param uWidth  the texture uv width
     * @param vHeight the texture uv height
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer blit(NamespaceID id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight) {
        this.blit(id, x, y, width, height, u, v, uWidth, vHeight, 256, 256);
        return this;
    }

    /**
     * Draws a texture by id at the specified coordinates
     *
     * @param id        the texture identifier
     * @param x         the setX coordinate
     * @param y         the setY coordinate
     * @param width     the width
     * @param height    the height
     * @param u         the texture u coordinate of the region
     * @param v         the texture v coordinate of the region
     * @param uWidth    the texture uv width
     * @param vHeight   the texture uv height
     * @param texWidth  the texture width
     * @param texHeight the texture height
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer blit(NamespaceID id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight) {
        this.batch.setColor(this.blitColor.toGdx());
        Texture tex = this.textureManager.getTexture(id);
        this.tmpUv.setTexture(tex);
        this.tmpUv.setRegion(1 * u / texWidth, 1 * v / texHeight, 1 * (u + uWidth) / texWidth, 1 * (v + vHeight) / texHeight);
        this.batch.draw(this.tmpUv, x, y + height, width, -height);
        return this;
    }

    /**
     * Draws a texture by id at the specified coordinates
     *
     * @param id              the texture identifier
     * @param x               the setX coordinate
     * @param y               the setY coordinate
     * @param width           the width
     * @param height          the height
     * @param u               the texture u coordinate of the region
     * @param v               the texture v coordinate of the region
     * @param uWidth          the texture uv width
     * @param vHeight         the texture uv height
     * @param texWidth        the texture width
     * @param texHeight       the texture height
     * @param backgroundColor the background color
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer blit(NamespaceID id, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight, Color backgroundColor) {
        this.setColor(backgroundColor);
        this.rect(x, y, width, height);
        Texture tex = this.textureManager.getTexture(id);
        this.batch.setColor(this.blitColor.toGdx());
        this.tmpUv.setTexture(tex);
        this.tmpUv.setRegion(texWidth / u, texHeight / v, texWidth / (u + uWidth), texHeight / (v + vHeight));
        this.batch.draw(tmpUv, x, y + height, width, -height);
        return this;
    }

    /**
     * Draws a sprite
     *
     * @param sprite the sprite
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer drawSprite(Sprite sprite, int x, int y) {
        drawSprite(sprite, x, y, sprite.getWidth(), sprite.getHeight());
        return this;
    }

    /**
     * Draws a sprite
     *
     * @param sprite the sprite
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param width  the width
     * @param height the height
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer drawSprite(Sprite sprite, int x, int y, int width, int height) {
        sprite.render(this, x, y, width, height);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text the text
     * @param x    the setX coordinate
     * @param y    the setY coordinate
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text  the text
     * @param x     the setX coordinate
     * @param y     the setY coordinate
     * @param color the color
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text  the text
     * @param x     the setX coordinate
     * @param y     the setY coordinate
     * @param color the color
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y, ColorCode color) {
        this.textLeft(text, x, y, RgbColor.of(color), true);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text   the text
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param shadow if the text should be drawn with a shadow
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text   the text
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param color  the color
     * @param shadow if the text should be drawn with a shadow
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y, Color color, boolean shadow) {
        this.drawText(text, x, y, color, shadow);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text   the text
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param color  the color
     * @param shadow if the text should be drawn with a shadow
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, int x, int y, ColorCode color, boolean shadow) {
        this.drawText(text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text the text
     * @param x    the setX coordinate
     * @param y    the setY coordinate
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text  the text
     * @param x     the setX coordinate
     * @param y     the setY coordinate
     * @param color the color
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text  the text
     * @param x     the setX coordinate
     * @param y     the setY coordinate
     * @param color the color
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color) {
        this.textLeft(text, x, y, RgbColor.of(color), true);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text   the text
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param shadow if the text should be drawn with a shadow
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text   the text
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param color  the color
     * @param shadow if the text should be drawn with a shadow
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color, boolean shadow) {
        this.drawText(text, x, y, color, shadow);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text   the text
     * @param x      the setX coordinate
     * @param y      the setY coordinate
     * @param color  the color
     * @param shadow if the text should be drawn with a shadow
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color, boolean shadow) {
        this.drawText(text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param maxWidth the max width
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, float maxWidth, String truncate) {
        this.textLeft(text, x, y, RgbColor.WHITE, maxWidth, truncate);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param color    the color
     * @param maxWidth the max width
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color, float maxWidth, String truncate) {
        this.textLeft(text, x, y, color, true, maxWidth, truncate);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param color    the color
     * @param maxWidth the max width
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color, float maxWidth, String truncate) {
        this.textLeft(text, x, y, RgbColor.of(color), true, maxWidth, truncate);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param shadow   if the text should be drawn with a shadow
     * @param maxWidth the max width
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, boolean shadow, float maxWidth, String truncate) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow, maxWidth, truncate);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param color    the color
     * @param shadow   if the text should be drawn with a shadow
     * @param maxWidth the max width
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color, boolean shadow, float maxWidth, String truncate) {
        this.drawText(text, x, y, color, shadow);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param color    the color
     * @param shadow   if the text should be drawn with a shadow
     * @param maxWidth the max width
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color, boolean shadow, float maxWidth, String truncate) {
        this.drawText(text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param maxWidth the max width
     * @param wrap     if the text should be wrapped
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, float maxWidth, boolean wrap, String truncate) {
        this.textLeft(text, x, y, RgbColor.WHITE, maxWidth, wrap, truncate);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param color    the color
     * @param maxWidth the max width
     * @param wrap     if the text should be wrapped
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color, float maxWidth, boolean wrap, String truncate) {
        this.textLeft(text, x, y, color, true, maxWidth, wrap, truncate);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param color    the color
     * @param maxWidth the max width
     * @param wrap     if the text should be wrapped
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color, float maxWidth, boolean wrap, String truncate) {
        this.textLeft(text, x, y, RgbColor.of(color), true, maxWidth, wrap, truncate);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param maxWidth the max width
     * @param wrap     if the text should be wrapped
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, boolean shadow, float maxWidth, boolean wrap, String truncate) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow, maxWidth, wrap, truncate);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param color    the color
     * @param shadow   if the text should be drawn with a shadow
     * @param maxWidth the max width
     * @param wrap     if the text should be wrapped
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, Color color, boolean shadow, float maxWidth, boolean wrap, String truncate) {
        this.drawText(text, x, y, color, shadow);
        return this;
    }

    /**
     * Draws text anchored to the left
     *
     * @param text     the text
     * @param x        the setX coordinate
     * @param y        the setY coordinate
     * @param color    the color
     * @param shadow   if the text should be drawn with a shadow
     * @param maxWidth the max width
     * @param wrap     if the text should be wrapped
     * @param truncate the text to show when truncated
     * @return this
     */
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull String text, float x, float y, ColorCode color, boolean shadow, float maxWidth, boolean wrap, String truncate) {
        this.drawText(text, x, y, RgbColor.of(color), shadow);
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
        this.drawText(text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull TextObject text, int x, int y, ColorCode color, boolean shadow) {
        this.drawText(text, x, y, RgbColor.of(color), shadow);
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
        this.drawText(text, x, y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y, ColorCode color) {
        this.textLeft(String.valueOf(text), x, y, color, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y, Color color, boolean shadow) {
        this.drawText(text, x, y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, int x, int y, ColorCode color, boolean shadow) {
        this.drawText(text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, float x, float y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, float x, float y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, float x, float y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull FormattedText text, float x, float y, Color color, boolean shadow) {
        this.drawText(text, x, y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y, ColorCode color) {
        this.textLeft(String.valueOf(text), x, y, color, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y, Color color, boolean shadow) {
        this.drawText(text, x, y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, int x, int y, ColorCode color, boolean shadow) {
        this.drawText(text, x, y, RgbColor.of(color), shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, float x, float y) {
        this.textLeft(text, x, y, RgbColor.WHITE);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, float x, float y, Color color) {
        this.textLeft(text, x, y, color, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, float x, float y, boolean shadow) {
        this.textLeft(text, x, y, RgbColor.WHITE, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textLeft(@NotNull List<FormattedText> text, float x, float y, Color color, boolean shadow) {
        this.drawText(text, x, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, int x, int y) {
        this.textLeft(text, x - this.textWidth(text) / 2, y);
        return this;
    }

    public int textWidth(@NotNull String text) {
        this.layout.setFont(font);
        this.layout.clear();
        this.font.markup(text, this.layout);
        return (int) this.layout.getWidth();
    }

    public int textWidth(@NotNull TextObject text) {
        return this.textWidth(text.getText());
    }

    @Deprecated
    private int textWidth(@NotNull FormattedText text) {
        return this.textWidth(text.getText());
    }

    @Deprecated
    private int textWidth(@NotNull List<FormattedText> text) {
        return this.textWidth(text.stream().map(FormattedText::getText).collect(Collectors.joining()));
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, int x, int y, Color color) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float x, float y) {
        this.textLeft(text, x - this.textWidth(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float x, float y, Color color) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float x, float y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, int x, int y) {
        this.textLeft(text, x - this.textWidth(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, int x, int y, Color color) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float x, float y) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float x, float y, Color color) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y, color);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float x, float y, boolean shadow) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float x, float y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float x, float y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float x, float y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, int x, int y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, int x, int y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, int x, int y) {
        this.textLeft(text, x - this.textWidth(text) / 2, y);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, int x, int y, Color color) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, color);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, float x, float y) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, float x, float y, Color color) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y, color);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, float x, float y, boolean shadow) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull FormattedText text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, float x, float y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, float x, float y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, float x, float y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, int x, int y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, int x, int y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull FormattedText text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, int x, int y) {
        this.textLeft(text, x - this.textWidth(text) / 2, y);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, int x, int y, Color color) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, color);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text) / 2, y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, float x, float y) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, float x, float y, Color color) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y, color);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, float x, float y, boolean shadow) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull List<FormattedText> text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - (float) this.textWidth(text) / 2, y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, float x, float y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, float x, float y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, float x, float y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, color, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, int x, int y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, shadow);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, int x, int y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull List<FormattedText> text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float x, float y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float x, float y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float x, float y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float x, float y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, color, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, int x, int y) {
        this.textRight(text, x, y, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, int x, int y, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, shadow);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, int x, int y, Color color) {
        this.textRight(text, x, y, color, true);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, int x, int y, Color color, boolean shadow) {
        this.textLeft(text, x - this.textWidth(text), y, color, shadow);
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
        this.textLeft(text, x / scale - this.textWidth(text) / 2, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, int x, int y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.textWidth(text) / 2, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, int x, int y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.textWidth(text) / 2, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, int x, int y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.textWidth(text) / 2, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, float x, float y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.textWidth(text) / 2, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, float x, float y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.textWidth(text) / 2, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, float x, float y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.textWidth(text) / 2, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull String text, float scale, float x, float y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - this.textWidth(text) / 2, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, int x, int y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.textWidth(text) / 2, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, int x, int y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.textWidth(text) / 2, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, int x, int y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.textWidth(text) / 2, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, int x, int y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.textWidth(text) / 2, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, float x, float y) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.textWidth(text) / 2, y / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, float x, float y, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.textWidth(text) / 2, y / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, float x, float y, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.textWidth(text) / 2, y / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textCenter(@NotNull TextObject text, float scale, float x, float y, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textLeft(text, x / scale - (float) this.textWidth(text) / 2, y / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float scale, float x, float value) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.textWidth(text), value / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float scale, float x, float value, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.textWidth(text), value / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float scale, float x, float value, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.textWidth(text), value / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull TextObject text, float scale, float x, float value, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.textWidth(text), value / scale, color, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float scale, float x, float value) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.textWidth(text), value / scale);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float scale, float x, float value, Color color) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.textWidth(text), value / scale, color);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float scale, float x, float value, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.textWidth(text), value / scale, shadow);
        this.popMatrix();
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer textRight(@NotNull String text, float scale, float x, float value, Color color, boolean shadow) {
        this.pushMatrix();
        this.scale(scale, scale);
        this.textRight(text, x / scale - this.textWidth(text), value / scale, color, shadow);
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
        y -= (int) this.font.getLineHeight();

        for (String line : text.split("\n")) {
            y += (int) (this.font.getLineHeight() + 2);
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
        this.matrices.translate(x, y);
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer translate(int x, int y) {
        this.matrices.translate(x, y);
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer translate(float x, float y, float z) {
        this.matrices.translate(x, y, z);
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer translate(int x, int y, int z) {
        this.matrices.translate(x, y, z);
        this.batch.setTransformMatrix(this.matrices.last());
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer rotate(double x, double y) {
        this.matrices.rotate(tmpQ.set(1, 0, 0, (float) x));
        this.matrices.rotate(tmpQ.set(0, 1, 0, (float) y));
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
        com.badlogic.gdx.graphics.Color.abgr8888ToColor(tmpC, this.shapes.getPackedColor());
        return Color.fromGdx(tmpC);
    }

    public com.github.tommyettinger.textra.Font getFont() {
        return this.font;
    }

    ///////////////////////////
    //     Miscellaneous     //
    ///////////////////////////
    @Deprecated
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

    @Deprecated
    @ApiStatus.Internal
    public boolean pushScissorsRaw(int x, int y, int width, int height) {
        return this.pushScissorsInternal(new Rectangle(x, y, width, height));
    }

    @CheckReturnValue
    private boolean pushScissorsInternal(Rectangle rect) {
        Vector3 translation = this.matrices.getTranslation(this.tmp3A);

        if (translation != null) {
            rect.setPosition(rect.getPosition(this.tmp2A).add(translation.x, translation.y));
        }
        rect.setPosition(rect.getPosition(this.tmp2A).add(0, 0));

        if (rect.x < 0) {
            rect.width = Math.max(rect.width + rect.x, 0);
            rect.x = 0;
        }

        if (rect.y < client.getDrawOffset().y) {
            rect.height = Math.max(rect.height + rect.y, 0);
            rect.y = client.getDrawOffset().y;
        }

        if (rect.width < 1) return false;
        if (rect.height < 1) return false;

        rect.y = Gdx.graphics.getHeight() - rect.y - rect.height;

        if (!Gdx.gl.glIsEnabled(GL20.GL_SCISSOR_TEST)) {
            Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        }

        this.flush();
        return ScissorStack.pushScissors(rect);
    }

    @Deprecated
    @CheckReturnValue
    public boolean pushScissors(int x, int y, int width, int height) {
        this.flush();
        return this.pushScissorsInternal(new Rectangle(
                x * this.client.getGuiScale(), y * this.client.getGuiScale(),
                width * this.client.getGuiScale(), height * this.client.getGuiScale())
        );
    }

    @Deprecated
    @CheckReturnValue
    public boolean pushScissors(float x, float y, float width, float height) {
        this.flush();
        Rectangle rect = new Rectangle();
        rect.x *= this.client.getGuiScale();
        rect.y *= this.client.getGuiScale();
        rect.width *= this.client.getGuiScale();
        rect.height *= this.client.getGuiScale();
        return this.pushScissorsInternal(rect);
    }

    @CheckReturnValue
    public boolean pushScissors(Rectangle rect) {
        this.flush();
        rect.x *= this.client.getGuiScale();
        rect.y *= this.client.getGuiScale();
        rect.width *= this.client.getGuiScale();
        rect.height *= this.client.getGuiScale();
        return this.pushScissorsInternal(rect);
    }

    @CanIgnoreReturnValue
    public Rectangle popScissors() {
        this.flush();
        return ScissorStack.popScissors();
    }

    @CanIgnoreReturnValue
    public Renderer flush() {
        this.batch.flush();
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
        return "Renderer%s";
    }

    @CanIgnoreReturnValue
    public Renderer font(GameFont font) {
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

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer fill(int x, int y, int width, int height, Color rgb) {
        this.setColor(rgb);
        this.rect(x, y, width, height);
        return this;
    }

    @Deprecated
    @CanIgnoreReturnValue
    public Renderer box(int x, int y, int width, int height, Color rgb) {
        this.setColor(rgb);
        this.rectLine(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer fill(int x, int y, int width, int height, com.badlogic.gdx.graphics.Color rgb) {
        this.setColor(rgb);
        this.rect(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer box(int x, int y, int width, int height, com.badlogic.gdx.graphics.Color rgb) {
        this.setColor(rgb);
        this.rectLine(x, y, width, height);
        return this;
    }

    @CanIgnoreReturnValue
    public Renderer draw9PatchTexture(Texture texture, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int texWidth, int texHeight) {
        this.blit(texture, x, y, uWidth, vHeight, u, v, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x + width - uWidth, y, uWidth, vHeight, u + uWidth * 2, v, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x, y + height - vHeight, uWidth, vHeight, u, v + vHeight * 2, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x + width - uWidth, y + height - vHeight, uWidth, vHeight, u + uWidth * 2, v + vHeight * 2, uWidth, vHeight, texWidth, texHeight);
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
    public Renderer draw9PatchTexture(NamespaceID id, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int texWidth, int texHeight) {
        Texture texture = this.client.getTextureManager().getTexture(id);

        this.blit(texture, x, y + height - vHeight, uWidth, vHeight, u, v + vHeight * 2, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x + width - uWidth, y + height - vHeight, uWidth, vHeight, u + uWidth * 2, v + vHeight * 2, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x, y, uWidth, vHeight, u, v, uWidth, vHeight, texWidth, texHeight);
        this.blit(texture, x + width - uWidth, y, uWidth, vHeight, u + uWidth * 2, v, uWidth, vHeight, texWidth, texHeight);
        for (int dx = x + uWidth; dx < width - uWidth; dx += uWidth) {
            int maxX = Math.min(dx + uWidth, width - uWidth);
            int uW = maxX - dx;
            this.blit(texture, dx, y + height - vHeight, uW, vHeight, u + uWidth, v, uW, vHeight, texWidth, texHeight);
            this.blit(texture, dx, y, uW, vHeight, u + uWidth, v + vHeight * 2, uW, vHeight, texWidth, texHeight);
        }

        for (int dy = y + vHeight; dy < height - vHeight; dy += vHeight) {
            int maxX = Math.min(dy + vHeight, height - vHeight);
            int vH = maxX - dy;
            this.blit(texture, x, dy, uWidth, vH, u, v + vHeight, uWidth, vH, texWidth, texHeight);
            this.blit(texture, x + width - uWidth, dy, uWidth, vH, u + uWidth * 2, u + vHeight, uWidth, vH, texWidth, texHeight);
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
        this.enableBlend();
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
    public Renderer line(float x1, float y1, float x2, float y2, com.badlogic.gdx.graphics.Color color) {
        this.shapes.line(x1, y1, x2, y2, color, this.strokeWidth);
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
        renderFrame(NamespaceID.of("textures/gui/frame.png"), x, y, w, h, 0, 0, 4, 4, 12, 12);
    }

    public void renderPopoutFrame(int x, int y, int w, int h) {
        renderFrame(NamespaceID.of("textures/gui/popout_frame.png"), x, y, w, h, 0, 0, 4, 4, 12, 12);
    }

    public void renderFrame(@NotNull NamespaceID texture, int x, int y, int w, int h, int u, int v, int uvW, int uvH, int texWidth, int texHeight) {
        renderFrame(texture, x, y, w, h, u, v, uvW, uvH, texWidth, texHeight, RgbColor.WHITE);
    }

    public void renderFrame(@NotNull NamespaceID texture, int x, int y, int w, int h, int u, int v, int uvW, int uvH, int texWidth, int texHeight, @NotNull Color color) {
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

    public void draw9Slice(NamespaceID texture, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int inset, int texWidth, int texHeight) {
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

        this.scissorOffsetX = 0;
        this.scissorOffsetY = 0;

        this.batch.begin();

        this.glState.begin();

        this.enableBlend();

        this.iTime = System.currentTimeMillis() / 1000f;
    }

    public void end() {
        if (!this.batch.isDrawing()) {
            QuantumClient.LOGGER.warn("Batch not drawing!", new Exception());
            return;
        }

        this.glState.end();
        this.batch.end();
    }

    public void actuallyEnd() {
        if (this.batch.isDrawing()) {
            QuantumClient.LOGGER.warn("Batch still drawing");
            this.batch.end();
        }
    }

    @Language("GLSL")
    final String VERT = """
                    attribute vec4 a_position;
                    attribute vec4 a_color;
                    attribute vec2 a_texCoord0;
                    uniform mat4 u_projTrans;

                    varying vec4 vColor;
                    varying vec2 vTexCoord;

                    void main() {
                    \tvColor = a_color;
                    \tvTexCoord = a_texCoord0;
                    \tgl_Position =  u_projTrans * a_position;
                    }
                    """;

    @Language("GLSL")
    final String FRAG = """
                    // Fragment shader
                    #ifdef GL_ES
                    precision highp float;
                    #endif
                    
                    #define pi 3.14159265
                    
                    varying vec4 vColor;
                    varying vec2 vTexCoord;
                    
                    uniform sampler2D u_texture;
                    uniform vec2 iResolution;
                    uniform float iBlurRadius; // Radius of the blur
                    uniform vec2 iBlurDirection; // Direction of the blur
                    uniform vec4 iClamp;
                    
                    // Function to calculate Gaussian weights
                    float gaussian(float x, float sigma) {
                        return exp(-0.5 * (x * x) / (sigma * sigma)) / (sigma * sqrt(2.0 * pi));
                    }
                    
                    void main() {
                        float sigma = iBlurRadius;  // Sigma is usually proportional to the radius
                        vec4 color = vec4(0.0);
                        float total = 0.0;
                    
                        vec2 iPos = vTexCoord * iResolution;
                    
                        // Gaussian kernel size depends on the radius (optimize for reasonable radius)
                        for (int i = -int(iBlurRadius); i <= int(iBlurRadius); i++) {
                            float weight = gaussian(float(i), sigma);
                            vec2 offset = vec2(float(i) / (iResolution.x), float(i) / (iResolution.y)) * iBlurDirection; // Horizontal blur
                    
                            color += texture2D(u_texture, vTexCoord + offset) * weight;
                            total += weight;
                        }
                    
                        gl_FragColor = color / total;  // Normalize by total weight
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
        if (!ClientConfig.blurEnabled) {
            block.run();
            return;
        }

        if (this.blurred) {
            block.run();
            return;
        }

        this.blurred = true;
        try {
            TextureRegion fboRegion = new TextureRegion(blurTargetA.getColorBufferTexture());

            //Start rendering to an offscreen color buffer
            blurTargetA.begin();
            clearColor(0x00000000);
            clear();

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
            blurShader.setUniformf("iResolution", QuantumClient.get().getWidth(), QuantumClient.get().getHeight());
            blurShader.setUniformf("iBlurRadius", radius / guiScale);
            blurShader.setUniformf("iTime", iTime);

            //our first blur pass goes to target B
            blurTargetB.begin();
            clear();

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
            blurShader.setUniformf("iResolution", QuantumClient.get().getWidth(), QuantumClient.get().getHeight());

            //update the Y-axis blur radius
            blurShader.setUniformf("radius", radius);

            //draw target B to the screen with a vertical blur effect
            fboRegion.setTexture(blurTargetB.getColorBufferTexture());
            this.batch.setColor(1f, 1f, 1f, overlayOpacity);
            batch.draw(fboRegion, 0, 0, QuantumClient.get().getWidth(), QuantumClient.get().getHeight());

            //reset to default shader without blurs
            batch.setShader(null);

            this.flush();

            this.batch.setColor(1, 1, 1, 1);
            this.batch.setColor(1f, 1f, 1f, 1f);
        } finally {
            this.batch.setColor(1, 1, 1, 1);
            this.blurred = false;
        }
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        if (blurTargetA != null) blurTargetA.dispose();
        if (blurTargetB != null) blurTargetB.dispose();

        blurTargetA = new FrameBuffer(Format.RGBA8888, width, height, false);
        blurTargetB = new FrameBuffer(Format.RGBA8888, width, height, false);
    }

    @Override
    public void dispose() {
        vfxManager.dispose();

        if (blurTargetA != null) blurTargetA.dispose();
        if (blurTargetB != null) blurTargetB.dispose();
    }

    public int getWidth() {
        return QuantumClient.get().getWidth();
    }

    public int getHeight() {
        return QuantumClient.get().getHeight();
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

    public void scissorOffset(int x, int y) {
        this.scissorOffsetX += x;
        this.scissorOffsetY += y;
    }

    public void renderFrame(Bounds bounds) {
        this.renderFrame(bounds.pos.x, bounds.pos.y, bounds.size.width, bounds.size.height);
    }

    public void enableBlend() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(GL20.GL_ONE, GL20.GL_ONE, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void disableBlend() {
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void enableCulling() {
        this.glState.enableCulling();
        this.glState.cullFace(GL20.GL_BACK);
    }

    public void disableCulling() {
        this.glState.disableCulling();
    }

    public void enableDepth() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    public void disableDepth() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    public void enableScissor() {
        this.glState.enableScissor();
    }

    public void disableScissor() {
        this.glState.disableScissor();
    }

    public void scissor(int x, int y, int width, int height) {
        this.glState.scissor(x, y, width, height);
    }

    public void pushState() {
        this.glState.push();
    }

    public void popState() {
        this.glState.pop();
    }

    public void drawItemStack(ItemStack item, int x, int y) {
        drawItemStack(item, x, y, Anchor.CENTER);
    }

    public void drawItemStack(ItemStack item, int x, int y, Anchor anchor) {
        if (item.isEmpty()) return;
        int mx = x + anchor.getX() * 8;
        int my = y + anchor.getY() * 8;
        this.client.itemRenderer.render(item.getItem(), this, mx, my);

        if (item.getCount() > 1) {
            this.textRight((isMathDay() ? "0x" : "") + Integer.toString(item.getCount(), isMathDay() ? 16 : 10), mx + 16, my + 8);
        }
    }

    private boolean isMathDay() {
        if (this.shouldCheckMathDay + 3000 > System.currentTimeMillis())
            return false;

        this.shouldCheckMathDay = System.currentTimeMillis();
        LocalDateTime clock = LocalDateTime.now(Clock.systemUTC());
        return clock.getMonth() == Month.MARCH && clock.getDayOfMonth() == 14;
    }

    public void renderTooltip(ItemStack item, int x, int y) {
        this.renderTooltip(item, x, y, item.getFullDescription());
    }

    @Deprecated
    public void renderTooltip(ItemStack item, int x, int y, List<TextObject> description) {
        this.renderTooltip(x, y, item.getItem().getTranslation(), String.join("\n", description.stream().map(TextObject::getText).toList()), item.getItem().getId().toString());
    }

    public void renderTooltip(ItemStack item, int x, int y, String description) {
        this.renderTooltip(x, y, item.getItem().getTranslation(), description, item.getItem().getId().toString());
    }

    public void renderTooltip(int x, int y, TextObject title, String description, @Nullable String subTitle) {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        this.translate(0, 0, -TOOLTIP_ZINDEX);

        x += 8;
        y += 8;

        var all = Lists.<String>newArrayList();
        all.addFirst(title.getText());
        all.addAll(Arrays.asList(description.split("\n")));
        if (subTitle != null) all.add(subTitle);
        boolean seen = false;
        int best = 0;
        int[] arr = new int[10];
        int count = 0;
        Font font1 = this.font;
        for (String textObject : all) {
            int width = textWidth(textObject);
            if (arr.length == count) arr = Arrays.copyOf(arr, count * 2);
            arr[count++] = width;
        }
        arr = Arrays.copyOfRange(arr, 0, count);
        for (int i : arr) {
            if (!seen || i > best) {
                seen = true;
                best = i;
            }
        }
        int textWidth = seen ? best : 0;
        int descHeight = description.isBlank() ? 0 : (int) (description.lines().count() * (this.font.getLineHeight() + 3) - 3);
        int textHeight = 1 + descHeight + (int) (3 + this.font.getLineHeight());

        if (description.isEmpty() && subTitle == null) {
            textHeight -= 3;
        }
        if (subTitle != null) {
            textHeight += (int) (3 + this.font.getLineHeight());
        }

        // Shadow
        this.fill(x + 5, y + 4, textWidth + 4, 1, TRANSPARENT_BLACK);
        this.fill(x + 4, y + 5, textWidth + 6, textHeight + 4, TRANSPARENT_BLACK);
        this.fill(x + 5, y + textHeight + 9, textWidth + 4, 1, TRANSPARENT_BLACK);


        // Box
        this.fill(x + 1, y, textWidth + 4, textHeight + 6, RgbColor.rgb(0x202020));
        this.fill(x, y + 1, textWidth + 6, textHeight + 4, RgbColor.rgb(0x202020));
        this.box(x + 1, y + 1, textWidth + 4, textHeight + 4, RgbColor.rgb(0x303030));

        this.textLeft("[#ffffff][*]" + title, x + 3, y + 3, RgbColor.WHITE);

        int lineNr = 0;
        for (String line : description.lines().toList()) {
            this.textLeft("[#a0a0a0]" + line, x + 3, y + 3 + this.font.getLineHeight() + 3 + lineNr * (this.font.getLineHeight() + 3f) - 3);
            lineNr++;
        }

        if (subTitle != null)
            this.textLeft("[#606060][/]" + subTitle, x + 3, y + 3 + this.font.getLineHeight() + 3 + (description.isBlank() ? 0 : lineNr * (this.font.getLineHeight() + 3f) - 3));

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    private void drawText(@NotNull String text, float x, float y, Color color, boolean shadow) {
        int c = (color.getRed() & 0xff) << 16 | (color.getGreen() & 0xff) << 8 | (color.getBlue() & 0xff);
        Color darker = color.darker().darker();
        int cd = (darker.getRed() & 0xff) << 16 | (darker.getGreen() & 0xff) << 8 | (darker.getBlue() & 0xff);

        if (shadow) {
            batch.setColor(0.5f, 0.5f, 0.5f, 1.0f);
            if (ClientConfig.diagonalFontShadow) {
                this.drawText0(this.batch, text, x + 1, y + 1, c);
            } else {
                this.drawText0(this.batch, text, x, y + 1, c);
            }
        }

        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawText0(this.batch, text, x, y, c);
    }

    public void drawText(@NotNull TextObject text, float x, float y, Color color, boolean shadow) {
        String string = text.getText();

        int c = (color.getRed() & 0xff) << 16 | (color.getGreen() & 0xff) << 8 | (color.getBlue() & 0xff);
        Color darker = color.darker().darker();
        int cd = (darker.getRed() & 0xff) << 16 | (darker.getGreen() & 0xff) << 8 | (darker.getBlue() & 0xff);

        if (shadow) {
            batch.setColor(0.5f, 0.5f, 0.5f, 1.0f);
            if (ClientConfig.diagonalFontShadow) {
                this.drawText0(this.batch, string, x + 1, y + 1, c);
            } else {
                this.drawText0(this.batch, string, x, y + 1, c);
            }
        }

        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawText0(this.batch, string, x, y, c);
    }

    @Deprecated
    private void drawText(@NotNull FormattedText text, float x, float y, Color color, boolean shadow) {
        String string = text.getText();

        int c = (color.getRed() & 0xff) << 16 + (color.getGreen() & 0xff) << 8 + (color.getBlue() & 0xff);
        Color darker = color.darker().darker();
        int cd = (darker.getRed() & 0xff) << 16 + (darker.getGreen() & 0xff) << 8 + (darker.getBlue() & 0xff);

        if (shadow) {
            batch.setColor(0.5f, 0.5f, 0.5f, 1.0f);
            if (ClientConfig.diagonalFontShadow) {
                this.drawText0(this.batch, string, x + 1, y + 1, c);
            } else {
                this.drawText0(this.batch, string, x, y + 1, c);
            }
        }

        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawText0(this.batch, string, x, y, c);
    }

    @Deprecated
    private void drawText(@NotNull List<FormattedText> text, float x, float y, Color color, boolean shadow) {
        String string = text.stream().map(FormattedText::getText).collect(Collectors.joining(""));

        int c = (color.getRed() & 0xff) << 16 + (color.getGreen() & 0xff) << 8 + (color.getBlue() & 0xff);
        Color darker = color.darker().darker();
        int cd = (darker.getRed() & 0xff) << 16 + (darker.getGreen() & 0xff) << 8 + (darker.getBlue() & 0xff);

        if (shadow) {
            batch.setColor(0.5f, 0.5f, 0.5f, 1.0f);
            if (ClientConfig.diagonalFontShadow) {
                this.drawText0(this.batch, string, x + 1, y + 1, c);
            } else {
                this.drawText0(this.batch, string, x, y + 1, c);
            }
        }

        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawText0(this.batch, string, x, y, c);
    }

    private void drawText0(Batch batch, String string, float x, float y, int c) {
        String formatted = "[#%06x]%s".formatted(c, string);

        if (this.layoutText.equals(formatted)) {
            this.font.drawGlyphs(batch, layout, x, y);
            return;
        }
        this.layoutText = formatted;
        this.layout.clear();
        this.font.markup(formatted, this.layout);
        this.font.drawGlyphs(batch, layout, x, y);
    }
}
