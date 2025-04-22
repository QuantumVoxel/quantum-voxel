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
//public enum ControllerUnsignedFloat implements ControllerInterDynamic<Float> {
//    TriggerMagnitude,
//    LeftStickMagnitude,
//    RightStickMagnitude,
//    DpadMagnitude,
//    LeftTrigger,
//    RightTrigger,
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
//                return QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftTrigger);
//            case RightTrigger:
//                return QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightTrigger);
//            case LeftStickX:
//                return (QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftStickX) + 1) / 2;
//            case LeftStickY:
//                return (QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftStickY) + 1) / 2;
//            case RightStickX:
//                return (QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightStickX) + 1) / 2;
//            case RightStickY:
//                return (QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightStickY) + 1) / 2;
//            case DpadX:
//                return (of(ControllerBoolean.DpadLeft, ControllerBoolean.DpadRight) + 1) / 2;
//            case DpadY:
//                return (of(ControllerBoolean.DpadUp, ControllerBoolean.DpadDown) + 1) / 2;
//            case LeftStickMagnitude:
//                return ControllerVec2.LeftStick.getMagnitude();
//            case RightStickMagnitude:
//                return ControllerVec2.RightStick.getMagnitude();
//            case DpadMagnitude:
//                return ControllerVec2.Dpad.getMagnitude();
//            case TriggerMagnitude:
//                return ControllerVec2.Triggers.getMagnitude();
//            default:
//                return 0;
//        }
//    }
//
//    private float of(ControllerBoolean controllerBoolean, ControllerBoolean controllerBoolean1) {
//        if (controllerBoolean.getValue() && controllerBoolean1.getValue()) return 0;
//        else if (controllerBoolean.getValue()) return -1;
//        else if (controllerBoolean1.getValue()) return 1;
//        else return 0;
//    }
//
//    public ControllerBoolean asBoolean() {
//        switch (this) {
//            case LeftTrigger:
//                return ControllerBoolean.LeftTrigger;
//            case RightTrigger:
//                return ControllerBoolean.RightTrigger;
//            case LeftStickX:
//                return ControllerBoolean.LeftStickX;
//            case LeftStickY:
//                return ControllerBoolean.LeftStickY;
//            case RightStickX:
//                return ControllerBoolean.RightStickX;
//            case RightStickY:
//                return ControllerBoolean.RightStickY;
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
//    public ControllerUnsignedFloat asUnsignedFloat() {
//        return this;
//    }
//
//    @Override
//    public ControllerSignedFloat asSignedFloat() {
//        switch (this) {
//            case LeftStickX:
//                return ControllerSignedFloat.LeftStickX;
//            case LeftStickY:
//                return ControllerSignedFloat.LeftStickY;
//            case RightStickX:
//                return ControllerSignedFloat.RightStickX;
//            case RightStickY:
//                return ControllerSignedFloat.RightStickY;
//            case DpadX:
//                return ControllerSignedFloat.DpadX;
//            case DpadY:
//                return ControllerSignedFloat.DpadY;
//            case LeftStickMagnitude:
//                return ControllerSignedFloat.LeftStickMagnitude;
//            case RightStickMagnitude:
//                return ControllerSignedFloat.RightStickMagnitude;
//            case DpadMagnitude:
//                return ControllerSignedFloat.DpadMagnitude;
//            case TriggerMagnitude:
//                return ControllerSignedFloat.TriggerMagnitude;
//            default:
//                return ControllerSignedFloat.Unknown;
//        }
//    }
//
//    @Override
//    public Pair<ControllerUnsignedFloat, Float> asUnsignedFloat(Float value) {
//        return new Pair<>(this, value);
//    }
//
//    @Override
//    public Pair<ControllerBoolean, Boolean> asBoolean(Float value) {
//        return new Pair<>(asBoolean(), value != 0);
//    }
//
//    @Override
//    public Pair<ControllerSignedFloat, Float> asSignedFloat(Float value) {
//        return new Pair<>(asSignedFloat(), value);
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
//            default:
//                return ControllerVec2.Unknown;
//        }
//    }
//
//    @Override
//    public Pair<ControllerVec2, Vector2> asVec2(Float value, Vector2 result) {
//        switch (this) {
//            case LeftStickX:
//                return new Pair<>(ControllerVec2.LeftStick, result.set(value, 0));
//            case LeftStickY:
//                return new Pair<>(ControllerVec2.LeftStick, result.set(0, value));
//            case RightStickX:
//                return new Pair<>(ControllerVec2.RightStick, result.set(value, 0));
//            case RightStickY:
//                return new Pair<>(ControllerVec2.RightStick, result.set(0, value));
//            case LeftStickMagnitude:
//                return new Pair<>(ControllerVec2.LeftStick, result.set(value, value));
//            case RightStickMagnitude:
//                return new Pair<>(ControllerVec2.RightStick, result.set(value, value));
//            case DpadX:
//                return new Pair<>(ControllerVec2.Dpad, result.set(value, 0));
//            case DpadY:
//                return new Pair<>(ControllerVec2.Dpad, result.set(0, value));
//            case DpadMagnitude:
//                return new Pair<>(ControllerVec2.Dpad, result.set(value, value));
//            default:
//                return new Pair<>(ControllerVec2.Unknown, result.set(0, 0));
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
//}
