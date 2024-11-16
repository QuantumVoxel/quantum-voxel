package dev.ultreon.quantum.client.render.meshing;

/**
 * Represents lighting data for corners of a block.
 * This data includes light values and shading values at each of the four corners.
 */
public class PerCornerLightData {
    public static final PerCornerLightData EMPTY = new PerCornerLightData(0, 0, 0, 0, 1, 1, 1, 1);
    public float l00, l01, l10, l11;
    public float s00, s01, s10, s11;

    /**
     * Default constructor for PerCornerLightData.
     * Initializes a new instance of PerCornerLightData with default light and shading values.
     */
    public PerCornerLightData() {

    }

    /**
     * Constructs a new PerCornerLightData object with the same light value for all four corners.
     *
     * @param l The light value to be applied to all four corners.
     */
    public PerCornerLightData(float l) {
        this(l, l, l, l);
    }

    /**
     * Constructs PerCornerLightData with uniform light and shading values for all four corners.
     *
     * @param l Light intensity to be used for all four corners.
     * @param s Shading intensity to be used for all four corners.
     */
    public PerCornerLightData(float l, float s) {
        this(l, l, l, l, s, s, s, s);
    }

    /**
     * Creates an instance of PerCornerLightData with specified light values for each corner.
     *
     * @param l00 Light intensity at the corner (0, 0).
     * @param l01 Light intensity at the corner (0, 1).
     * @param l10 Light intensity at the corner (1, 0).
     * @param l11 Light intensity at the corner (1, 1).
     */
    public PerCornerLightData(
            float l00, float l01, float l10, float l11
    ) {
        this.l00 = l00;
        this.l01 = l01;
        this.l10 = l10;
        this.l11 = l11;
    }

    /**
     * Constructs a PerCornerLightData instance with specified light and sunlight values for each corner.
     *
     * @param l00 Light value at corner (0,0)
     * @param l01 Light value at corner (0,1)
     * @param l10 Light value at corner (1,0)
     * @param l11 Light value at corner (1,1)
     * @param s00 Sunlight value at corner (0,0)
     * @param s01 Sunlight value at corner (0,1)
     * @param s10 Sunlight value at corner (1,0)
     * @param s11 Sunlight value at corner (1,1)
     */
    public PerCornerLightData(
            float l00, float l01, float l10, float l11,
            float s00, float s01, float s10, float s11
    ) {
        this.l00 = l00;
        this.l01 = l01;
        this.l10 = l10;
        this.l11 = l11;
        this.s00 = s00;
        this.s01 = s01;
        this.s10 = s10;
        this.s11 = s11;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(o == this)
            return true;
        if(o instanceof PerCornerLightData p) {
            return p.l10 == this.l10 && p.l11 == this.l11 && p.l00 == this.l00 && p.l01 == this.l01 &&
                   p.s10 == this.s10 && p.s11 == this.s11 && p.s00 == this.s00 && p.s01 == this.s01;
        }
        return false;
    }
}
