//package dev.ultreon.quantum.client.input.controller;
//
//import com.badlogic.gdx.math.Vector2;
//import dev.ultreon.libs.commons.v0.tuple.Pair;
//import dev.ultreon.quantum.client.QuantumClient;
//import dev.ultreon.quantum.client.gui.icon.ControllerIcon;
//import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;
//import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerAxis;
//import org.intellij.lang.annotations.MagicConstant;
//
//public enum ControllerSignedFloat implements ControllerInterDynamic<Float> {
//    TriggerMagnitude,
//    LeftStickMagnitude,
//    RightStickMagnitude,
//    DpadMagnitude,
//    LeftTrigger,
//    RightTrigger,
//    Triggers,
//    Shoulders,
//    LeftStickX,
//    LeftStickY,
//    RightStickX,
//    RightStickY,
//    DpadX,
//    DpadY,
//    Unknown;
//
//    public float getValue() {
//        switch (this) {
//            case LeftTrigger:
//                return QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftTrigger) * 2 - 1;
//            case RightTrigger:
//                return QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightTrigger) * 2 - 1;
//            case Triggers:
//                return QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftTrigger) - QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightTrigger);
//            case Shoulders:
//                return (QuantumClient.get().controllerInput.isButtonPressed(ControllerBoolean.LeftShoulder) ? -1 : 0) + (QuantumClient.get().controllerInput.isButtonPressed(ControllerBoolean.RightShoulder) ? 1 : 0);
//            case LeftStickX:
//                return QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftStickX);
//            case LeftStickY:
//                return QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftStickY);
//            case RightStickX:
//                return QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightStickX);
//            case RightStickY:
//                return QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightStickY);
//            case DpadX:
//                return of(ControllerBoolean.DpadLeft, ControllerBoolean.DpadRight);
//            case DpadY:
//                return of(ControllerBoolean.DpadDown, ControllerBoolean.DpadUp);
//            case LeftStickMagnitude:
//                return ControllerVec2.LeftStick.getMagnitude() * 2 - 1;
//            case RightStickMagnitude:
//                return ControllerVec2.RightStick.getMagnitude() * 2 - 1;
//            case DpadMagnitude:
//                return ControllerVec2.Dpad.getMagnitude() * 2 - 1;
//            case TriggerMagnitude:
//                return ControllerVec2.Triggers.getMagnitude() * 2 - 1;
//            default:
//                return 0;
//        }
//    }
//
//    private float of(ControllerBoolean a, ControllerBoolean b) {
//        float v = 0;
//        if (a.getValue()) v--;
//        if (b.getValue()) v++;
//        return v;
//    }
//
//    @Override
//    public ControllerBoolean asBoolean() {
//        switch (this) {
//            case LeftTrigger:
//                return ControllerBoolean.LeftTrigger;
//            case RightTrigger:
//                return ControllerBoolean.RightTrigger;
//            case Triggers:
//            case TriggerMagnitude:
//                return ControllerBoolean.AnyTrigger;
//            case Shoulders:
//                return ControllerBoolean.AnyShoulder;
//            case LeftStickX:
//                return ControllerBoolean.LeftStickLeft;
//            case LeftStickY:
//                return ControllerBoolean.LeftStickUp;
//            case RightStickX:
//                return ControllerBoolean.RightStickLeft;
//            case RightStickY:
//                return ControllerBoolean.RightStickUp;
//            case DpadX:
//                return ControllerBoolean.DpadX;
//            case DpadY:
//                return ControllerBoolean.DpadY;
//            case LeftStickMagnitude:
//                return ControllerBoolean.LeftStickUsed;
//            case RightStickMagnitude:
//                return ControllerBoolean.RightStickUsed;
//            case DpadMagnitude:
//                return ControllerBoolean.DpadUsed;
//            default:
//                return ControllerBoolean.Unknown;
//        }
//    }
//
//    @Override
//    public Pair<ControllerBoolean, Boolean> asBoolean(Float value) {
//        switch (this) {
//            case LeftTrigger:
//                return new Pair<>(ControllerBoolean.LeftTrigger, value > 0);
//            case RightTrigger:
//                return new Pair<>(ControllerBoolean.RightTrigger, value > 0);
//            case LeftStickX:
//                return new Pair<>(ControllerBoolean.LeftStickLeft, value < 0);
//            case LeftStickY:
//                return new Pair<>(ControllerBoolean.LeftStickUp, value > 0);
//            case RightStickX:
//                return new Pair<>(ControllerBoolean.RightStickLeft, value < 0);
//            case RightStickY:
//                return new Pair<>(ControllerBoolean.RightStickUp, value > 0);
//            case DpadX:
//                return new Pair<>(ControllerBoolean.DpadX, value > 0);
//            case DpadY:
//                return new Pair<>(ControllerBoolean.DpadY, value > 0);
//            case LeftStickMagnitude:
//                return new Pair<>(ControllerBoolean.LeftStickUsed, value > 0);
//            case RightStickMagnitude:
//                return new Pair<>(ControllerBoolean.RightStickUsed, value > 0);
//            case DpadMagnitude:
//                return new Pair<>(ControllerBoolean.DpadUsed, value > 0);
//            case TriggerMagnitude:
//                return new Pair<>(ControllerBoolean.AnyTrigger, value > 0);
//            default:
//                return new Pair<>(ControllerBoolean.Unknown, false);
//        }
//    }
//
//    @Override
//    public ControllerUnsignedFloat asUnsignedFloat() {
//        switch (this) {
//            case LeftStickX:
//                return ControllerUnsignedFloat.LeftStickX;
//            case LeftStickY:
//                return ControllerUnsignedFloat.LeftStickY;
//            case RightStickX:
//                return ControllerUnsignedFloat.RightStickX;
//            case RightStickY:
//                return ControllerUnsignedFloat.RightStickY;
//            case LeftStickMagnitude:
//                return ControllerUnsignedFloat.LeftStickMagnitude;
//            case RightStickMagnitude:
//                return ControllerUnsignedFloat.RightStickMagnitude;
//            case DpadX:
//                return ControllerUnsignedFloat.DpadX;
//            case DpadY:
//                return ControllerUnsignedFloat.DpadY;
//            case DpadMagnitude:
//                return ControllerUnsignedFloat.DpadMagnitude;
//            case TriggerMagnitude:
//                return ControllerUnsignedFloat.TriggerMagnitude;
//            default:
//                return ControllerUnsignedFloat.Unknown;
//        }
//    }
//
//    @Override
//    public ControllerSignedFloat asSignedFloat() {
//        return this;
//    }
//
//    @Override
//    public Pair<ControllerUnsignedFloat, Float> asUnsignedFloat(Float value) {
//        return new Pair<>(asUnsignedFloat(), value * 2 - 1);
//    }
//
//    @Override
//    public Pair<ControllerSignedFloat, Float> asSignedFloat(Float value) {
//        return new Pair<>(this, value);
//    }
//
//    @Override
//    public Pair<ControllerVec2, Vector2> asVec2(Float value, Vector2 result) {
//        Pair<ControllerSignedFloat, Float> signedFloat = asSignedFloat(value);
//        return signedFloat.getFirst().asVec2(signedFloat.getSecond(), result);
//    }
//
//    @Override
//    public ControllerVec2 asVec2() {
//        switch (this) {
//            case LeftStickX:
//            case LeftStickY:
//            case LeftStickMagnitude:
//                return ControllerVec2.LeftStick;
//            case RightStickX:
//            case RightStickY:
//            case RightStickMagnitude:
//                return ControllerVec2.RightStick;
//            case DpadX:
//            case DpadY:
//            case DpadMagnitude:
//                return ControllerVec2.Dpad;
//            case TriggerMagnitude:
//                return ControllerVec2.Triggers;
//            default:
//                return ControllerVec2.Unknown;
//        }
//    }
//
//    @Override
//    public ControllerIcon getIcon() {
//        switch (this) {
//            case LeftTrigger:
//            case TriggerMagnitude:
//                return ControllerIcon.LeftTrigger;
//            case RightTrigger:
//                return ControllerIcon.RightTrigger;
//            case LeftStickX:
//                return ControllerIcon.LeftJoyStickX;
//            case LeftStickY:
//                return ControllerIcon.LeftJoyStickY;
//            case RightStickX:
//                return ControllerIcon.RightJoyStickX;
//            case RightStickY:
//                return ControllerIcon.RightJoyStickY;
//            case LeftStickMagnitude:
//                return ControllerIcon.LeftJoyStickMove;
//            case RightStickMagnitude:
//                return ControllerIcon.RightJoyStickMove;
//            case DpadX:
//                return ControllerIcon.DpadLeftRight;
//            case DpadY:
//                return ControllerIcon.DpadUpDown;
//            case DpadMagnitude:
//                return ControllerIcon.Dpad;
//            default:
//                return ControllerIcon.AnyJoyStick;
//        }
//    }
//
//    public @MagicConstant(valuesFromClass = SDL_GameControllerAxis.class) int sdlAxis() {
//        switch (this) {
//            case LeftTrigger:
//                return SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_TRIGGERLEFT;
//            case RightTrigger:
//                return SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_TRIGGERRIGHT;
//            case LeftStickX:
//                return SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_LEFTX;
//            case LeftStickY:
//                return SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_LEFTY;
//            case RightStickX:
//                return SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_RIGHTX;
//            case RightStickY:
//                return SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_RIGHTY;
//            default:
//                return SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_INVALID;
//        }
//    }
//}
