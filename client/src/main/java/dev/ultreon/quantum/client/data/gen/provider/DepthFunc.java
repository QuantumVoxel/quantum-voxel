package dev.ultreon.quantum.client.data.gen.provider;

import com.badlogic.gdx.graphics.GL20;

import java.util.Objects;
import java.util.function.BiPredicate;

public enum DepthFunc {
    LESS(GL20.GL_LESS, (a, b) -> a < b),
    LEQUAL(GL20.GL_LEQUAL, (a, b) -> a <= b),
    GREATER(GL20.GL_GREATER, (a, b) -> a > b),
    GEQUAL(GL20.GL_GEQUAL, (a, b) -> a >= b),
    EQUAL(GL20.GL_EQUAL, Objects::equals),
    NOTEQUAL(GL20.GL_NOTEQUAL, (a, b) -> !Objects.equals(a, b)),
    ALWAYS(GL20.GL_ALWAYS, (a, b) -> true),
    NEVER(GL20.GL_NEVER, (a, b) -> false);

    private final int glFunc;
    private final BiPredicate<Float, Float> predicate;

    DepthFunc(int glFunc, BiPredicate<Float, Float> predicate) {
        this.glFunc = glFunc;
        this.predicate = predicate;
    }

    public int getGlFunc() {
        return glFunc;
    }

    public BiPredicate<Float, Float> getPredicate() {
        return predicate;
    }
}
