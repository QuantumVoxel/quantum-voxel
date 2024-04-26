package com.ultreon.quantum.client.particle;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.utils.Pool;

public class PFXPool extends Pool<ParticleEffect> {
	private final ParticleEffect sourceEffect;

	public PFXPool(ParticleEffect sourceEffect) {
		this.sourceEffect = sourceEffect;
	}

	@Override
	public void free(ParticleEffect pfx) {
		pfx.reset();
		super.free(pfx);
	}

	@Override
	protected ParticleEffect newObject() {
		return sourceEffect.copy();
	}
}
