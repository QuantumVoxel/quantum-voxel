package dev.ultreon.quantum.client.input.controller;

import com.badlogic.gdx.math.Vector2;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.icon.ControllerIcon;
import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerAxis;
import org.intellij.lang.annotations.MagicConstant;

public enum ControllerUnsignedFloat implements ControllerInterDynamic<Float> {
    TriggerMagnitude,
    LeftStickMagnitude,
    RightStickMagnitude,
    DpadMagnitude,
    LeftTrigger,
    RightTrigger,
    LeftStickX,
    LeftStickY,
    RightStickX,
    RightStickY,
    DpadX,
    DpadY,
    Unknown;

    public float getValue() {
        return switch (this) {
            case LeftTrigger -> QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftTrigger);
            case RightTrigger -> QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightTrigger);
            case LeftStickX -> (QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftStickX) + 1) / 2;
            case LeftStickY -> (QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftStickY) + 1) / 2;
            case RightStickX -> (QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightStickX) + 1) / 2;
            case RightStickY -> (QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightStickY) + 1) / 2;
            case DpadX -> (of(ControllerBoolean.DpadLeft, ControllerBoolean.DpadRight) + 1) / 2;
            case DpadY -> (of(ControllerBoolean.DpadUp, ControllerBoolean.DpadDown) + 1) / 2;
            case LeftStickMagnitude -> ControllerVec2.LeftStick.getMagnitude();
            case RightStickMagnitude -> ControllerVec2.RightStick.getMagnitude();
            case DpadMagnitude -> ControllerVec2.Dpad.getMagnitude();
            case TriggerMagnitude -> ControllerVec2.Triggers.getMagnitude();
            default -> 0;
        };
    }

    private float of(ControllerBoolean controllerBoolean, ControllerBoolean controllerBoolean1) {
        if (controllerBoolean.getValue() && controllerBoolean1.getValue()) return 0;
        else if (controllerBoolean.getValue()) return -1;
        else if (controllerBoolean1.getValue()) return 1;
        else return 0;
    }

    public ControllerBoolean asBoolean() {
        return switch (this) {
            case LeftTrigger -> ControllerBoolean.LeftTrigger;
            case RightTrigger -> ControllerBoolean.RightTrigger;
            case LeftStickX -> ControllerBoolean.LeftStickX;
            case LeftStickY -> ControllerBoolean.LeftStickY;
            case RightStickX -> ControllerBoolean.RightStickX;
            case RightStickY -> ControllerBoolean.RightStickY;
            case DpadX -> ControllerBoolean.DpadX;
            case DpadY -> ControllerBoolean.DpadY;
            case LeftStickMagnitude -> ControllerBoolean.LeftStickUsed;
            case RightStickMagnitude -> ControllerBoolean.RightStickUsed;
            case DpadMagnitude -> ControllerBoolean.DpadUsed;
            default -> ControllerBoolean.Unknown;
        };
    }

    @Override
    public ControllerUnsignedFloat asUnsignedFloat() {
        return this;
    }

    @Override
    public ControllerSignedFloat asSignedFloat() {
        return switch (this) {
            case LeftStickX -> ControllerSignedFloat.LeftStickX;
            case LeftStickY -> ControllerSignedFloat.LeftStickY;
            case RightStickX -> ControllerSignedFloat.RightStickX;
            case RightStickY -> ControllerSignedFloat.RightStickY;
            case DpadX -> ControllerSignedFloat.DpadX;
            case DpadY -> ControllerSignedFloat.DpadY;
            case LeftStickMagnitude -> ControllerSignedFloat.LeftStickMagnitude;
            case RightStickMagnitude -> ControllerSignedFloat.RightStickMagnitude;
            case DpadMagnitude -> ControllerSignedFloat.DpadMagnitude;
            case TriggerMagnitude -> ControllerSignedFloat.TriggerMagnitude;
            default -> ControllerSignedFloat.Unknown;
        };
    }

    @Override
    public Pair<ControllerUnsignedFloat, Float> asUnsignedFloat(Float value) {
        return new Pair<>(this, value);
    }

    @Override
    public Pair<ControllerBoolean, Boolean> asBoolean(Float value) {
        return new Pair<>(asBoolean(), value != 0);
    }

    @Override
    public Pair<ControllerSignedFloat, Float> asSignedFloat(Float value) {
        return new Pair<>(asSignedFloat(), value);
    }

    @Override
    public ControllerVec2 asVec2() {
        return switch (this) {
            case LeftStickX, LeftStickY, LeftStickMagnitude -> ControllerVec2.LeftStick;
            case RightStickX, RightStickY, RightStickMagnitude -> ControllerVec2.RightStick;
            case DpadX, DpadY, DpadMagnitude -> ControllerVec2.Dpad;
            default -> ControllerVec2.Unknown;
        };
    }

    @Override
    public Pair<ControllerVec2, Vector2> asVec2(Float value, Vector2 result) {
        return switch (this) {
            case LeftStickX -> new Pair<>(ControllerVec2.LeftStick, result.set(value, 0));
            case LeftStickY -> new Pair<>(ControllerVec2.LeftStick, result.set(0, value));
            case RightStickX -> new Pair<>(ControllerVec2.RightStick, result.set(value, 0));
            case RightStickY -> new Pair<>(ControllerVec2.RightStick, result.set(0, value));
            case LeftStickMagnitude -> new Pair<>(ControllerVec2.LeftStick, result.set(value, value));
            case RightStickMagnitude -> new Pair<>(ControllerVec2.RightStick, result.set(value, value));
            case DpadX -> new Pair<>(ControllerVec2.Dpad, result.set(value, 0));
            case DpadY -> new Pair<>(ControllerVec2.Dpad, result.set(0, value));
            case DpadMagnitude -> new Pair<>(ControllerVec2.Dpad, result.set(value, value));
            default -> new Pair<>(ControllerVec2.Unknown, result.set(0, 0));
        };
    }

    public @MagicConstant(valuesFromClass = SDL_GameControllerAxis.class) int sdlAxis() {
        return switch (this) {
            case LeftTrigger -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_TRIGGERLEFT;
            case RightTrigger -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_TRIGGERRIGHT;
            case LeftStickX -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_LEFTX;
            case LeftStickY -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_LEFTY;
            case RightStickX -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_RIGHTX;
            case RightStickY -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_RIGHTY;
            default -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_INVALID;
        };
    }

    @Override
    public ControllerIcon getIcon() {
        return switch (this) {
            case LeftTrigger, TriggerMagnitude -> ControllerIcon.LeftTrigger;
            case RightTrigger -> ControllerIcon.RightTrigger;
            case LeftStickX -> ControllerIcon.LeftJoyStickX;
            case LeftStickY -> ControllerIcon.LeftJoyStickY;
            case RightStickX -> ControllerIcon.RightJoyStickX;
            case RightStickY -> ControllerIcon.RightJoyStickY;
            case LeftStickMagnitude -> ControllerIcon.LeftJoyStickMove;
            case RightStickMagnitude -> ControllerIcon.RightJoyStickMove;
            case DpadX -> ControllerIcon.DpadLeftRight;
            case DpadY -> ControllerIcon.DpadUpDown;
            case DpadMagnitude -> ControllerIcon.Dpad;
            default -> ControllerIcon.AnyJoyStick;
        };
    }
}
