package dev.ultreon.quantum.client.render.meshing;

public class PerCornerLightData {
    public float l00, l01, l10, l11;
    public float s00, s01, s10, s11;

    public PerCornerLightData() {

    }

    public PerCornerLightData(float l) {
        this(l, l, l, l);
    }

    public PerCornerLightData(float l, float s) {
        this(l, l, l, l, s, s, s, s);
    }

    public PerCornerLightData(
            float l00, float l01, float l10, float l11
    ) {
        this.l00 = l00;
        this.l01 = l01;
        this.l10 = l10;
        this.l11 = l11;
    }

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
