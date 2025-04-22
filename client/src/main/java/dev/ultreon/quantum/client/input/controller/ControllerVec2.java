//package dev.ultreon.quantum.client.input.controller;
//
//import com.badlogic.gdx.math.Vector2;
//import dev.ultreon.libs.commons.v0.tuple.Pair;
//import dev.ultreon.quantum.client.gui.icon.ControllerIcon;
//import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;
//
//public enum ControllerVec2 implements ControllerInterDynamic<Vector2> {
//    LeftStick,
//    RightStick,
//    Dpad,
//    Triggers,
//    Unknown;
//
//    private static final Vector2 VEC = new Vector2();
//
//    public float getX() {
//        switch (this) {
//            case LeftStick:
//                return ControllerSignedFloat.LeftStickX.getValue();
//            case RightStick:
//                return ControllerSignedFloat.RightStickX.getValue();
//            case Dpad:
//                return ControllerSignedFloat.DpadX.getValue();
//            case Triggers:
//                return ControllerSignedFloat.LeftTrigger.getValue();
//            default:
//                return 0f;
//        }
//    }
//
//    public float getY() {
//        switch (this) {
//            case LeftStick:
//                return ControllerSignedFloat.LeftStickY.getValue();
//            case RightStick:
//                return ControllerSignedFloat.RightStickY.getValue();
//            case Dpad:
//                return ControllerSignedFloat.DpadY.getValue();
//            case Triggers:
//                return ControllerSignedFloat.RightTrigger.getValue();
//            default:
//                return 0f;
//        }
//    }
//
//    @Deprecated
//    public Vector2 get() {
//        return new Vector2(getX(), getY());
//    }
//
//    public Vector2 get(Vector2 out) {
//        return out.set(getX(), getY());
//    }
//
//    /**
//     * A value between 0 and 1 that represents the magnitude of the vector
//     *
//     * @return the magnitude
//     */
//    public float getMagnitude() {
//        return get(VEC).len();
//    }
//
//    @Override
//    public ControllerBoolean asBoolean() {
//        switch (this) {
//            case LeftStick:
//                return ControllerBoolean.LeftStickAny;
//            case RightStick:
//                return ControllerBoolean.RightStickAny;
//            case Dpad:
//                return ControllerBoolean.AnyDpad;
//            case Triggers:
//                return ControllerBoolean.AnyTrigger;
//            default:
//                return ControllerBoolean.Unknown;
//        }
//    }
//
//    @Override
//    public ControllerSignedFloat asSignedFloat() {
//        switch (this) {
//            case LeftStick:
//                return ControllerSignedFloat.LeftStickMagnitude;
//            case RightStick:
//                return ControllerSignedFloat.RightStickMagnitude;
//            case Dpad:
//                return ControllerSignedFloat.DpadMagnitude;
//            case Triggers:
//                return ControllerSignedFloat.TriggerMagnitude;
//            default:
//                return ControllerSignedFloat.Unknown;
//        }
//    }
//
//    @Override
//    public ControllerUnsignedFloat asUnsignedFloat() {
//        switch (this) {
//            case LeftStick:
//                return ControllerUnsignedFloat.LeftStickMagnitude;
//            case RightStick:
//                return ControllerUnsignedFloat.RightStickMagnitude;
//            case Dpad:
//                return ControllerUnsignedFloat.DpadMagnitude;
//            case Triggers:
//                return ControllerUnsignedFloat.TriggerMagnitude;
//            default:
//                return ControllerUnsignedFloat.Unknown;
//        }
//    }
//
//    @Override
//    public Pair<ControllerSignedFloat, Float> asSignedFloat(Vector2 value) {
//        return new Pair<>(asSignedFloat(), value.x != 0 || value.y != 0 ? 1f : 0f);
//    }
//
//    @Override
//    public Pair<ControllerUnsignedFloat, Float> asUnsignedFloat(Vector2 value) {
//        return new Pair<>(asUnsignedFloat(), value.x != 0 || value.y != 0 ? 1f : 0f);
//    }
//
//    @Override
//    public Pair<ControllerVec2, Vector2> asVec2(Vector2 value, Vector2 result) {
//        return new Pair<>(asVec2(), result.set(getX(), getY()));
//    }
//
//    @Override
//    public ControllerVec2 asVec2() {
//        return this;
//    }
//
//    @Override
//    public Pair<ControllerBoolean, Boolean> asBoolean(Vector2 value) {
//        return new Pair<>(asBoolean(), value.x != 0 || value.y != 0);
//    }
//
//
//
//    @Override
//    public ControllerIcon getIcon() {
//        switch (this) {
//            case LeftStick:
//                return ControllerIcon.LeftJoyStickMove;
//            case RightStick:
//                return ControllerIcon.RightJoyStickMove;
//            case Dpad:
//                return ControllerIcon.DpadAny;
//            case Triggers:
//                return ControllerIcon.LeftTrigger;
//            default:
//                return ControllerIcon.AnyJoyStick;
//        }
//    }
//}
