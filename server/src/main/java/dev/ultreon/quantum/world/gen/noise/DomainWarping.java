package dev.ultreon.quantum.world.gen.noise;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.util.Vec2d;
import dev.ultreon.quantum.util.Vec2i;
import dev.ultreon.quantum.util.MathHelper;

public class DomainWarping implements Disposable {
    public final NoiseInstance domainX;
    public final NoiseInstance domainY;
    public int amplitudeX = 20, amplitudeY = 20;

    public DomainWarping(NoiseInstance domainX, NoiseInstance domainY) {
        this.domainX = domainX;
        this.domainY = domainY;
    }

    public DomainWarping(long seed, NoiseConfig settingsX, NoiseConfig settingsZ) {
        this.domainX = settingsX.create(seed);
        this.domainY = settingsZ.create(seed);
    }

    public double generateDomainNoise(int x, int z, NoiseInstance defaultNoiseSettings) {
        Vec2d domainOffset = this.generateDomainOffset(x, z);
        return NoiseUtils.octavePerlin(x + domainOffset.x, z + domainOffset.y, this.domainX);
    }

    public Vec2d generateDomainOffset(int x, int z) {
        double noiseX = NoiseUtils.octavePerlin(x, z, this.domainX) * this.amplitudeX;
        double noiseY = NoiseUtils.octavePerlin(x, z, this.domainY) * this.amplitudeY;
        return new Vec2d(noiseX, noiseY);
    }

    public Vec2i generateDomainOffsetInt(int x, int z) {
        return MathHelper.round(this.generateDomainOffset(x, z));
    }

    @Override
    public void dispose() {
        this.domainX.dispose();
        this.domainY.dispose();
    }
}
