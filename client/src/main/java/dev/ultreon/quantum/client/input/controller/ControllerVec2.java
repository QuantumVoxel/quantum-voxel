package dev.ultreon.quantum.client.input.controller;

import com.badlogic.gdx.math.Vector2;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.client.gui.icon.ControllerIcon;
import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;

public enum ControllerVec2 implements ControllerInterDynamic<Vector2> {
    LeftStick,
    RightStick,
    Dpad,
    Triggers,
    Unknown;

    private static final Vector2 VEC = new Vector2();

    public float getX() {
        return switch (this) {
            case LeftStick -> ControllerSignedFloat.LeftStickX.getValue();
            case RightStick -> ControllerSignedFloat.RightStickX.getValue();
            case Dpad -> ControllerSignedFloat.DpadX.getValue();
            case Triggers -> ControllerSignedFloat.LeftTrigger.getValue();
            default -> 0f;
        };
    }

    public float getY() {
        return switch (this) {
            case LeftStick -> ControllerSignedFloat.LeftStickY.getValue();
            case RightStick -> ControllerSignedFloat.RightStickY.getValue();
            case Dpad -> ControllerSignedFloat.DpadY.getValue();
            case Triggers -> ControllerSignedFloat.RightTrigger.getValue();
            default -> 0f;
        };
    }

    @Deprecated
    public Vector2 get() {
        return new Vector2(getX(), getY());
    }

    public Vector2 get(Vector2 out) {
        return out.set(getX(), getY());
    }

    /**
     * A value between 0 and 1 that represents the magnitude of the vector
     *
     * @return the magnitude
     */
    public float getMagnitude() {
        return get(VEC).len();
    }

    @Override
    public ControllerBoolean asBoolean() {
        return switch (this) {
            case LeftStick -> ControllerBoolean.LeftStickAny;
            case RightStick -> ControllerBoolean.RightStickAny;
            case Dpad -> ControllerBoolean.AnyDpad;
            case Triggers -> ControllerBoolean.AnyTrigger;
            default -> ControllerBoolean.Unknown;
        };
    }

    @Override
    public ControllerSignedFloat asSignedFloat() {
        return switch (this) {
            case LeftStick -> ControllerSignedFloat.LeftStickMagnitude;
            case RightStick -> ControllerSignedFloat.RightStickMagnitude;
            case Dpad -> ControllerSignedFloat.DpadMagnitude;
            case Triggers -> ControllerSignedFloat.TriggerMagnitude;
            default -> ControllerSignedFloat.Unknown;
        };
    }

    @Override
    public ControllerUnsignedFloat asUnsignedFloat() {
        return switch (this) {
            case LeftStick -> ControllerUnsignedFloat.LeftStickMagnitude;
            case RightStick -> ControllerUnsignedFloat.RightStickMagnitude;
            case Dpad -> ControllerUnsignedFloat.DpadMagnitude;
            case Triggers -> ControllerUnsignedFloat.TriggerMagnitude;
            default -> ControllerUnsignedFloat.Unknown;
        };
    }

    @Override
    public Pair<ControllerSignedFloat, Float> asSignedFloat(Vector2 value) {
        return new Pair<>(asSignedFloat(), value.x != 0 || value.y != 0 ? 1f : 0f);
    }

    @Override
    public Pair<ControllerUnsignedFloat, Float> asUnsignedFloat(Vector2 value) {
        return new Pair<>(asUnsignedFloat(), value.x != 0 || value.y != 0 ? 1f : 0f);
    }

    @Override
    public Pair<ControllerVec2, Vector2> asVec2(Vector2 value, Vector2 result) {
        return new Pair<>(asVec2(), result.set(getX(), getY()));
    }

    @Override
    public ControllerVec2 asVec2() {
        return this;
    }

    @Override
    public Pair<ControllerBoolean, Boolean> asBoolean(Vector2 value) {
        return new Pair<>(asBoolean(), value.x != 0 || value.y != 0);
    }



    @Override
    public ControllerIcon getIcon() {
        return switch (this) {
            case LeftStick -> ControllerIcon.LeftJoyStickMove;
            case RightStick -> ControllerIcon.RightJoyStickMove;
            case Dpad -> ControllerIcon.DpadAny;
            case Triggers -> ControllerIcon.LeftTrigger;
            default -> ControllerIcon.AnyJoyStick;
        };
    }
}
