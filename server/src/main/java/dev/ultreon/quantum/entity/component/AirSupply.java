package dev.ultreon.quantum.entity.component;

import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.component.GameComponent;

public class AirSupply extends GameComponent {
    private float air;
    private int maxAir;

    public AirSupply(float air, int maxAir) {
        this.setAir(air);
        this.setMaxAir(maxAir);
    }

    public AirSupply(AirSupply air) {
        this(air.getAir(), air.getMaxAir());
    }

    public AirSupply() {
        this(0, 0);
    }

    public AirSupply(int maxAir) {
        this(maxAir, maxAir)        ;
    }

    public AirSupply set(AirSupply airSupply) {
        this.setAir(airSupply.getAir());
        this.setMaxAir(airSupply.getMaxAir());
        return this;
    }

    public AirSupply set(float air, int maxAir) {
        this.setAir(air);
        this.setMaxAir(maxAir);
        return this;
    }

    public AirSupply useAir(float amount) {
        this.setAir(Mth.clamp(this.getAir() - amount, 0, this.getMaxAir()));
        return this;
    }

    public AirSupply fillAir(float amount) {
        this.setAir(Mth.clamp(this.getAir() + amount, 0, this.getMaxAir()));
        return this;
    }

    public boolean isSuffocating() {
        return getAir() <= 0;
    }

    public float getAir() {
        return air;
    }

    public void setAir(float air) {
        this.air = air;
    }

    public int getMaxAir() {
        return maxAir;
    }

    public void setMaxAir(int maxAir) {
        this.maxAir = maxAir;
    }
}
