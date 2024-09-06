package dev.ultreon.quantum.client.input.dyn;

import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.client.input.controller.ControllerSignedFloat;

public interface SignedFloatConvertible<T> {
    Pair<ControllerSignedFloat, Float> asSignedFloat(T value);
}
