package dev.ultreon.quantum.entity.component;

import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.component.GameComponent;

public class AirSupply extends GameComponent {
    public float air;
    public int maxAir;

    public AirSupply(float air, int maxAir) {
        this.air = air;
        this.maxAir = maxAir;
    }

    public AirSupply(AirSupply air) {
        this(air.air, air.maxAir);
    }

    public AirSupply() {
        this(0, 0);
    }

    public AirSupply(int maxAir) {
        this(maxAir, maxAir)        ;
    }

    public AirSupply set(AirSupply airSupply) {
        this.air = airSupply.air;
        this.maxAir = airSupply.maxAir;
        return this;
    }

    public AirSupply set(float air, int maxAir) {
        this.air = air;
        this.maxAir = maxAir;
        return this;
    }

    public AirSupply useAir(float amount) {
        this.air = Mth.clamp(this.air - amount, 0, this.maxAir);
        return this;
    }

    public AirSupply fillAir(float amount) {
        this.air = Mth.clamp(this.air + amount, 0, this.maxAir);
        return this;
    }

    public boolean isSuffocating() {
        return air <= 0;
    }
}
