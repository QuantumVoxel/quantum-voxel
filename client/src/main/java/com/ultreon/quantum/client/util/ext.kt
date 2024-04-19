package com.ultreon.quantum.client.util

import com.badlogic.gdx.math.MathUtils

data class Rot(val radians: Float) {
    val degrees: Float
        get() = radians * MathUtils.radDeg
}

fun Number.deg(): Rot {
    return Rot(this.toFloat() * MathUtils.degRad)
}

fun Number.rad(): Rot {
    return Rot(this.toFloat())
}
