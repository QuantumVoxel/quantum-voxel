package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import dev.ultreon.quantum.client.render.meshing.GreedyMesher;
import dev.ultreon.quantum.client.render.meshing.GreedyMesher.LightLevelData;
import dev.ultreon.quantum.client.render.meshing.PerCornerLightData;
import dev.ultreon.quantum.client.world.AOUtils;
import dev.ultreon.quantum.util.Color;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.Direction;

/**
 * NormalBlockRenderer is responsible for rendering the six faces of a block in a 3D environment.
 * This class provides methods to render the north, south, west, east, top, and bottom faces of a block,
 * taking into account the texture region and lighting data provided.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class NormalBlockRenderer implements BlockRenderer {
    public static final NormalBlockRenderer INSTANCE = new NormalBlockRenderer();
    private static final float TEXTURE_PERCENTAGE = 16f / 2048f;
    private static final int BLOCKS_PER_WIDTH = 2048 / 16;

    private final ThreadLocal<VertexInfo> c00 = ThreadLocal.withInitial(VertexInfo::new);
    private final ThreadLocal<VertexInfo> c01 = ThreadLocal.withInitial(VertexInfo::new);
    private final ThreadLocal<VertexInfo> c10 = ThreadLocal.withInitial(VertexInfo::new);
    private final ThreadLocal<VertexInfo> c11 = ThreadLocal.withInitial(VertexInfo::new);

    @Override
    public void renderNorth(TextureRegion region, float x1, float y1, float x2, float y2, float z, LightLevelData lld, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder) {
        var lightLevel = lld.blockBrightness();

        if (region == null) return;

        // POSITIVE Z
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        VertexInfo c00 = this.c00.get();
        VertexInfo c01 = this.c01.get();
        VertexInfo c10 = this.c10.get();
        VertexInfo c11 = this.c11.get();
        c00.setPos(x1, y1, z).setNor(0, 0, 1).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c01.setPos(x1, y2, z).setNor(0, 0, 1).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c10.setPos(x2, y1, z).setNor(0, 0, 1).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c11.setPos(x2, y2, z).setNor(0, 0, 1).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));

        Color c = RgbColor.rgba(lld.blockBrightness(), lld.blockBrightness(), lld.blockBrightness(), lld.sunBrightness());
        if (lightData == null) {
            c00.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c01.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c10.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c11.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
        } else {
            c00.setCol(lightData.l00, lightData.l00, lightData.l00, lightData.s00);
            c01.setCol(lightData.l01, lightData.l01, lightData.l01, lightData.s01);
            c10.setCol(lightData.l10, lightData.l10, lightData.l10, lightData.s10);
            c11.setCol(lightData.l11, lightData.l11, lightData.l11, lightData.s11);
        }

        applyAo(c00, c01, c10, c11, AOUtils.aoForSide(ao, Direction.NORTH));
        builder.rect(c00, c10, c11, c01);
    }

    @Override
    public void renderSouth(TextureRegion region, float x1, float y1, float x2, float y2, float z, LightLevelData lld, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder) {
        var lightLevel = lld.blockBrightness();

        if (region == null) return;

        // NEGATIVE Z
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        VertexInfo c00 = this.c00.get();
        VertexInfo c01 = this.c01.get();
        VertexInfo c10 = this.c10.get();
        VertexInfo c11 = this.c11.get();
        c00.setPos(x1, y1, z).setNor(0, 0, -1).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c01.setPos(x1, y2, z).setNor(0, 0, -1).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c10.setPos(x2, y1, z).setNor(0, 0, -1).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c11.setPos(x2, y2, z).setNor(0, 0, -1).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));

        Color c = RgbColor.rgba(lld.blockBrightness(), lld.blockBrightness(), lld.blockBrightness(), lld.sunBrightness());
        if (lightData == null) {
            c00.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c01.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c10.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c11.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
        } else {
            c00.setCol(lightData.l00, lightData.l00, lightData.l00, lightData.s00);
            c01.setCol(lightData.l01, lightData.l01, lightData.l01, lightData.s01);
            c10.setCol(lightData.l10, lightData.l10, lightData.l10, lightData.s10);
            c11.setCol(lightData.l11, lightData.l11, lightData.l11, lightData.s11);
        }

        applyAo(c00, c01, c10, c11, AOUtils.aoForSide(ao, Direction.SOUTH));
        builder.rect(c01, c11, c10, c00);
    }

    @Override
    public void renderWest(TextureRegion region, float z1, float y1, float z2, float y2, float x, LightLevelData lld, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder) {
        var lightLevel = lld.blockBrightness();

        if (region == null) return;

        // NEGATIVE X
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        VertexInfo c00 = this.c00.get();
        VertexInfo c01 = this.c01.get();
        VertexInfo c10 = this.c10.get();
        VertexInfo c11 = this.c11.get();
        c00.setPos(x, y1, z1).setNor(-1, 0, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c01.setPos(x, y1, z2).setNor(-1, 0, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c10.setPos(x, y2, z1).setNor(-1, 0, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c11.setPos(x, y2, z2).setNor(-1, 0, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));

        Color c = RgbColor.rgba(lld.blockBrightness(), lld.blockBrightness(), lld.blockBrightness(), lld.sunBrightness());
        if (lightData == null) {
            c00.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c01.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c10.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c11.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
        } else {
            c00.setCol(lightData.l00, lightData.l00, lightData.l00, lightData.s00);
            c01.setCol(lightData.l01, lightData.l01, lightData.l01, lightData.s01);
            c10.setCol(lightData.l10, lightData.l10, lightData.l10, lightData.s10);
            c11.setCol(lightData.l11, lightData.l11, lightData.l11, lightData.s11);
        }

        applyAo(c00, c01, c10, c11, AOUtils.aoForSide(ao, Direction.WEST));
        builder.rect(c01, c11, c10, c00);
    }

    @Override
    public void renderEast(TextureRegion region, float z1, float y1, float z2, float y2, float x, LightLevelData lld, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder) {
        var lightLevel = lld.blockBrightness();

        if (region == null) return;

        // POSITIVE X
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        VertexInfo c00 = this.c00.get();
        VertexInfo c01 = this.c01.get();
        VertexInfo c10 = this.c10.get();
        VertexInfo c11 = this.c11.get();
        c00.setPos(x, y1, z1).setNor(1, 0, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c01.setPos(x, y1, z2).setNor(1, 0, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c10.setPos(x, y2, z1).setNor(1, 0, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c11.setPos(x, y2, z2).setNor(1, 0, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));

        Color c = RgbColor.rgba(lld.blockBrightness(), lld.blockBrightness(), lld.blockBrightness(), lld.sunBrightness());
        if (lightData == null) {
            c00.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c01.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c10.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c11.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
        } else {
            c00.setCol(lightData.l00, lightData.l00, lightData.l00, lightData.s00);
            c01.setCol(lightData.l01, lightData.l01, lightData.l01, lightData.s01);
            c10.setCol(lightData.l10, lightData.l10, lightData.l10, lightData.s10);
            c11.setCol(lightData.l11, lightData.l11, lightData.l11, lightData.s11);
        }

        applyAo(c00, c01, c10, c11, AOUtils.aoForSide(ao, Direction.EAST));
        builder.rect(c00, c10, c11, c01);
    }

    @Override
    public void renderTop(TextureRegion region, float x1, float z1, float x2, float z2, float y, LightLevelData lld, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder) {
        var lightLevel = lld.blockBrightness();

        if (region == null) return;

        // POSITIVE Y
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        VertexInfo c00 = this.c00.get();
        VertexInfo c01 = this.c01.get();
        VertexInfo c10 = this.c10.get();
        VertexInfo c11 = this.c11.get();
        c00.setPos(x1, y, z1).setNor(0, 1, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c01.setPos(x1, y, z2).setNor(0, 1, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c10.setPos(x2, y, z1).setNor(0, 1, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c11.setPos(x2, y, z2).setNor(0, 1, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));

        if (lightData == null) {
            c00.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c01.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c10.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c11.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
        } else {
            c00.setCol(lightData.l00, lightData.l00, lightData.l00, lightData.s00);
            c01.setCol(lightData.l01, lightData.l01, lightData.l01, lightData.s01);
            c10.setCol(lightData.l10, lightData.l10, lightData.l10, lightData.s10);
            c11.setCol(lightData.l11, lightData.l11, lightData.l11, lightData.s11);
        }

        applyAo(c00, c01, c10, c11, AOUtils.aoForSide(ao, Direction.UP));
        builder.rect(c01, c11, c10, c00);
    }

    @Override
    public void renderBottom(TextureRegion region, float x1, float z1, float x2, float z2, float y, LightLevelData lld, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder) {
        var lightLevel = lld.blockBrightness();

        if (region == null) return;

        // NEGATIVE Y
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        VertexInfo c00 = this.c00.get();
        VertexInfo c01 = this.c01.get();
        VertexInfo c10 = this.c10.get();
        VertexInfo c11 = this.c11.get();
        c00.setPos(x1, y, z1).setNor(0, -1, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c01.setPos(x1, y, z2).setNor(0, -1, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c10.setPos(x2, y, z1).setNor(0, -1, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));
        c11.setPos(x2, y, z2).setNor(0, -1, 0).setUV(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region));

        Color c = RgbColor.rgba(lld.blockBrightness(), lld.blockBrightness(), lld.blockBrightness(), lld.sunBrightness());
        if (lightData == null) {
            c00.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c01.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c10.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
            c11.setCol(lightLevel, lightLevel, lightLevel, lightLevel);
        } else {
            c00.setCol(lightData.l00, lightData.l00, lightData.l00, lightData.s00);
            c01.setCol(lightData.l01, lightData.l01, lightData.l01, lightData.s01);
            c10.setCol(lightData.l10, lightData.l10, lightData.l10, lightData.s10);
            c11.setCol(lightData.l11, lightData.l11, lightData.l11, lightData.s11);
        }

        applyAo(c00, c01, c10, c11, AOUtils.aoForSide(ao, Direction.DOWN));
        builder.rect(c00, c10, c11, c01);
    }

    /**
     * Applies the AO (Ambient Occlusion) to the vertices.
     * 
     * @param c00 The first vertex.
     * @param c01 The second vertex.
     * @param c10 The third vertex.
     * @param c11 The fourth vertex.
     * @param ao The AO value.
     */
    private void applyAo(VertexInfo c00, VertexInfo c01, VertexInfo c10, VertexInfo c11, int ao) {
        getMul(c00, AOUtils.hasAoCorner00(ao));
        getMul(c01, AOUtils.hasAoCorner01(ao));
        getMul(c10, AOUtils.hasAoCorner10(ao));
        getMul(c11, AOUtils.hasAoCorner11(ao));
    }

    /**
     * Gets the multiplier for the vertex.
     * 
     * @param c00 The vertex.
     * @param ao The AO value.
     */
    private static void getMul(VertexInfo c00, boolean ao) {
        c00.color.add(0.2f, 0.2f, 0.2f, 0.0f).mul((float) (ao ? 0.5 : 1.0), (float) (ao ? 0.5 : 1.0), (float) (ao ? 0.5 : 1.0), 1.0f);
    }

    /**
     * Gets the color for the vertex.
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     * @return The color.
     */
    protected Color getColor(int x, int y, int z) {
        return RgbColor.WHITE;
    }

    /**
     * Initializes the renderer.
     */
    private static void initialize() {
    }

    /**
     * Gets the U coordinate for the texture.
     * 
     * @param region The texture region.
     * @return The U coordinate.
     */
    public static float getU(TextureRegion region) {
        return region.getU() / (1 + NormalBlockRenderer.TEXTURE_PERCENTAGE);
    }

    /**
     * Gets the V coordinate for the texture.
     * 
     * @param region The texture region.
     * @return The V coordinate.
     */
    public static float getV(TextureRegion region) {
        return region.getV();
    }

    /**
     * Gets the U2 coordinate for the texture.
     * 
     * @param region The texture region.
     * @return The U2 coordinate.
     */
    public static float getU2(TextureRegion region) {
        return region.getU2() / (1 + NormalBlockRenderer.TEXTURE_PERCENTAGE);
    }

    /**
     * Gets the V2 coordinate for the texture.
     * 
     * @param region The texture region.
     * @return The V2 coordinate.
     */
    public static float getV2(TextureRegion region) {
        return region.getV2();
    }
}