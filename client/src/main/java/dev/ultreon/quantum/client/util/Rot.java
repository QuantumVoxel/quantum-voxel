package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.math.MathUtils;

public record Rot(float radians) {
	public float getDegrees() {
		return radians * MathUtils.radDeg;
	}
	
	public static Rot deg(Number num) {
		return new Rot(num.floatValue() * MathUtils.degRad);
	}
	
	public static Rot rad(Number num) {
		return new Rot(num.floatValue());
	}
}
